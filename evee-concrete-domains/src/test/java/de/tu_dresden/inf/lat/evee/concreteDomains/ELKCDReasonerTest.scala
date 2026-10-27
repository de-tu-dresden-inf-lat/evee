package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport._
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, ontology}
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import org.junit.Assert.{assertEquals, assertFalse, assertThrows, assertTrue, fail}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.reasoner.{InconsistentOntologyException, InferenceType}

/** The concrete domain reasoner behaves like an OWLReasoner, e.g., inside Protégé. */
class ELKCDReasonerTest {

  private val factory = OWLManager.getOWLDataFactory

  /** C0 and C1 have contradicting constraints */
  private val contradicting = Seq("C0" -> ":x = 0", "C1" -> ":x > 1")

  @Test def inconsistentOntology(): Unit = withReasoner(ontology("QDiff", contradicting,
    "ClassAssertion(:C0 :a)", "ClassAssertion(:C1 :a)")) { reasoner =>
    assertFalse("the ontology should be inconsistent", reasoner.isConsistent)
    assertThrows(classOf[InconsistentOntologyException], () => reasoner.getSuperClasses(cls("C0"), false))
    // Protégé classifies with precomputeInferences and reports an inconsistent ontology if it gets this exception
    assertThrows(classOf[InconsistentOntologyException], () => reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY))
  }

  @Test def inconsistencyCanBeRemoved(): Unit = {
    val o = ontology("QDiff", contradicting, "ClassAssertion(:C0 :a)", "ClassAssertion(:C1 :a)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try {
      assertFalse("the ontology should be inconsistent", reasoner.isConsistent)
      val a = factory.getOWLNamedIndividual(IRI.create(TestOntologies.NS + "a"))
      o.getOWLOntologyManager.removeAxiom(o, factory.getOWLClassAssertionAxiom(cls("C1"), a))
      reasoner.flush()
      assertTrue("the ontology should be consistent again", reasoner.isConsistent)
      assertTrue(reasoner.getTypes(a, false).containsEntity(cls("C0")))
    } finally reasoner.dispose()
  }

  /** def(hr) only holds where hr has a value, e.g. because of hr = 83 (B11) */
  private def assertDefinedOnlyWithValue(domain: String): Unit = withReasoner(ontology(domain,
    Seq("HasValue" -> "def(:hr)", "Hr83" -> ":hr = 83"), "SubClassOf(:P :Hr83)", "Declaration(Class(:Q))")) {
    reasoner =>
      assertTrue("P has a value for hr", reasoner.isEntailed(subClassOf("P", "HasValue")))
      assertFalse("Q need not have a value for hr", reasoner.isEntailed(subClassOf("Q", "HasValue")))
      assertFalse("not everything has a value for hr",
        reasoner.isEntailed(factory.getOWLSubClassOfAxiom(factory.getOWLThing, cls("HasValue"))))
  }

  @Test def definedValueDiff(): Unit = assertDefinedOnlyWithValue("QDiff")
  @Test def definedValueMult(): Unit = assertDefinedOnlyWithValue("QMult")
  @Test def definedValueLinear(): Unit = assertDefinedOnlyWithValue("QLinear")

  @Test def bufferingReasonerAppliesChangesOnFlush(): Unit = {
    val o = ontology("QDiff", contradicting, "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try {
      assertTrue(reasoner.isSatisfiable(cls("A")))
      o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C1"))
      assertFalse("the change should be pending", reasoner.getPendingChanges.isEmpty)
      assertTrue("the change should not be visible before flush", reasoner.isSatisfiable(cls("A")))
      reasoner.flush()
      assertTrue("no change should be pending after flush", reasoner.getPendingChanges.isEmpty)
      assertFalse("the change should be visible after flush", reasoner.isSatisfiable(cls("A")))
    } finally reasoner.dispose()
  }

  @Test def nonBufferingReasonerAppliesChangesImmediately(): Unit = {
    val o = ontology("QDiff", contradicting, "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createNonBufferingReasoner(o)
    try {
      assertTrue(reasoner.isSatisfiable(cls("A")))
      o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C1"))
      assertFalse("the change should be visible immediately", reasoner.isSatisfiable(cls("A")))
    } finally reasoner.dispose()
  }

  @Test def disposedReasonerStopsListening(): Unit = {
    val o = ontology("QDiff", contradicting)
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    reasoner.dispose()
    o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C1"))
    assertTrue("a disposed reasoner should not record changes", reasoner.getPendingChanges.isEmpty)
  }

  @Test def importedAxiomsAreUsed(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    ontology(manager, "http://ex.org/imported", null, Seq(), "SubClassOf(:A :C1)")
    val o = ontology(manager, "http://ex.org/o", "QDiff", contradicting,
      "Import(<http://ex.org/imported>)", "SubClassOf(:A :C0)")
    assertUnsatisfiable(o, "A")
  }

  @Test def unsatisfiableConjunctionNotInOntology(): Unit = withReasoner(ontology("QDiff", contradicting)) {
    reasoner => assertFalse("C0 and C1 should be unsatisfiable", reasoner.isSatisfiable(and("C0", "C1")))
  }

  @Test def subsumptionOfConjunctionNotInOntology(): Unit = withReasoner(ontology("QDiff",
    Seq("C0" -> ":x + 1 = :y", "C1" -> ":x = 0", "C2" -> ":y = 1"))) { reasoner =>
    assertTrue("C0 and C1 should be subsumed by C2",
      reasoner.isEntailed(factory.getOWLSubClassOfAxiom(and("C0", "C1"), cls("C2"))))
  }

  private val r = factory.getOWLObjectProperty(IRI.create(TestOntologies.NS + "r"))

  @Test def unsatisfiableNestedExpressionNotInOntology(): Unit = withReasoner(ontology("QDiff", contradicting)) {
    reasoner => assertFalse("r some (C0 and C1) should be unsatisfiable",
      reasoner.isSatisfiable(factory.getOWLObjectSomeValuesFrom(r, and("C0", "C1"))))
  }

  /** the subsumption needs C0 and C1 ⊑ C2 from the concrete domain, then the axiom about r some C2 */
  @Test def nestedSubsumptionNotInOntology(): Unit = withReasoner(ontology("QDiff",
    Seq("C0" -> ":x + 1 = :y", "C1" -> ":x = 0", "C2" -> ":y = 1"),
    "SubClassOf(ObjectSomeValuesFrom(:r :C2) :D)")) { reasoner =>
    assertTrue("r some (C0 and C1) should be subsumed by D", reasoner.isEntailed(
      factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(r, and("C0", "C1")), cls("D"))))
  }

  /**
   * C1 is a direct superclass of A and B and C0, which occurs only nested in the ontology. ELK 0.6.0 omits the direct
   * superclasses of complex class expressions when asked for all superclasses.
   */
  @Test def directSuperclassOfNestedExpressionIsUsed(): Unit = assertUnsatisfiable(ontology("QDiff", contradicting,
    "SubClassOf(ObjectIntersectionOf(:A :B) :C1)",
    "SubClassOf(:E ObjectSomeValuesFrom(:r ObjectIntersectionOf(:A :B :C0)))"), "E")

  @Test def superclassesOfExpressionNotInOntology(): Unit = withReasoner(ontology("QDiff",
    Seq("C0" -> ":x = 0", "C1" -> ":x > -1"), "SubClassOf(ObjectIntersectionOf(:A :C1) :D)")) { reasoner =>
    val superClasses = reasoner.getSuperClasses(and("A", "C0"), false)
    for (name <- Seq("A", "C0", "C1", "D"))
      assertTrue(name + " should be a superclass of A and C0", superClasses.containsEntity(cls(name)))
  }

  /** C0 and C1 occur only in their constraint annotations, not in any logical axiom */
  @Test def constraintClassesAreClassified(): Unit = withReasoner(ontology("QMult",
    Seq("C0" -> ":x = 5", "C1" -> ":x < 6"))) { reasoner =>
    assertTrue("C1 should be a superclass of C0", reasoner.getSuperClasses(cls("C0"), false).containsEntity(cls("C1")))
  }

  @Test def constraintsOfImportedOntologiesAreUsed(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    ontology(manager, "http://ex.org/imported", null, Seq("C1" -> ":x > 1"))
    val o = ontology(manager, "http://ex.org/o", "QDiff", Seq("C0" -> ":x = 0"),
      "Import(<http://ex.org/imported>)", "SubClassOf(:A :C0)", "SubClassOf(:A :C1)")
    assertUnsatisfiable(o, "A")
  }

  @Test def changesOfImportedOntologiesArePending(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    val imported = ontology(manager, "http://ex.org/imported", null, Seq())
    val o = ontology(manager, "http://ex.org/o", "QDiff", contradicting, "Import(<http://ex.org/imported>)",
      "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try {
      manager.addAxiom(imported, subClassOf("A", "C1"))
      assertEquals(1, reasoner.getPendingChanges.size)
      reasoner.flush()
      assertFalse(reasoner.isSatisfiable(cls("A")))
    } finally reasoner.dispose()
  }

  @Test def failedFlushKeepsTheReasonerWorking(): Unit = {
    val o = ontology("QDiff", contradicting, "SubClassOf(:A :C0)")
    val manager = o.getOWLOntologyManager
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try {
      assertTrue(reasoner.isSatisfiable(cls("A")))
      val wrong = factory.getOWLAnnotationAssertionAxiom(
        factory.getOWLAnnotationProperty(ExtendedOntologyParser.CONSTRAINT_ANNOTATION_PROP_IRI),
        cls("C1").getIRI, factory.getOWLLiteral(":x = five"))
      manager.addAxiom(o, wrong)
      try {
        reasoner.flush()
        fail("the wrong constraint should be reported")
      } catch {
        case e: ParsingException => assertTrue(e.getMessage, e.getMessage.contains(":x = five"))
      }
      assertTrue("the reasoner should still answer", reasoner.isSatisfiable(cls("A")))
      manager.removeAxiom(o, wrong)
      manager.addAxiom(o, subClassOf("A", "C1"))
      reasoner.flush()
      assertFalse("the corrected ontology should be used", reasoner.isSatisfiable(cls("A")))
    } finally reasoner.dispose()
  }

  private def concreteDomainAxioms(reasoner: org.semanticweb.owlapi.reasoner.OWLReasoner) =
    reasoner.asInstanceOf[ELKCDReasoner[_]].concreteDomainAxioms

  @Test def addedAxiomsAreRecorded(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 0", "C1" -> ":x > -1", "C2" -> ":x > 1"), "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    try {
      assertTrue(reasoner.isEntailed(subClassOf("A", "C1")))
      assertEquals(Set(subClassOf("C0", "C1"), subClassOf("C2", "C1")), concreteDomainAxioms(reasoner))
      // a query on an expression that is not in the ontology adds C0 ⊓ C1 ⊓ C2 ⊑ ⊥ (C1 is implied by C0)
      assertFalse(reasoner.isSatisfiable(and("C0", "C2")))
      assertTrue(concreteDomainAxioms(reasoner).contains(
        factory.getOWLSubClassOfAxiom(and("C0", "C1", "C2"), factory.getOWLNothing)))
      // the recorded axioms belong to the current internal ontology
      o.getOWLOntologyManager.removeAxiom(o, subClassOf("A", "C0"))
      o.getOWLOntologyManager.addAxiom(o, factory.getOWLDeclarationAxiom(cls("C3")))
      reasoner.flush()
      assertFalse(reasoner.isEntailed(subClassOf("A", "C1")))
      assertEquals(Set(subClassOf("C0", "C1"), subClassOf("C2", "C1")), concreteDomainAxioms(reasoner))
    } finally reasoner.dispose()
  }

  @Test def assertedAxiomsAreNotRecorded(): Unit = withReasoner(ontology("QMult",
    Seq("C0" -> ":x = 5", "C1" -> ":x < 6"), "SubClassOf(:C0 :C1)")) { reasoner =>
    assertTrue(reasoner.isEntailed(subClassOf("C0", "C1")))
    assertTrue("C0 ⊑ C1 is asserted, not added", concreteDomainAxioms(reasoner).isEmpty)
  }

  @Test def ontologyIsNotChanged(): Unit = {
    val o = ontology("QDiff", contradicting, "SubClassOf(:A :C0)", "SubClassOf(:A :C1)")
    val axioms = o.getAxioms()
    withReasoner(o) { reasoner =>
      assertFalse(reasoner.isSatisfiable(cls("A")))
      assertFalse(reasoner.isSatisfiable(factory.getOWLObjectSomeValuesFrom(r, and("C0", "C1"))))
      assertEquals("the ontology should not be changed by reasoning", axioms, o.getAxioms())
    }
  }
}
