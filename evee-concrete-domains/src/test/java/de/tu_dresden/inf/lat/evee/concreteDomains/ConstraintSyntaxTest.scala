package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.ReasonerTestSupport._
import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.ontology
import org.junit.Assert.{assertTrue, fail}
import org.junit.{Ignore, Test}

/** Constraints written in the cd:constraint annotations are read correctly. */
class ConstraintSyntaxTest {

  @Ignore("B1")
  @Test def linearConstraint(): Unit = assertEntailed(ontology("QLinear",
    Seq("C0" -> "1*:x+1*:y=5", "C1" -> "1*:x=2", "C2" -> "1*:y=3"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B1, B9")
  @Test def linearConstraintWithSpaces(): Unit = assertEntailed(ontology("QLinear",
    Seq("C0" -> "2 * :x + 3 * :y = 5", "C1" -> "1 * :x = 1", "C2" -> "1 * :y = 1"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B1, B9")
  @Test def linearConstraintWithoutCoefficients(): Unit = assertEntailed(ontology("QLinear",
    Seq("C0" -> ":x + :y = 5", "C1" -> ":x = 2", "C2" -> "-:y = -3"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B1, B9")
  @Test def linearConstraintWithHyphenatedIRI(): Unit = assertEntailed(ontology("QLinear",
    Seq("C0" -> "<http://ex.org/my-onto#x> - :y = 0", "C1" -> "<http://ex.org/my-onto#x> = 4", "C2" -> ":y = 4"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B9")
  @Test def diffConstraintWithMinus(): Unit = assertEntailed(ontology("QDiff",
    Seq("C0" -> ":x - 3 = :y", "C1" -> ":x = 5", "C2" -> ":y = 2"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B2")
  @Test def multEquality(): Unit = assertEntailed(ontology("QMult",
    Seq("C0" -> ":x = 5", "C1" -> ":x < 6"),
    "SubClassOf(:A :C0)"), "A", "C1")

  @Test def multProduct(): Unit = assertEntailed(ontology("QMult",
    Seq("C0" -> ":x * 2 = :y", "C1" -> ":y < 6", "C2" -> ":x < 3"),
    "SubClassOf(:A :C0)", "SubClassOf(:A :C1)"), "A", "C2")

  @Ignore("B8")
  @Test def twoConstraintsOnOneClassAreRejected(): Unit = {
    val o = ontology("QDiff", Seq("C0" -> ":x = 0", "C0" -> ":x > 1"))
    try {
      withReasoner(o)(_.isConsistent)
      fail("two constraints on the same class should be rejected")
    } catch {
      case e: Exception =>
        assertTrue("the error should name the class: " + messages(e), messages(e).exists(_.contains("C0")))
    }
  }
}
