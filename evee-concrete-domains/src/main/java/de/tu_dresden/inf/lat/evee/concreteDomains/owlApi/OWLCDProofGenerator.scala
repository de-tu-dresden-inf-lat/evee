package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDConstraint, ConstraintTokens}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedAxiom
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.CDAnnotationNames
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.{ELKCDProofGenerator, ELKCDReasoner}
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof, IProofGenerator}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.PrefixDocumentFormat
import org.semanticweb.owlapi.model._

import scala.collection.JavaConverters._

/**
 * Proofs over OWL axioms for ontologies with a concrete domain, obtained from the proofs of [[ELKCDProofGenerator]].
 * Classes whose constraint is not one of the ontology (Aux1, Aux2, ...) are always shown by their constraint: they
 * become classes whose IRI ends in "#c" for the constraint c, written as in a cd:constraint annotation of the ontology
 * (see [[OWLCDProofGenerator.constraintClass]]). Classes of the ontology with a constraint are shown as they are (the
 * default), or optionally also by their constraint, except those in the goal, so that the proof still proves the
 * goal; steps that become trivial, such as "Same Constraint", are left out.
 *
 * Only OWL API methods that are the same in versions 4 and 5 are used, since Protégé runs this code with version 4.
 *
 * @param initialReasoner the concrete domain reasoner to use for the ontology it was created for; see
 *                        [[ELKCDProofGenerator]]
 */
class OWLCDProofGenerator(initialReasoner: ELKCDReasoner[CDConstraint] = null)
  extends IProofGenerator[OWLAxiom, OWLOntology] {

  import OWLCDProofGenerator._

  private val generator = new ELKCDProofGenerator(initialReasoner)
  /** the classes of the ontology that have a constraint */
  private var inputConstraints: Map[OWLClass, CDConstraint] = Map()
  private var prefixes: Map[String, String] = Map()
  /** the classes of the ontology that have a constraint and the classes that show a constraint in the proofs so far */
  private val classConstraints = scala.collection.mutable.Map[OWLClass, CDConstraint]()
  private var inputClassesAsConstraints = false

  /** whether the classes of the ontology that have a constraint are shown by their constraint (default: false) */
  def setInputClassesAsConstraints(value: Boolean): Unit = inputClassesAsConstraints = value

  /** see [[ELKCDProofGenerator.setReasoner]] */
  def setReasoner(reasoner: ELKCDReasoner[CDConstraint]): Unit = generator.setReasoner(reasoner)

  /**
   * the constraints of the classes in the proofs: of the classes of the ontology that have one, and of the classes that
   * show one. The map is filled again by setOntology and grows with each proof.
   */
  def constraints: scala.collection.Map[OWLClass, CDConstraint] = classConstraints

  /** for QLinear: see [[ELKCDProofGenerator.setMaxVariablesForAllOrders]] */
  def setMaxVariablesForAllOrders(max: Int): Unit = generator.setMaxVariablesForAllOrders(max)

  override def setOntology(ontology: OWLOntology): Unit = {
    generator.setOntology(ExtendedOntologyParser.toExtendedOntology(ontology))
    inputConstraints = generator.constraintMap
    prefixes = prefixesOf(ontology)
    classConstraints.clear()
    classConstraints ++= inputConstraints
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
        !goalClasses.contains(cls) && (inputClassesAsConstraints || !inputConstraints.contains(cls)) }
      if (shown.isEmpty) axiom.axiom
      else replace(axiom.axiom, shown.map { case (cls, constraint) =>
        val shownClass = constraintClass(constraint, prefixes)
        classConstraints(shownClass) = constraint
        cls -> shownClass
      })
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

  /**
   * the namespace of the classes that show constraints. Ontologies declare no prefix for it, so Protégé shows such a
   * class by the text after "#", like a class of the ontology rendered by its cd:constraint annotation.
   */
  val CONSTRAINT_CLASS_NAMESPACE: String = CDAnnotationNames.NAMESPACE.stripSuffix("#") + "/constraint#"

  /** the prefixes of the ontology, from prefix names such as ":" and "ex:" to their namespaces */
  def prefixesOf(ontology: OWLOntology): Map[String, String] =
    Option(ontology.getOWLOntologyManager.getOntologyFormat(ontology))
      .collect { case format: PrefixDocumentFormat => format.getPrefixName2PrefixMap.asScala.toMap }.getOrElse(Map())

  /**
   * the class that shows the constraint: its IRI is [[CONSTRAINT_CLASS_NAMESPACE]] followed by the constraint, with
   * its data properties as prefixed names with the given prefixes, e.g. ":x + 2 = :y". A data property without a
   * suitable prefix is written by the short form of its IRI.
   */
  def constraintClass(constraint: CDConstraint, prefixes: Map[String, String]): OWLClass = {
    val text = IRI_IN_ANGLE_BRACKETS.replaceAllIn(constraint.toString, m =>
      scala.util.matching.Regex.quoteReplacement(prefixedName(IRI.create(m.group(1)), prefixes)))
    factory.getOWLClass(IRI.create(CONSTRAINT_CLASS_NAMESPACE + text))
  }

  /** the IRI as a prefixed name, with the longest namespace that gives a name; otherwise its short form */
  private def prefixedName(iri: IRI, prefixes: Map[String, String]): String = {
    val names = prefixes.toSeq.collect { case (prefix, namespace) if iri.toString.startsWith(namespace) =>
      (namespace, prefix, prefix + iri.toString.substring(namespace.length))
    }.filter { case (_, _, name) => ConstraintTokens.isName(name) }
    if (names.isEmpty) iri.getShortForm
    else names.minBy { case (namespace, prefix, _) => (-namespace.length, prefix.length, prefix) }._3
  }

  /** the classes in the axiom, once for each occurrence, in the axioms and class expressions of EL */
  def classOccurrences(axiom: OWLAxiom): Seq[OWLClass] = {
    def expression(c: OWLClassExpression): Seq[OWLClass] = c match {
      case cls: OWLClass => Seq(cls)
      case and: OWLObjectIntersectionOf => and.asConjunctSet.asScala.toSeq.flatMap(expression)
      case some: OWLObjectSomeValuesFrom => expression(some.getFiller)
      case _ => Seq()
    }
    axiom match {
      case sub: OWLSubClassOfAxiom => expression(sub.getSubClass) ++ expression(sub.getSuperClass)
      case equivalent: OWLEquivalentClassesAxiom => equivalent.getClassExpressions.asScala.toSeq.flatMap(expression)
      case disjoint: OWLDisjointClassesAxiom => disjoint.getClassExpressions.asScala.toSeq.flatMap(expression)
      case assertion: OWLClassAssertionAxiom => expression(assertion.getClassExpression)
      case _ => Seq()
    }
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
