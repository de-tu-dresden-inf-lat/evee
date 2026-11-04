package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDConstraint, CDProofGenerator, ConstraintPropagationReasoner}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.{ExtendedAxiom, ExtendedOntology}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationProver
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import de.tu_dresden.inf.lat.evee.proofGenerators.ELKProofGenerator
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof, IProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import org.semanticweb.elk.owlapi.ElkReasoner
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLClassExpression, OWLSubClassOfAxiom}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Proofs for entailments of an ontology with a concrete domain, which combine ELK proofs with proofs of the concrete
 * domain, following the construction in the proof of Lemma 6 of "Combining Proofs for Description Logic and Concrete
 * Domain Reasoning" (Alrabbaa et al., 2023) and the proof format in the plan of the concreteDomains branch.
 *
 * ELK proves the entailment from the internal ontology of the concrete domain reasoner, which contains the axioms
 * L ⊑ E that the reasoner added because of the concrete domain; L is a conjunction of classes with constraints and E
 * a class with a constraint or owl:Nothing. These axioms are never asserted in the proofs. Instead, the steps of a
 * proof of the constraint of E (or a contradiction) from the constraints of the conjuncts of L are lifted to a context
 * X: a step α1 ... αn ⊢ β becomes X ⊑ A1, ..., X ⊑ An ⊢ X ⊑ B, where Ai and B are classes with these constraints.
 *  - If an added axiom L ⊑ E is the second premise of a step C ⊑ L, L ⊑ E ⊢ C ⊑ E, there are two alternatives. Either
 *    the step is replaced by the constraint proof in the context C, where premises C ⊑ Ci for the conjuncts Ci of L
 *    are taken from the ELK proof if it derives them, and are otherwise derived from C ⊑ L and L ⊑ Ci (Intersection
 *    Decomposition). Or the step is kept, and the added axiom is proved in the context L as below.
 *  - Otherwise, e.g., inside an existential restriction or if it is the goal, the added axiom itself is proved by the
 *    constraint proof in the context L, with the premises L ⊑ Ci by Intersection Decomposition (or Class Inclusion
 *    Tautology if L is a single class).
 *
 * Constraints are expressed by classes: those of the ontology for constraints that classes of the ontology have, and
 * fresh classes Aux1, Aux2, ... for the other constraints; the map of each ExtendedAxiom gives their constraints.
 * Contradictions are owl:Nothing, and steps whose conclusion is among their premises are left out.
 *
 * The proofs are derivation structures with the alternatives of ELK and of the constraint provers; minimisation
 * selects a proof. The ontology is never changed.
 *
 * @param initialReasoner the concrete domain reasoner to use for the ontology it was created for; for other
 *                        ontologies, the generator creates its own reasoner (see [[setReasoner]])
 */
