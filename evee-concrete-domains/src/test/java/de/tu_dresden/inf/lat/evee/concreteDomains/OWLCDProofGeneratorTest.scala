package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, load, ontology, owlClass}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedAxiom
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDProofGenerator
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.{ConstraintSizeWeightedTreeSizeMeasure, OWLCDProofGenerator}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.TreeProofGenerator
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLOntology}

import scala.collection.JavaConverters._

/** The proofs over OWL axioms are correct and show constraints as classes "[c]". */
class OWLCDProofGeneratorTest {

  private val factory = OWLManager.getOWLDataFactory

  private def constraintReasoner(domain: ConcreteDomainName): CDReasoner[CDConstraint] = (domain match {
    case ConcreteDomainName.QDiff => new DiffReasoner()
    case ConcreteDomainName.QMult => new MultReasoner()
    case ConcreteDomainName.QLinear => new GaussianEliminationReasoner()
  }).asInstanceOf[CDReasoner[CDConstraint]]

  private def proof(o: OWLOntology, goal: OWLAxiom, inputClassesAsConstraints: Boolean): IProof[OWLAxiom] = {
    val generator = new OWLCDProofGenerator()
    try {
      generator.setInputClassesAsConstraints(inputClassesAsConstraints)
      generator.setOntology(o)
      generator.getProof(goal)
    } finally generator.dispose()
  }

  /**
   * checks the proof with the proof checker, and that no inference derives an axiom from itself; returns a proof of
   * minimal tree size. If the classes of the ontology are shown by their constraints, so are those of its axioms,
   * except the classes of the goal.
   */
  private def assertCorrectProof(o: OWLOntology, goal: OWLAxiom, inputClassesAsConstraints: Boolean): IProof[OWLAxiom] = {
    val owlProof = proof(o, goal, inputClassesAsConstraints)
    val extended = ExtendedOntologyParser.toExtendedOntology(o)
    // all constraints of the proof, from the proof with classes Aux1, Aux2, ...
    val generator = new ELKCDProofGenerator()
    val allConstraints = try {
      generator.setOntology(extended)
      generator.getProof(ExtendedAxiom[CDConstraint](goal, Map())).getInferences.asScala
        .flatMap(inference => (inference.getConclusion +: inference.getPremises.asScala).flatMap(_.map.values)).toSet
    } finally generator.dispose()
    val constraints = extended.constraintMap ++ allConstraints.map(c => OWLCDProofGenerator.constraintClass(c) -> c)

    val asserted = if (!inputClassesAsConstraints) o else {
      val classes = extended.constraintMap.filterKeys(!goal.getClassesInSignature.contains(_))
        .map { case (cls, c) => cls -> OWLCDProofGenerator.constraintClass(c) }
      val manager = OWLManager.createOWLOntologyManager()
      val shown = manager.createOntology()
      manager.addAxioms(shown, o.getLogicalAxioms().asScala.map(OWLCDProofGenerator.replace(_, classes): OWLAxiom).asJava)
      shown
    }
    ProofChecker.forOWL(asserted, constraints, constraintReasoner(extended.concreteDomain), goal).assertCorrect(owlProof)
    for (inference <- owlProof.getInferences.asScala)
      assertFalse("an axiom is derived from itself: " + inference, inference.getPremises.contains(inference.getConclusion))
    TreeProofGenerator.getMinimalTreeSizeProof(owlProof)
  }

  private def assertCorrectProofs(path: String, sub: String, sup: String): Unit = {
    val o = load(path)
    for (inputClassesAsConstraints <- Seq(true, false))
      assertCorrectProof(o, factory.getOWLSubClassOfAxiom(owlClass(o, sub), owlClass(o, sup)), inputClassesAsConstraints)
  }

  private def classes(proof: IProof[OWLAxiom]): Set[OWLClass] =
    proof.getInferences.asScala.flatMap(i => (i.getConclusion +: i.getPremises.asScala).flatMap(_.getClassesInSignature.asScala)).toSet

  private def shown(constraint: String) = factory.getOWLClass(IRI.create(CDAnnotationNames.NAMESPACE + "[" + constraint + "]"))

