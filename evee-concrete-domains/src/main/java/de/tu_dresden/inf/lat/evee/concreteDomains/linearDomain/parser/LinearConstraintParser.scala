package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, ConstraintTokens}
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLOntology}

/**
 * Parser for the concrete domain QLinear. A constraint is a linear equation a1 * x1 + ... + an * xn = c, where the
 * xi are distinct data properties and the ai are non-zero numbers, e.g., 2 * x - y = 0.5. A coefficient 1 can be
 * left out. The constraint def(x) says that x has a value; it is represented as 0 * x = 0.
 */
class LinearConstraintParser(val ontology: OWLOntology) extends ConstraintParser[LinearConstraint[BigFraction]] {

  override protected def parse(tokens: ConstraintTokens): LinearConstraint[BigFraction] =
    definedProperty(tokens).map(x => LinearConstraint(Map(x -> BigFraction.ZERO), BigFraction.ZERO))
      .getOrElse(equation(tokens))

  private def equation(tokens: ConstraintTokens): LinearConstraint[BigFraction] = {
    var coefficients = Map[OWLDataProperty, BigFraction]()
    var first = true
    do {
      val sign =
        if (tokens.accept("+")) BigFraction.ONE
        else if (tokens.accept("-")) BigFraction.MINUS_ONE
        else if (first) BigFraction.ONE
        else tokens.fail("'+', '-' or '='")
      val coefficient =
        if (tokens.atNumber) { val c = tokens.number(); tokens.expect("*"); c }
        else BigFraction.ONE
      val name = tokens.name()
      val x = property(name)
      if (coefficients.contains(x))
        throw new ParsingException(s"$name occurs twice; write its coefficients as one term")
      if (coefficient.equals(BigFraction.ZERO))
        throw new ParsingException(s"the coefficient of $name is 0; use def($name) to say that $name has a value")
      coefficients += x -> sign * coefficient
      first = false
    } while (!tokens.accept("="))
    LinearConstraint(coefficients, tokens.number())
  }
}
