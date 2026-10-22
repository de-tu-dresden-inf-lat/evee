package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.assertEntailedOnlyWithConcreteDomain
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.load
import org.junit.{Ignore, Test}

/**
 * Subsumptions from the test suite of the original prototype. Each of them depends on concrete domain reasoning,
 * i.e., it is not entailed by ELK alone.
 */
class ExampleOntologiesTest {

  @Test def diffPatient(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/patient.ofn"), "CurrentPatient", "NeedAttention")

  @Test def diffExample(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/example-cd2.ofn"), "Drone1", "SmallLargeDrone")

  @Test def diffToyDrone(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/toyDrone.ofn"), "Drone", "Cat")

  @Test def diffToyDrone2(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/toyDrone2.ofn"), "Drone", "Hexicopter")

  @Test def diffDrones1a(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/ddist1a.ofn"), "World", "PartiallySafe")

  @Test def diffDrones13c(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/ddist13c.ofn"), "World", "PartiallySafe")

  @Test def diffDrones13cUnique(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qdiff/ddist13c-unique.ofn"), "World", "PartiallySafe")

  @Ignore("B1, B9")
  @Test def linearCoffee(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/coffee.ofn"), "Order", "Ristretto")

  @Ignore("B1, B9")
  @Test def linearPatient(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/patient.ofn"), "CurrentPatient", "NeedAttention")

  @Ignore("B1, B9")
  @Test def linearExample(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/example.ofn"), "System1", "GoodSystem")

  @Ignore("B1, B9")
  @Test def linearExample2(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/example2.ofn"), "A1", "B3")

  @Ignore("B1, B9")
  @Test def linearArtificial3a(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/artificial3a.ofn"), "A", "B")

  @Ignore("B1, B9")
  @Test def linearArtificial3aUnique(): Unit =
    assertEntailedOnlyWithConcreteDomain(load("qlinear/artificial3a-unique.ofn"), "A", "B")
}
