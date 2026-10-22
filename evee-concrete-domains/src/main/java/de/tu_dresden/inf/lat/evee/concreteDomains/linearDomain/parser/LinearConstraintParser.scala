package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, ConstraintTokens}
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLOntology}

/**
 * Parser for the concrete domain QLinear. A constraint is a linear equation a1 * x1 + ... + an * xn = c, where the
 * xi are data properties and the ai and c are numbers, e.g., 2 * x - y = 0.5. A coefficient 1 can be left out, and
 * the coefficients of a data property that occurs several times are added up.
 */
class LinearConstraintParser(val ontology: OWLOntology) extends ConstraintParser[LinearConstraint[BigFraction]] {

  override protected def parse(tokens: ConstraintTokens): LinearConstraint[BigFraction] = {
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
      val x = property(tokens)
      coefficients += x -> (coefficients.getOrElse(x, BigFraction.ZERO) + sign * coefficient)
      first = false
    } while (!tokens.accept("="))
    LinearConstraint(coefficients, tokens.number())
  }
}
