package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.TestOntologies.ontology
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser.LinearConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser.MultConstraintParser
import org.apache.commons.math3.fraction.BigFraction
import org.junit.Assert.{assertEquals, assertTrue, fail}
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.KRSS2DocumentFormat
import org.semanticweb.owlapi.model.{IRI, OWLDataProperty}

/** The constraint parsers read every supported syntax and give helpful error messages. */
class ConstraintParserTest {

  /** an ontology with the default prefix ":" for TestOntologies.NS and the prefix "cd:" */
  private val o = ontology("QDiff", Seq())
  private val diff = new DiffConstraintParser(o)
  private val mult = new MultConstraintParser(o)
  private val linear = new LinearConstraintParser(o)

  private def property(iri: String): OWLDataProperty = OWLManager.getOWLDataFactory.getOWLDataProperty(IRI.create(iri))
  private val (x, y) = (property(TestOntologies.NS + "x"), property(TestOntologies.NS + "y"))

  private def fraction(numerator: Int, denominator: Int = 1) = new BigFraction(numerator, denominator)

  @Test def diffConstraints(): Unit = {
    assertEquals(DiffEqual(x, fraction(5)), diff.toConstraint("x = 5"))
    assertEquals(DiffGreaterThan(x, fraction(-5, 2)), diff.toConstraint(":x > -2.5"))
    assertEquals(DiffSum(x, fraction(3), y), diff.toConstraint(":x + 3 = :y"))
    assertEquals(DiffSum(x, fraction(-3), y), diff.toConstraint(":x - 3 = :y"))
    assertEquals(DiffSum(x, fraction(-3), y), diff.toConstraint(":x+-3=:y"))
    assertEquals(DiffSum(property("http://ex.org/my-onto#x"), fraction(1, 3), property("http://tu-dresden.de/inf/lat/evee/cd#y")),
      diff.toConstraint("<http://ex.org/my-onto#x> + 1/3 = cd:y"))
  }

  @Test def multConstraints(): Unit = {
    assertEquals(MultEqual(x, fraction(5)), mult.toConstraint(":x = 5"))
    assertEquals(MultLessThan(x, fraction(1, 10)), mult.toConstraint(":x < 0.1"))
    assertEquals(Multiplication(x, fraction(2), y), mult.toConstraint(":x * 2 = :y"))
    assertEquals(Multiplication(x, fraction(2), y), mult.toConstraint("2 * :x = :y"))
  }

  @Test def linearConstraints(): Unit = {
    def equation(coefficients: (OWLDataProperty, BigFraction)*)(constant: BigFraction) =
      LinearConstraint[BigFraction](coefficients.toMap, constant)
    assertEquals(equation(x -> fraction(2), y -> fraction(3))(fraction(5)), linear.toConstraint("2 * :x + 3 * :y = 5"))
    assertEquals(equation(x -> fraction(2), y -> fraction(-1))(fraction(5)), linear.toConstraint("2*:x+-1*:y=5"))
    assertEquals(equation(x -> fraction(1), y -> fraction(-1))(fraction(0)), linear.toConstraint(":x - :y = 0"))
    assertEquals(equation(x -> fraction(-1))(fraction(-3)), linear.toConstraint("-:x = -3"))
    assertEquals(equation(x -> fraction(2))(fraction(4)), linear.toConstraint("x + x = 4"))
    assertEquals(equation(x -> fraction(1, 2), y -> fraction(-1))(fraction(1)), linear.toConstraint("0.5 * :x − :y = 1"))
  }

  /** the constraints can be read from their own string representation */
  @Test def roundTrip(): Unit = {
    val diffs = Seq(DiffEqual(x, fraction(1, 3)), DiffGreaterThan(x, fraction(-2)), DiffSum(x, fraction(-5, 2), y))
    diffs.foreach(c => assertEquals(c, diff.toConstraint(c.toString)))
    val mults = Seq(MultEqual(x, fraction(7, 2)), MultLessThan(x, fraction(1, 3)), Multiplication(x, fraction(2, 3), y))
    mults.foreach(c => assertEquals(c, mult.toConstraint(c.toString)))
    val linears = Seq(LinearConstraint[BigFraction](Map(x -> fraction(1, 3), y -> fraction(-1)), fraction(5, 2)))
    linears.foreach(c => assertEquals(c, linear.toConstraint(c.toString)))
  }

  private def assertError(parser: ConstraintParser[_], constraint: String, expected: String): Unit =
    try {
      parser.toConstraint(constraint)
      fail(s"$constraint should not be readable")
    } catch {
      case e: ParsingException => assertTrue(e.getMessage, e.getMessage.contains(expected))
    }

  @Test def errors(): Unit = {
    assertError(diff, ":x =", "expected a number, but the constraint ends")
    assertError(diff, ":x = 5 6", "expected the end of the constraint, but found '6' at position 8")
    assertError(diff, ":x < 5", "expected '=', '>', '+' or '-', but found '<'")
    assertError(diff, ":x = 5 #", "unexpected '#' at position 8")
    assertError(diff, "ex:x = 1", "the prefix ex: of ex:x is not defined")
    assertError(mult, "0 * :x = :y", "the factor 0 has to be positive")
    assertError(linear, "2 :x = 1", "expected '*', but found ':x'")
    assertError(linear, ":x + 1 = 2", "expected '*', but found '='")
  }

  @Test def ontologyWithoutPrefixes(): Unit = {
    val withoutPrefixes = ontology("QDiff", Seq())
    withoutPrefixes.getOWLOntologyManager.setOntologyFormat(withoutPrefixes, new KRSS2DocumentFormat)
    val parser = new DiffConstraintParser(withoutPrefixes)
    assertEquals(DiffEqual(x, fraction(1)), parser.toConstraint("<" + TestOntologies.NS + "x> = 1"))
    assertError(parser, ":x = 1", "cannot resolve :x, since the ontology defines no prefixes")
  }

  @Test def constraintMustBeAString(): Unit = {
    val wrong = ontology("QDiff", Seq(), "AnnotationAssertion(cd:constraint :C0 :value)")
    try {
      ExtendedOntologyParser.toExtendedOntology(wrong)
      fail("a constraint that is not a string should be rejected")
    } catch {
      case e: ParsingException => assertTrue(e.getMessage, e.getMessage.contains("C0 has to be a string"))
    }
  }

  @Test def errorNamesTheClass(): Unit = {
    val wrong = ontology("QDiff", Seq("C0" -> ":x = five"))
    try {
      ExtendedOntologyParser.toExtendedOntology(wrong)
      fail("the constraint should not be readable")
    } catch {
      case e: ParsingException =>
        assertTrue(e.getMessage, e.getMessage.contains("\":x = five\" of http://ex.org/o#C0"))
    }
  }
}
