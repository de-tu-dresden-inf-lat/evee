package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedAxiom
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.CDAnnotationNames
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.{ELKCDProofGenerator, ELKCDReasoner}
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof, IProofGenerator}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._

import scala.collection.JavaConverters._

/**
 * Proofs over OWL axioms for ontologies with a concrete domain, obtained from the proofs of [[ELKCDProofGenerator]].
 * Classes whose constraint is not one of the ontology (Aux1, Aux2, ...) are always shown by their constraint: they
 * become classes whose IRI ends in "#[c]" for the constraint c, with the short names of its data properties. Classes
 * of the ontology with a constraint are shown as they are (the default), or optionally also by their constraint,
 * except those in the goal, so that the proof still proves the goal; steps that become trivial, such as
 * "Same Constraint", are left out.
 *
 * Only OWL API methods that are the same in versions 4 and 5 are used, since Protégé runs this code with version 4.
 *
 * @param givenReasoner the concrete domain reasoner to use for the ontology it was created for; see
 *                      [[ELKCDProofGenerator]]
 */
class OWLCDProofGenerator(givenReasoner: ELKCDReasoner[CDConstraint] = null)
  extends IProofGenerator[OWLAxiom, OWLOntology] {

  import OWLCDProofGenerator._

  private val generator = new ELKCDProofGenerator(givenReasoner)
  private var constraints: Map[OWLClass, CDConstraint] = Map()
  private var inputClassesAsConstraints = false

  /** whether the classes of the ontology that have a constraint are shown by their constraint (default: false) */
  def setInputClassesAsConstraints(value: Boolean): Unit = inputClassesAsConstraints = value

  /** for QLinear: see [[ELKCDProofGenerator.setMaxVariablesForAllOrders]] */
  def setMaxVariablesForAllOrders(max: Int): Unit = generator.setMaxVariablesForAllOrders(max)

  override def setOntology(ontology: OWLOntology): Unit = {
    val extended = ExtendedOntologyParser.toExtendedOntology(ontology)
    constraints = extended.constraintMap
    generator.setOntology(extended)
  }

  /** disposes of the reasoner if the generator created it */
  def dispose(): Unit = generator.dispose()

  override def supportsProof(axiom: OWLAxiom): Boolean = generator.supportsProof(extended(axiom))

  override def successful(): Boolean = true

  override def getProof(goal: OWLAxiom): IProof[OWLAxiom] = {
    val proof = generator.getProof(extended(goal))
    val goalClasses = goal.getClassesInSignature.asScala.toSet
    def render(axiom: ExtendedAxiom[CDConstraint]): OWLAxiom = {
      val shown = axiom.map.filter { case (cls, _) =>
        !goalClasses.contains(cls) && (inputClassesAsConstraints || !constraints.contains(cls)) }
      if (shown.isEmpty) axiom.axiom
      else replace(axiom.axiom, shown.map { case (cls, constraint) => cls -> constraintClass(constraint) })
    }
    val inferences = proof.getInferences.asScala.map(inference => new Inference[OWLAxiom](
      render(inference.getConclusion), inference.getRuleName, inference.getPremises.asScala.map(render).asJava))
      .filterNot(inference => inference.getPremises.contains(inference.getConclusion))
      .toSeq.distinct
    new Proof[OWLAxiom](goal, inferences.map(i => i: IInference[OWLAxiom]).asJava)
  }

  private def extended(axiom: OWLAxiom) = ExtendedAxiom[CDConstraint](axiom, Map())
}

object OWLCDProofGenerator {

  private val factory = OWLManager.getOWLDataFactory
  private val IRI_IN_ANGLE_BRACKETS = "<([^<>\\s]+)>".r

  /** the class that shows the constraint: its IRI ends in "#[c]", with the short names of the data properties */
  def constraintClass(constraint: CDConstraint): OWLClass = {
    val text = IRI_IN_ANGLE_BRACKETS.replaceAllIn(constraint.toString, m =>
      scala.util.matching.Regex.quoteReplacement(IRI.create(m.group(1)).getShortForm))
    factory.getOWLClass(IRI.create(CDAnnotationNames.NAMESPACE + "[" + text + "]"))
  }

  /** the axiom with the classes replaced; only the axioms and class expressions of EL that occur in proofs change */
  def replace(axiom: OWLAxiom, classes: Map[OWLClass, OWLClass]): OWLAxiom = {
    def expression(c: OWLClassExpression): OWLClassExpression = c match {
      case cls: OWLClass => classes.getOrElse(cls, cls)
      case and: OWLObjectIntersectionOf =>
        factory.getOWLObjectIntersectionOf(and.asConjunctSet.asScala.toSeq.map(expression): _*)
      case some: OWLObjectSomeValuesFrom => factory.getOWLObjectSomeValuesFrom(some.getProperty, expression(some.getFiller))
      case other => other
    }
    axiom match {
      case sub: OWLSubClassOfAxiom => factory.getOWLSubClassOfAxiom(expression(sub.getSubClass), expression(sub.getSuperClass))
      case equivalent: OWLEquivalentClassesAxiom =>
        factory.getOWLEquivalentClassesAxiom(equivalent.getClassExpressions.asScala.toSeq.map(expression): _*)
      case disjoint: OWLDisjointClassesAxiom =>
        factory.getOWLDisjointClassesAxiom(disjoint.getClassExpressions.asScala.toSeq.map(expression): _*)
      case assertion: OWLClassAssertionAxiom =>
        factory.getOWLClassAssertionAxiom(expression(assertion.getClassExpression), assertion.getIndividual)
      case other => other
    }
  }
}
