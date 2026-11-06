package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.and
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, load, ontology, owlClass}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.{ExtendedAxiom, ExtendedOntology}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.{ELKCDProofGenerator, ELKCDReasoner}
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.MultReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.TreeProofGenerator
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClassExpression, OWLOntology}

import scala.collection.JavaConverters._

/** The proofs of entailments that depend on the concrete domain are correct and follow the proof format. */
class ELKCDProofGeneratorTest {

  private val factory = OWLManager.getOWLDataFactory

  private def constraintReasoner(domain: ConcreteDomainName): CDReasoner[CDConstraint] = (domain match {
    case ConcreteDomainName.QDiff => new DiffReasoner()
    case ConcreteDomainName.QMult => new MultReasoner()
    case ConcreteDomainName.QLinear => new GaussianEliminationReasoner()
  }).asInstanceOf[CDReasoner[CDConstraint]]

  private def extended(o: OWLOntology): ExtendedOntology[CDConstraint] = ExtendedOntologyParser.toExtendedOntology(o)

  /** the derivation structure for the goal */
  private def proof(o: OWLOntology, goal: OWLAxiom): IProof[ExtendedAxiom[CDConstraint]] = {
    val generator = new ELKCDProofGenerator()
    try {
      generator.setOntology(extended(o))
      generator.getProof(ExtendedAxiom[CDConstraint](goal, Map()))
    } finally generator.dispose()
  }

  /**
   * checks the derivation structure for the goal with the proof checker, and that no inference derives an axiom from
   * itself; returns a proof of minimal tree size
   */
  private def assertCorrectProof(o: OWLOntology, goal: OWLAxiom): IProof[ExtendedAxiom[CDConstraint]] = {
    val extendedProof = proof(o, goal)
    val inferences = extendedProof.getInferences.asScala
    // the constraints of all classes in the proof, including auxiliary ones
    val constraints = extended(o).constraintMap ++
      inferences.flatMap(inference => (inference.getConclusion +: inference.getPremises.asScala).flatMap(_.map))
    val owlProof = new Proof[OWLAxiom](goal, inferences.map(inference =>
      new Inference[OWLAxiom](inference.getConclusion.axiom, inference.getRuleName,
        inference.getPremises.asScala.map(_.axiom).asJava): IInference[OWLAxiom]).asJava)
    ProofChecker.forOWL(o, constraints, constraintReasoner(extended(o).concreteDomain), goal).assertCorrect(owlProof)
    for (inference <- inferences)
      assertFalse("an axiom is derived from itself: " + inference, inference.getPremises.contains(inference.getConclusion))
    TreeProofGenerator.getMinimalTreeSizeProof(extendedProof)
  }

  private def assertCorrectProof(path: String, sub: String, sup: String): Unit = {
    val o = load(path)
    assertCorrectProof(o, factory.getOWLSubClassOfAxiom(owlClass(o, sub), owlClass(o, sup)))
  }

  /** whether the proof has a step with this conclusion, rule and premises */
  private def hasStep(proof: IProof[ExtendedAxiom[CDConstraint]], conclusion: OWLAxiom, rule: String,
                      premises: OWLAxiom*): Boolean =
    proof.getInferences.asScala.exists(inference => inference.getConclusion.axiom == conclusion &&
      inference.getRuleName == rule && inference.getPremises.asScala.map(_.axiom).toSet == premises.toSet)

  private def sub(c: OWLClassExpression, d: OWLClassExpression) = factory.getOWLSubClassOfAxiom(c, d)

