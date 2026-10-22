package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDReasonerFactory
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.semanticweb.elk.owlapi.ElkReasonerFactory

/**
 * Subsumptions from the test suite of the original prototype. Each of them depends on concrete domain reasoning,
 * i.e., it is not entailed by ELK alone.
 */
class ExampleOntologiesTest {

  private def assertEntailedOnlyWithConcreteDomain(path: String, sub: String, sup: String): Unit = {
    val ontology = TestOntologies.load(path)
    val factory = ontology.getOWLOntologyManager.getOWLDataFactory
    val axiom = factory.getOWLSubClassOfAxiom(TestOntologies.owlClass(ontology, sub), TestOntologies.owlClass(ontology, sup))

    val elk = new ElkReasonerFactory().createReasoner(ontology)
    try assertFalse(s"$axiom should not follow without the concrete domain", elk.isEntailed(axiom))
    finally elk.dispose()

    val reasoner = new OWLCDReasonerFactory().createReasoner(ontology)
    try assertTrue(s"$axiom should be entailed", reasoner.isEntailed(axiom))
    finally reasoner.dispose()
  }

  @Test def diffPatient(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/patient.ofn", "CurrentPatient", "NeedAttention")

  @Test def diffExample(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/example-cd2.ofn", "Drone1", "SmallLargeDrone")

  @Test def diffToyDrone(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/toyDrone.ofn", "Drone", "Cat")

  @Test def diffToyDrone2(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/toyDrone2.ofn", "Drone", "Hexicopter")

  @Test def diffDrones1a(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/ddist1a.ofn", "World", "PartiallySafe")

  @Test def diffDrones13c(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/ddist13c.ofn", "World", "PartiallySafe")

  @Test def diffDrones13cUnique(): Unit = assertEntailedOnlyWithConcreteDomain("qdiff/ddist13c-unique.ofn", "World", "PartiallySafe")
}
