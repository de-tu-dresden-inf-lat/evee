package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport.assertSatisfiable
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.ontology
import org.junit.{Ignore, Test}

/** Decimal numbers are treated exactly: each of these constraint sets has a solution. */
class ExactArithmeticTest {

  @Ignore("B3")
  @Test def diff(): Unit = assertSatisfiable(ontology("QDiff",
    Seq("C0" -> ":x = 0", "C1" -> ":x + 0.1 = :y", "C2" -> ":y + 0.2 = :z", "C3" -> ":z = 0.3"),
    "SubClassOf(:A ObjectIntersectionOf(:C0 :C1 :C2 :C3))"), "A")

  @Ignore("B2, B3")
  @Test def mult(): Unit = assertSatisfiable(ontology("QMult",
    Seq("C0" -> ":x = 0.1", "C1" -> ":x * 3 = :y", "C2" -> ":y = 0.3"),
    "SubClassOf(:A ObjectIntersectionOf(:C0 :C1 :C2))"), "A")

  @Ignore("B1, B3")
  @Test def linear(): Unit = assertSatisfiable(ontology("QLinear",
    Seq("C0" -> "0.1*:x=1", "C1" -> "1*:x=10"),
    "SubClassOf(:A ObjectIntersectionOf(:C0 :C1))"), "A")
}