  @Test def diffPatient(): Unit = assertCorrectProof("qdiff/patient.ofn", "CurrentPatient", "NeedAttention")
  @Test def diffExample(): Unit = assertCorrectProof("qdiff/example-cd2.ofn", "Drone1", "SmallLargeDrone")
  @Test def diffToyDrone(): Unit = assertCorrectProof("qdiff/toyDrone.ofn", "Drone", "Cat")
  @Test def diffToyDrone2(): Unit = assertCorrectProof("qdiff/toyDrone2.ofn", "Drone", "Hexicopter")
  @Test def diffDrones1a(): Unit = assertCorrectProof("qdiff/ddist1a.ofn", "World", "PartiallySafe")
  @Test def diffDrones13c(): Unit = assertCorrectProof("qdiff/ddist13c.ofn", "World", "PartiallySafe")
  @Test def diffDrones13cUnique(): Unit = assertCorrectProof("qdiff/ddist13c-unique.ofn", "World", "PartiallySafe")
  @Test def linearCoffee(): Unit = assertCorrectProof("qlinear/coffee.ofn", "Order", "Ristretto")
  @Test def linearPatient(): Unit = assertCorrectProof("qlinear/patient.ofn", "CurrentPatient", "NeedAttention")
  @Test def linearExample(): Unit = assertCorrectProof("qlinear/example.ofn", "System1", "GoodSystem")
  @Test def linearExample2(): Unit = assertCorrectProof("qlinear/example2.ofn", "A1", "B3")
  @Test def linearArtificial3a(): Unit = assertCorrectProof("qlinear/artificial3a.ofn", "A", "B")
  @Test def linearArtificial3aUnique(): Unit = assertCorrectProof("qlinear/artificial3a-unique.ofn", "A", "B")

  /**
   * the added axiom A-pp ⊑ PulsePressureLargerThan50mmHG is proved in its own context, but the step in the context
   * CurrentPatient is smaller
   */
  @Test def contextOfTheSubclass(): Unit = {
    val o = load("qdiff/patient.ofn")
    def c(name: String) = owlClass(o, name)
    val goal = sub(c("CurrentPatient"), c("NeedAttention"))
    val added = sub(c("A-pp"), c("PulsePressureLargerThan50mmHG"))
    assertTrue("the added axiom should be proved", proof(o, goal).getInferences.asScala.exists(_.getConclusion.axiom == added))
    val minimal = assertCorrectProof(o, goal)
    assertTrue(minimal.toString, hasStep(minimal, sub(c("CurrentPatient"), c("PulsePressureLargerThan50mmHG")),
      "Introduce >", sub(c("CurrentPatient"), c("A-pp"))))
    assertFalse("the added axiom should not occur", minimal.getInferences.asScala.exists(_.getConclusion.axiom == added))
  }

  /**
   * the added axiom C0 ⊓ C1 ⊑ C2 is used with A ⊑ C0 ⊓ C1: in the context A, A ⊑ C0 is derived from A ⊑ C0 ⊓ C1 as ELK
   * does, but the proof in the context C0 ⊓ C1 is smaller
   */
  @Test def bothContexts(): Unit = {
    val o = ontology("QDiff",
      Seq("C0" -> ":x = 0", "C1" -> ":x + 1 = :y", "C2" -> ":y = 1"), "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))")
    val all = proof(o, subClassOf("A", "C2"))
    assertTrue(all.toString, hasStep(all, subClassOf("A", "C0"), "Class Hierarchy",
      sub(cls("A"), and("C0", "C1")), sub(and("C0", "C1"), cls("C0"))))
    assertTrue(all.toString, hasStep(all, subClassOf("A", "C2"), "Propagate =",
      subClassOf("A", "C0"), subClassOf("A", "C1")))
    val minimal = assertCorrectProof(o, subClassOf("A", "C2"))
    assertTrue(minimal.toString, hasStep(minimal, sub(and("C0", "C1"), cls("C0")), "Intersection Decomposition"))
    assertTrue(minimal.toString, hasStep(minimal, sub(and("C0", "C1"), cls("C2")), "Propagate =",
      sub(and("C0", "C1"), cls("C0")), sub(and("C0", "C1"), cls("C1"))))
    assertTrue(minimal.toString, hasStep(minimal, subClassOf("A", "C2"), "Class Hierarchy",
      sub(cls("A"), and("C0", "C1")), sub(and("C0", "C1"), cls("C2"))))
  }

