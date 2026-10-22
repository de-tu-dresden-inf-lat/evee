package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport._
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.{cls, ontology}
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import org.junit.Assert.{assertFalse, assertThrows, assertTrue}
import org.junit.{Ignore, Test}
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException

/** The concrete domain reasoner behaves like an OWLReasoner, e.g., inside Protégé. */
class ELKCDReasonerTest {

  private val factory = OWLManager.getOWLDataFactory

  /** C0 and C1 have contradicting constraints */
  private val contradicting = Seq("C0" -> ":x = 0", "C1" -> ":x > 1")

  @Ignore("B4")
  @Test def inconsistentOntology(): Unit = withReasoner(ontology("QDiff", contradicting,
    "ClassAssertion(:C0 :a)", "ClassAssertion(:C1 :a)")) { reasoner =>
    assertFalse("the ontology should be inconsistent", reasoner.isConsistent)
    assertThrows(classOf[InconsistentOntologyException], () => reasoner.getSuperClasses(cls("C0"), false))
  }

  @Ignore("B5")
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

  @Ignore("B5")
  @Test def nonBufferingReasonerAppliesChangesImmediately(): Unit = {
    val o = ontology("QDiff", contradicting, "SubClassOf(:A :C0)")
    val reasoner = new OWLCDReasonerFactory().createNonBufferingReasoner(o)
    try {
      assertTrue(reasoner.isSatisfiable(cls("A")))
      o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C1"))
      assertFalse("the change should be visible immediately", reasoner.isSatisfiable(cls("A")))
    } finally reasoner.dispose()
  }

  @Ignore("B10")
  @Test def disposedReasonerStopsListening(): Unit = {
    val o = ontology("QDiff", contradicting)
    val reasoner = new OWLCDReasonerFactory().createReasoner(o)
    reasoner.dispose()
    o.getOWLOntologyManager.addAxiom(o, subClassOf("A", "C1"))
    assertTrue("a disposed reasoner should not record changes", reasoner.getPendingChanges.isEmpty)
  }

  @Ignore("B6")
  @Test def importedAxiomsAreUsed(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    ontology(manager, "http://ex.org/imported", null, Seq(), "SubClassOf(:A :C1)")
    val o = ontology(manager, "http://ex.org/o", "QDiff", contradicting,
      "Import(<http://ex.org/imported>)", "SubClassOf(:A :C0)")
    assertUnsatisfiable(o, "A")
  }

  @Ignore("B7")
  @Test def unsatisfiableConjunctionNotInOntology(): Unit = withReasoner(ontology("QDiff", contradicting)) {
    reasoner => assertFalse("C0 and C1 should be unsatisfiable", reasoner.isSatisfiable(and("C0", "C1")))
  }

  @Ignore("B7")
  @Test def subsumptionOfConjunctionNotInOntology(): Unit = withReasoner(ontology("QDiff",
    Seq("C0" -> ":x + 1 = :y", "C1" -> ":x = 0", "C2" -> ":y = 1"))) { reasoner =>
    assertTrue("C0 and C1 should be subsumed by C2",
      reasoner.isEntailed(factory.getOWLSubClassOfAxiom(and("C0", "C1"), cls("C2"))))
  }
}