class ELKCDProofGenerator(initialReasoner: ELKCDReasoner[CDConstraint] = null)
  extends IProofGenerator[ExtendedAxiom[CDConstraint], ExtendedOntology[CDConstraint]] {

  private val factory = OWLManager.getOWLDataFactory
  private val logger = LoggerFactory.getLogger(classOf[ELKCDProofGenerator])

  private var givenReasoner: ELKCDReasoner[CDConstraint] = initialReasoner
  private var reasoner: ELKCDReasoner[CDConstraint] = _
  /** whether this generator created the reasoner, and therefore has to dispose of it */
  private var ownReasoner = false
  private var prover: CDProofGenerator[CDConstraint] = _
  private var maxVariablesForAllOrders = GaussianEliminationProver.DEFAULT_MAX_VARIABLES_FOR_ALL_ORDERS

  /** for QLinear: see [[GaussianEliminationProver.setMaxVariablesForAllOrders]] */
  def setMaxVariablesForAllOrders(max: Int): Unit = {
    maxVariablesForAllOrders = max
    (prover: Any) match {
      case linear: GaussianEliminationProver => linear.setMaxVariablesForAllOrders(max)
      case _ =>
    }
  }

  private var maxVariablesForAllPaths = ConstraintPropagationReasoner.DEFAULT_MAX_VARIABLES_FOR_ALL_PATHS

  /**
   * for QDiff and QMult: see [[ConstraintPropagationReasoner.setMaxVariablesForAllPaths]]; above the limit, the
   * complete networks are used (DiffNetworkComplete, MultNetworkComplete)
   */
  def setMaxVariablesForAllPaths(max: Int): Unit = {
    maxVariablesForAllPaths = max
    (prover: Any) match {
      case propagation: ConstraintPropagationReasoner[_, _] => propagation.setMaxVariablesForAllPaths(max)
      case _ =>
    }
  }

  /**
   * The concrete domain reasoner to use from the next call of [[setOntology]] on, if it is for that ontology; null to
   * let the generator use its own reasoner. The generator never disposes of it.
   */
  def setReasoner(reasoner: ELKCDReasoner[CDConstraint]): Unit = givenReasoner = reasoner

  /**
   * Sets the ontology. The given concrete domain reasoner is used if it is for this ontology; otherwise, the
   * generator creates a non-buffering reasoner, which takes changes of the ontology into account.
   */
  override def setOntology(ontology: ExtendedOntology[CDConstraint]): Unit = {
    if (givenReasoner != null && givenReasoner.getRootOntology == ontology.ontology) {
      if (reasoner ne givenReasoner) {
        dispose()
        reasoner = givenReasoner
      }
    } else if (reasoner == null || !ownReasoner || reasoner.getRootOntology != ontology.ontology) {
      dispose()
      reasoner = new OWLCDReasonerFactory().createNonBufferingReasoner(ontology.ontology)
        .asInstanceOf[ELKCDReasoner[CDConstraint]]
      ownReasoner = true
    }
    prover = constraintProver(reasoner.extendedOntology.concreteDomain)
  }

  /**
   * the constraints of the classes, as the reasoner read them when it last loaded the ontology; the proofs are for
   * that state of the ontology
   */
  def constraintMap: Map[OWLClass, CDConstraint] =
    if (reasoner == null) Map() else reasoner.extendedOntology.constraintMap

  /** disposes of the reasoner if the generator created it */
  def dispose(): Unit = {
    if (ownReasoner && reasoner != null)
      reasoner.dispose()
    reasoner = null
    ownReasoner = false
  }

  override def supportsProof(axiom: ExtendedAxiom[CDConstraint]): Boolean =
    reasoner != null && reasoner.isEntailmentCheckingSupported(axiom.axiom.getAxiomType) &&
      reasoner.isEntailed(axiom.axiom)

  override def getProof(goal: ExtendedAxiom[CDConstraint]): IProof[ExtendedAxiom[CDConstraint]] = {
    if (reasoner == null)
      throw new ProofGenerationException("No ontology has been set")
    if (!reasoner.isEntailed(goal.axiom))
      throw new ProofGenerationException("The axiom is not entailed: " + goal.axiom)
    val start = System.nanoTime()
    val proof = new Construction(goal.axiom).proof
    if (logger.isDebugEnabled)
      logger.debug(s"Derivation structure for ${goal.axiom}: ${proof.getInferences.size} inferences in " +
        s"${(System.nanoTime() - start) / 1000000} ms")
    proof
  }

  /** how the prover proves constraints with this many variables, for debug messages */
  private def method(variables: Int): String = (prover: Any) match {
    case _: GaussianEliminationProver =>
      if (variables <= maxVariablesForAllOrders) "all elimination orders" else "one elimination order"
    case _ => if (variables <= maxVariablesForAllPaths) "all paths" else "complete network"
  }

  override def successful(): Boolean = true

  private def constraintProver(domain: ConcreteDomainName): CDProofGenerator[CDConstraint] = (domain match {
    case ConcreteDomainName.QDiff =>
      val diff = new DiffReasoner()
      diff.setCompleteReasoning()
      diff.setMaxVariablesForAllPaths(maxVariablesForAllPaths)
      diff
    case ConcreteDomainName.QMult =>
      val mult = new MultReasoner()
      mult.setCompleteReasoning()
      mult.setMaxVariablesForAllPaths(maxVariablesForAllPaths)
      mult
    case ConcreteDomainName.QLinear =>
      val linear = new GaussianEliminationProver()
      linear.setMaxVariablesForAllOrders(maxVariablesForAllOrders)
      linear
    case _ => null
  }).asInstanceOf[CDProofGenerator[CDConstraint]]

  /** the construction of one proof, for the current state of the reasoner */
  private class Construction(goal: OWLAxiom) {

    private val elkProof = new ELKProofGenerator(reasoner.internalReasoner.asInstanceOf[ElkReasoner]).getProof(goal)
    private val added: Set[OWLAxiom] = reasoner.concreteDomainAxioms
    private val constraints: Map[OWLClass, CDConstraint] = reasoner.extendedOntology.constraintMap
    /** for each constraint, the classes of the ontology that have it, ordered by IRI */
    private val classesOf: Map[CDConstraint, Seq[OWLClass]] =
      constraints.groupBy(_._2).mapValues(_.keys.toSeq.sortBy(_.getIRI.toString)).toMap
    private val auxiliaryClasses = mutable.LinkedHashMap[CDConstraint, OWLClass]()
    private val elkConclusions: Set[OWLAxiom] = elkProof.getInferences.asScala.map(_.getConclusion).toSet

    private val inferences = mutable.LinkedHashSet[IInference[OWLAxiom]]()
    /**
     * the steps of the constraint proofs, sorted, for each set of given constraints and goal (None for a
     * contradiction); both alternatives of a step C ⊑ L, L ⊑ E ⊢ C ⊑ E need the same constraint proof
     */
    private val constraintSteps = mutable.Map[(Set[CDConstraint], Option[CDConstraint]), Seq[IInference[CDConstraint]]]()

    val proof: IProof[ExtendedAxiom[CDConstraint]] = {
      // the added axioms that need their own proof in the context of their left-hand side
      val provedInOwnContext = mutable.LinkedHashSet[OWLSubClassOfAxiom]()
      for (inference <- elkProof.getInferences.asScala) {
        val premises = inference.getPremises.asScala.toList
        premises match {
          case List(first: OWLSubClassOfAxiom, second: OWLSubClassOfAxiom)
            if added.contains(second) && first.getSuperClass == second.getSubClass &&
              inference.getConclusion == factory.getOWLSubClassOfAxiom(first.getSubClass, second.getSuperClass) =>
            // both alternatives, for minimisation: the context C is smaller if the ELK proof derives C ⊑ Ci anyway; the
            // context L needs one Class Hierarchy step for C ⊑ E instead of one for each C ⊑ Ci
            liftInContext(second, first.getSubClass, Some(first))
            inferences += inference
            provedInOwnContext += second
          case _ if ProofTools.isAsserted(inference) && added.contains(inference.getConclusion) =>
          case _ =>
            inferences += inference
            provedInOwnContext ++= premises.collect { case premise: OWLSubClassOfAxiom if added.contains(premise) => premise }
        }
      }
      goal match {
        case subsumption: OWLSubClassOfAxiom if added.contains(subsumption) => provedInOwnContext += subsumption
        case _ =>
      }
      provedInOwnContext.foreach(axiom => liftInContext(axiom, axiom.getSubClass, None))

      val allConstraints = constraints ++ auxiliaryClasses.map(_.swap)
      // each axiom occurs in several inferences
      val extendedAxioms = mutable.Map[OWLAxiom, ExtendedAxiom[CDConstraint]]()
      def extended(axiom: OWLAxiom) = extendedAxioms.getOrElseUpdate(axiom, ExtendedAxiom[CDConstraint](axiom,
        axiom.getClassesInSignature.asScala.filter(allConstraints.contains).map(c => c -> allConstraints(c)).toMap))
      val result: Seq[IInference[ExtendedAxiom[CDConstraint]]] = inferences.toSeq
        .filterNot(inference => inference.getPremises.contains(inference.getConclusion))
        .map(inference => new Inference(extended(inference.getConclusion), inference.getRuleName,
          inference.getPremises.asScala.map(extended).asJava))
      new Proof(extended(goal), result.asJava)
    }

    /**
     * Adds the steps that prove the added axiom L ⊑ E in the given context X, i.e., derive X ⊑ E. In the context of
     * a class expression C with the ELK premise C ⊑ L, the premises C ⊑ Ci are taken from the ELK proof or derived
     * from C ⊑ L; in the context of L, they are tautologies.
     */
    private def liftInContext(axiom: OWLSubClassOfAxiom, context: OWLClassExpression,
                              contextPremise: Option[OWLSubClassOfAxiom]): Unit = {
      val lhs = axiom.getSubClass
      val conjuncts = lhs.asConjunctSet.asScala.toSeq.collect { case c: OWLClass if constraints.contains(c) => c }
        .sortBy(_.getIRI.toString)
      // the class of each given constraint; of several conjuncts with the same constraint, the first one
      val given: Map[CDConstraint, OWLClass] = conjuncts.reverse.map(c => constraints(c) -> c).toMap
      val target = axiom.getSuperClass
      val goalConstraint: Option[CDConstraint] = if (target.isOWLNothing) None else Some(constraints(target.asOWLClass))

      def owlClass(constraint: CDConstraint): OWLClassExpression =
        if (constraint.isInconsistent) factory.getOWLNothing
        else given.get(constraint)
          .orElse(if (goalConstraint.contains(constraint)) Some(target.asOWLClass) else None)
          .orElse(classesOf.get(constraint).map(_.head))
          .getOrElse(auxiliaryClasses.getOrElseUpdate(constraint,
            factory.getOWLClass(IRI.create(CDAnnotationNames.NAMESPACE + "Aux" + (auxiliaryClasses.size + 1)))))
      def inContext(c: OWLClassExpression): OWLAxiom = factory.getOWLSubClassOfAxiom(context, c)

      // the premises X ⊑ Ci for the given constraints
      for (conjunct <- given.values) {
        val premise = inContext(conjunct)
        if (contextPremise.isEmpty) {
          if (conjuncts.size == 1) inferences += new Inference[OWLAxiom](premise, CLASS_INCLUSION_TAUTOLOGY, List[OWLAxiom]().asJava)
          else inferences += new Inference[OWLAxiom](premise, INTERSECTION_DECOMPOSITION, List[OWLAxiom]().asJava)
        } else if (!elkConclusions.contains(premise)) {
          val decomposition = factory.getOWLSubClassOfAxiom(lhs, conjunct)
          inferences += new Inference[OWLAxiom](decomposition, INTERSECTION_DECOMPOSITION, List[OWLAxiom]().asJava)
          inferences += new Inference[OWLAxiom](premise, CLASS_HIERARCHY, List[OWLAxiom](contextPremise.get, decomposition).asJava)
        }
      }

      goalConstraint.filter(given.contains) match {
        case Some(constraint) =>
          // E has the same constraint as a conjunct of L
          inferences += new Inference[OWLAxiom](inContext(target), SAME_CONSTRAINT, List(inContext(given(constraint))).asJava)
        case None =>
          val steps = constraintSteps.getOrElseUpdate((given.keySet, goalConstraint), {
            val start = System.nanoTime()
            prover.setOntology(given.keys)
            val constraintProof = goalConstraint match {
              case Some(constraint) => prover.getProof(constraint)
              case None => prover.proveInconsistency()
            }
            if (logger.isDebugEnabled) {
              val variables = (given.keys ++ goalConstraint).flatMap(_.getOWLDataProperties).toSet.size
              logger.debug(s"Constraint proof of ${goalConstraint.getOrElse("a contradiction")} from ${given.size} " +
                s"constraints with $variables variables (${method(variables)}): " +
                s"${constraintProof.getInferences.size} inferences in ${(System.nanoTime() - start) / 1000000} ms")
            }
            // sorted, so that the auxiliary classes are numbered in the same way each time; each step is converted to
            // a string only once, because sortBy would convert it in every comparison
            constraintProof.getInferences.asScala.toSeq.map(step => step.toString -> step).sortBy(_._1).map(_._2)
          })
          for (step <- steps if !ProofTools.isAsserted(step))
            inferences += new Inference[OWLAxiom](inContext(owlClass(step.getConclusion)), step.getRuleName,
              step.getPremises.asScala.map(p => inContext(owlClass(p))).asJava)
      }
    }
  }

  private val CLASS_HIERARCHY = "Class Hierarchy"
  private val INTERSECTION_DECOMPOSITION = "Intersection Decomposition"
  private val CLASS_INCLUSION_TAUTOLOGY = "Class Inclusion Tautology"
  private val SAME_CONSTRAINT = "Same Constraint"
}