  /** the goal is an added axiom, proved in the context of its left-hand side, a single class */
  @Test def contextOfASingleClass(): Unit = {
    val minimal = assertCorrectProof(ontology("QMult", Seq("C0" -> ":x = 5", "C1" -> ":x < 6")), subClassOf("C0", "C1"))
    assertTrue(minimal.toString, hasStep(minimal, subClassOf("C0", "C0"), "Class Inclusion Tautology"))
    assertTrue(minimal.toString, minimal.getInferences.asScala.exists(i => i.getConclusion.axiom == subClassOf("C0", "C1") &&
      i.getPremises.asScala.map(_.axiom) == Seq(subClassOf("C0", "C0"))))
  }

  /** the added axiom C0 ⊓ C1 ⊑ ⊥ is used inside an existential restriction, so it is proved in its own context */
  @Test def contextOfAConjunction(): Unit = {
    val minimal = assertCorrectProof(ontology("QDiff", Seq("C0" -> ":x = 0", "C1" -> ":x > 1"),
      "SubClassOf(:E ObjectSomeValuesFrom(:r ObjectIntersectionOf(:C0 :C1)))"), sub(cls("E"), factory.getOWLNothing))
    for (name <- Seq("C0", "C1"))
      assertTrue(minimal.toString, hasStep(minimal, sub(and("C0", "C1"), cls(name)), "Intersection Decomposition"))
    assertTrue(minimal.toString, hasStep(minimal, sub(and("C0", "C1"), factory.getOWLNothing), "Constant too small",
      sub(and("C0", "C1"), cls("C0")), sub(and("C0", "C1"), cls("C1"))))
  }