  @Test def diffPatient(): Unit = assertCorrectProofs("qdiff/patient.ofn", "CurrentPatient", "NeedAttention")
  @Test def linearCoffee(): Unit = assertCorrectProofs("qlinear/coffee.ofn", "Order", "Ristretto")
  @Test def linearExample(): Unit = assertCorrectProofs("qlinear/example.ofn", "System1", "GoodSystem")

  /** the auxiliary classes are always shown by their constraints, the classes of the ontology only if chosen */
  @Test def auxiliaryClasses(): Unit = {
    val o = load("qlinear/patient.ofn")
    val goal = factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), owlClass(o, "NeedAttention"))
    val constraintClasses = ExtendedOntologyParser.toExtendedOntology(o).constraintMap.keySet
    for (inputClassesAsConstraints <- Seq(true, false)) {
      val used = classes(assertCorrectProof(o, goal, inputClassesAsConstraints))
      assertFalse(used.toString, used.exists(_.getIRI.toString.startsWith(CDAnnotationNames.NAMESPACE + "Aux")))
      assertTrue(used.toString, used.exists(_.getIRI.toString.startsWith(CDAnnotationNames.NAMESPACE + "[")))
      assertEquals(used.toString, !inputClassesAsConstraints, used.exists(constraintClasses.contains))
    }
  }

  /** shown by their constraints, the classes of the ontology with the same constraint are the same class */
  @Test def sameConstraint(): Unit = {
    val o = load("qlinear/coffee.ofn")
    val goal = factory.getOWLSubClassOfAxiom(owlClass(o, "Order"), owlClass(o, "Ristretto"))
    val withConstraints = assertCorrectProof(o, goal, inputClassesAsConstraints = true)
    assertFalse(withConstraints.toString, withConstraints.getInferences.asScala.exists(_.getRuleName == "Same Constraint"))
    val withNames = assertCorrectProof(o, goal, inputClassesAsConstraints = false)
    assertTrue(withNames.toString, withNames.getInferences.asScala.exists(_.getRuleName == "Same Constraint"))
  }

  /** the classes of the goal keep their names, so that the proof still proves the goal */
  @Test def goalAsAsked(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x > 1", "C1" -> ":x > 1"), "SubClassOf(:A :C0)")
    val goal = factory.getOWLSubClassOfAxiom(cls("A"), cls("C1"))
    val minimal = assertCorrectProof(o, goal, inputClassesAsConstraints = true)
    assertEquals(goal, minimal.getFinalConclusion)
    assertTrue(minimal.toString, minimal.getInferences.asScala.exists(i => i.getConclusion == goal &&
      i.getRuleName == "Same Constraint" && i.getPremises.asScala == Seq(factory.getOWLSubClassOfAxiom(cls("A"), shown("x > 1")))))
  }

  /** a class with a constraint counts with the size of its constraint, also when it is shown by its constraint */
  @Test def constraintSizes(): Unit = {
    val o = load("qdiff/patient.ofn")
    val generator = new OWLCDProofGenerator()
    try {
      generator.setInputClassesAsConstraints(true)
      generator.setOntology(o)
      val measure = new ConstraintSizeWeightedTreeSizeMeasure(generator.constraints)
      // pp = 65 has 3 symbols, CurrentPatient and ⊑ one each
      assertEquals(5d, measure.leafValue(factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), owlClass(o, "A-pp"))), 0d)
      val proof = generator.getProof(factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), owlClass(o, "NeedAttention")))
      assertTrue(proof.getInferences.asScala.exists(_.getConclusion == factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), shown("pp = 65"))))
      assertEquals(5d, measure.leafValue(factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), shown("pp = 65"))), 0d)
    } finally generator.dispose()
  }

  /** the constraint is shown with the short names of the data properties and fractions */
  @Test def constraintClass(): Unit = {
    val o = ontology("QLinear", Seq("C0" -> "2 * :x + :y = 1", "C1" -> ":x = 1/2"))
    val constraints = ExtendedOntologyParser.toExtendedOntology(o).constraintMap
    assertEquals(shown("2*x + y = 1"), OWLCDProofGenerator.constraintClass(constraints(cls("C0"))))
    assertEquals(shown("x = 1/2"), OWLCDProofGenerator.constraintClass(constraints(cls("C1"))))
  }
}
