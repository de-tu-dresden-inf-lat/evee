package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport._
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, ontology}
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import org.junit.Assert.{assertEquals, assertFalse, assertNotEquals, assertThrows, assertTrue, fail}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName
import org.semanticweb.owlapi.model.{AddOntologyAnnotation, IRI, OWLAnnotationValue, OWLOntology}
import org.semanticweb.owlapi.reasoner.{InconsistentOntologyException, InferenceType, Node, NodeSet, OWLReasoner}
import org.semanticweb.owlapi.util.Version

import scala.util.{Failure, Success, Try}

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

  /**
   * B12: there is no positive factor between the values 2 and 0, which made the reasoner throw an exception (the
   * subsumption holds whether or not QMult allows the value 0)
   */
  @Test def multZeroValue(): Unit = withReasoner(ontology("QMult",
    Seq("C0" -> ":x = 2", "C1" -> ":y = 0", "C2" -> ":y < 1"), "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))")) {
    reasoner => assertTrue("A should be subsumed by C2", reasoner.isEntailed(subClassOf("A", "C2")))
  }

  /** QMult is over all rational numbers: 4 * x = x is satisfiable and gives x = 0, which contradicts x < 0 */
  @Test def multFactorForZero(): Unit = withReasoner(ontology("QMult",
    Seq("C0" -> "4 * :x = :x", "C1" -> ":x = 0", "C2" -> ":x < 0"),
    "SubClassOf(:A :C0)", "SubClassOf(:B ObjectIntersectionOf(:C0 :C2))")) { reasoner =>
    assertTrue("C0 should be satisfiable", reasoner.isSatisfiable(cls("C0")))
    assertTrue("A should be subsumed by C1", reasoner.isEntailed(subClassOf("A", "C1")))
    assertFalse("B should be unsatisfiable", reasoner.isSatisfiable(cls("B")))
  }

  /** the version comes from reasoner.properties, which Maven fills in; 0.0.0.0 means it was not found or not filled in */
  @Test def version(): Unit = withReasoner(ontology("QDiff", contradicting.take(1))) { reasoner =>
    assertNotEquals(new Version(0, 0, 0, 0), reasoner.getReasonerVersion)
  }

  /** the other reasoning methods give ELK's answers, or throw ELK's exception where ELK does not support them */
  @Test def otherMethodsAsElk(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 0"), "SubClassOf(:A :C0)", "SubObjectPropertyOf(:r :s)",
      "ObjectPropertyDomain(:r :A)", "ClassAssertion(:A :a)", "ObjectPropertyAssertion(:r :a :b)")
    val x = factory.getOWLDataProperty(IRI.create(TestOntologies.NS + "x"))
    val a = factory.getOWLNamedIndividual(IRI.create(TestOntologies.NS + "a"))
    val calls: Seq[(String, OWLReasoner => Any)] = Seq(
      "getDisjointClasses" -> (_.getDisjointClasses(cls("A"))),
      "getDisjointObjectProperties" -> (_.getDisjointObjectProperties(r)),
      "getInverseObjectProperties" -> (_.getInverseObjectProperties(r)),
      "getObjectPropertyDomains" -> (_.getObjectPropertyDomains(r, false)),
      "getObjectPropertyRanges" -> (_.getObjectPropertyRanges(r, false)),
      "getTopDataPropertyNode" -> (_.getTopDataPropertyNode()),
      "getBottomDataPropertyNode" -> (_.getBottomDataPropertyNode()),
      "getSubDataProperties" -> (_.getSubDataProperties(x, false)),
      "getSuperDataProperties" -> (_.getSuperDataProperties(x, false)),
      "getEquivalentDataProperties" -> (_.getEquivalentDataProperties(x)),
      "getDisjointDataProperties" -> (_.getDisjointDataProperties(x)),
      "getDataPropertyDomains" -> (_.getDataPropertyDomains(x, false)),
      "getObjectPropertyValues" -> (_.getObjectPropertyValues(a, r)),
      "getDataPropertyValues" -> (_.getDataPropertyValues(a, x)),
      "getDifferentIndividuals" -> (_.getDifferentIndividuals(a)))
    def result(value: Any): Any = value match {
      case nodes: NodeSet[_] => nodes.getFlattened
      case node: Node[_] => node.getEntities
      case other => other
    }
    withReasoner(o) { pacer =>
      withElk(o) { elk =>
        for ((name, call) <- calls)
          (Try(result(call(elk))), Try(result(call(pacer)))) match {
            case (Success(expected), Success(actual)) => assertEquals(name, expected, actual)
            case (Failure(expected), Failure(actual)) => assertEquals(name, expected.getClass, actual.getClass)
            case (expected, actual) => fail(s"$name: ELK gives $expected, PACER $actual")
          }
      }
    }
  }

  /** the message of the error that the factory reports instead of creating a reasoner */
  private def creationError(o: OWLOntology): String =
    try {
      new OWLCDReasonerFactory().createReasoner(o).dispose()
      fail("the reasoner should not be created")
      ""
    } catch {
      case e: ParsingException => e.getMessage
    }

  private def addConcreteDomain(o: OWLOntology, value: OWLAnnotationValue): Unit =
    o.getOWLOntologyManager.applyChange(new AddOntologyAnnotation(o, factory.getOWLAnnotation(
      factory.getOWLAnnotationProperty(ExtendedOntologyParser.CONCRETE_DOMAIN_ANNOTATION_PROP_IRI), value)))

  /** a wrong constraint is reported as it is, naming the constraint and its class */
  @Test def wrongConstraintAtCreation(): Unit = {
    val message = creationError(ontology("QDiff", Seq("C0" -> ":x = five")))
    assertTrue(message, message.contains(":x = five") && message.contains("C0"))
  }

  /** a wrong concrete domain is reported, instead of reasoning like ELK */
  @Test def wrongConcreteDomain(): Unit = {
    val unknown = creationError(ontology("QDif", Seq()))
    assertTrue(unknown, unknown.contains("\"QDif\"") && unknown.contains(ConcreteDomainName.NAMES))
    val notString = ontology(null, Seq())
    addConcreteDomain(notString, IRI.create(TestOntologies.NS + "QDiff"))
    assertTrue(creationError(notString), creationError(notString).contains("has to be a string"))
    val two = ontology("QDiff", Seq())
    addConcreteDomain(two, factory.getOWLLiteral("QMult"))
    assertTrue(creationError(two), creationError(two).contains("only one concrete domain"))
  }

  /** without an annotation cd:concreteDomain, PACER reasons like ELK */
  @Test def noConcreteDomain(): Unit =
    withReasoner(ontology(null, Seq(), "SubClassOf(:A :B)", "SubClassOf(:B :C)")) { reasoner =>
      assertTrue(reasoner.isEntailed(subClassOf("A", "C")))
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