  /** an intermediate constraint, e.g. maxHR = 178 or age + hr = 215, has no class in the ontology */
  @Test def auxiliaryClasses(): Unit = {
    val o = load("qlinear/patient.ofn")
    val minimal = assertCorrectProof(o, factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), owlClass(o, "NeedAttention")))
    val auxiliary = minimal.getInferences.asScala.flatMap(_.getConclusion.map)
      .filter(_._1.getIRI.toString.startsWith(CDAnnotationNames.NAMESPACE + "Aux"))
    assertEquals(minimal.toString, 1, auxiliary.map(_._1).toSet.size)
    assertTrue(minimal.toString, minimal.getInferences.asScala.forall(i =>
      !i.getRuleName.startsWith("[") && (i.getRuleName.contains(" ") || !i.getRuleName.matches(".*[0-9].*"))))
    assertTrue(minimal.toString, minimal.getInferences.asScala.exists(_.getRuleName.startsWith("Linear combination [")))
  }

  /** C1 has the same constraint as C0 */
  @Test def sameConstraint(): Unit = {
    val minimal = assertCorrectProof(ontology("QDiff", Seq("C0" -> ":x > 1", "C1" -> ":x > 1"), "SubClassOf(:A :C0)"),
      subClassOf("A", "C1"))
    assertTrue(minimal.toString, hasStep(minimal, subClassOf("A", "C1"), "Same Constraint", subClassOf("A", "C0")))
  }

  /** P has a value for hr because of hr = 83 */
  @Test def defined(): Unit = {
    val minimal = assertCorrectProof(ontology("QDiff", Seq("HasValue" -> "def(:hr)", "Hr83" -> ":hr = 83"),
      "SubClassOf(:P :Hr83)"), subClassOf("P", "HasValue"))
    assertTrue(minimal.toString, hasStep(minimal, subClassOf("P", "HasValue"), "Defined", subClassOf("P", "Hr83")))
  }

  @Test def mult(): Unit = assertCorrectProof(ontology("QMult",
    Seq("C0" -> ":x * 2 = :y", "C1" -> ":y < 6", "C2" -> ":x < 3"), "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"),
    subClassOf("A", "C2"))

  /** x = y = 0 satisfy every factor, which the step from the two values shows; A is satisfiable */
  @Test def multZero(): Unit = {
    val o = ontology("QMult", Seq("C0" -> "2 * :x = :y", "C1" -> "3 * :x = :y", "C2" -> "5 * :x = :y"),
      "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try assertTrue("A should be satisfiable", reasoner.isSatisfiable(cls("A")))
    finally reasoner.dispose()
    assertCorrectProof(o, subClassOf("A", "C2"))
  }

  @Test def unsatisfiableClass(): Unit = assertCorrectProof(ontology("QDiff",
    Seq("C0" -> ":x = 0", "C1" -> ":x + 1 = :y", "C2" -> ":y > 2"), "SubClassOf(:A ObjectIntersectionOf(:C0 :C1 :C2))"),
    sub(cls("A"), factory.getOWLNothing))

  @Test def inconsistentOntology(): Unit = assertCorrectProof(ontology("QDiff",
    Seq("C0" -> ":x = 0", "C1" -> ":x > 1"), "ClassAssertion(:C0 :a)", "ClassAssertion(:C1 :a)"),
    sub(factory.getOWLThing, factory.getOWLNothing))

  /** the conjunction does not occur in the ontology */
  @Test def expressionNotInOntology(): Unit = assertCorrectProof(ontology("QDiff",
    Seq("C0" -> ":x + 1 = :y", "C1" -> ":x = 0", "C2" -> ":y = 1")), sub(and("C0", "C1"), cls("C2")))

  @Test def ontologyIsNotChanged(): Unit = {
    val o = load("qdiff/patient.ofn")
    val axioms = o.getAxioms()
    val signature = o.getSignature()
    proof(o, factory.getOWLSubClassOfAxiom(owlClass(o, "CurrentPatient"), owlClass(o, "NeedAttention")))
    assertEquals("the axioms should not be changed", axioms, o.getAxioms())
    assertEquals("the signature should not be changed", signature, o.getSignature())
  }

  /** a reasoner for the ontology is used and stays usable; for another ontology, the generator creates its own */
  @Test def reusesTheGivenReasoner(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 5", "C1" -> ":x > 3"), "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o).asInstanceOf[ELKCDReasoner[CDConstraint]]
    try {
      val generator = new ELKCDProofGenerator(reasoner)
      generator.setOntology(extended(o))
      generator.getProof(ExtendedAxiom[CDConstraint](subClassOf("A", "C1"), Map()))
      assertTrue("the given reasoner should be used", reasoner.concreteDomainAxioms.contains(subClassOf("C0", "C1")))
      generator.setOntology(extended(ontology("QDiff", Seq("C0" -> ":x = 5"))))
      generator.dispose()
      assertTrue("the given reasoner should not be disposed", reasoner.isEntailed(subClassOf("A", "C1")))
    } finally reasoner.dispose()
  }

  /** a reasoner set later is used from the next setOntology on; without it, the generator uses its own */
  @Test def setsTheReasonerLater(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 5", "C1" -> ":x > 3"), "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o).asInstanceOf[ELKCDReasoner[CDConstraint]]
    val generator = new ELKCDProofGenerator()
    try {
      generator.setReasoner(reasoner)
      generator.setOntology(extended(o))
      generator.getProof(ExtendedAxiom[CDConstraint](subClassOf("A", "C1"), Map()))
      assertTrue("the reasoner should be used", reasoner.concreteDomainAxioms.contains(subClassOf("C0", "C1")))
      generator.setReasoner(null)
      generator.setOntology(extended(o))
      generator.getProof(ExtendedAxiom[CDConstraint](subClassOf("A", "C1"), Map()))
      generator.dispose()
      assertTrue("the reasoner should not be disposed", reasoner.isEntailed(subClassOf("A", "C1")))
    } finally reasoner.dispose()
  }

  private def subClassOf(sub: String, sup: String) = factory.getOWLSubClassOfAxiom(cls(sub), cls(sup))
}
