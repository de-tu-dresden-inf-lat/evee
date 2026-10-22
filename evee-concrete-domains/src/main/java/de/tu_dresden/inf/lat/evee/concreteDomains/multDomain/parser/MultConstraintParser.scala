package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, ConstraintTokens}
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.{OWLDataProperty, OWLOntology}

/**
 * Parser for the concrete domain QMult. A constraint has one of the forms x = c, x < c, x * c = y or c * x = y,
 * where x and y are data properties and c is a number; a factor c has to be positive.
 */
class MultConstraintParser(val ontology: OWLOntology) extends ConstraintParser[MultConstraint] {

  override protected def parse(tokens: ConstraintTokens): MultConstraint =
    if (tokens.atNumber) {
      val factor = tokens.number()
      tokens.expect("*")
      product(property(tokens), factor, tokens)
    } else {
      val x = property(tokens)
      if (tokens.accept("=")) MultEqual(x, tokens.number())
      else if (tokens.accept("<")) MultLessThan(x, tokens.number())
      else if (tokens.accept("*")) product(x, tokens.number(), tokens)
      else tokens.fail("'=', '<' or '*'")
    }

  /** reads the rest "= y" of a product x * factor = y */
  private def product(x: OWLDataProperty, factor: BigFraction, tokens: ConstraintTokens): Multiplication = {
    tokens.expect("=")
    val y = property(tokens)
    if (factor.compareTo(BigFraction.ZERO) <= 0)
      throw new ParsingException(s"the factor ${Numbers.format(factor)} has to be positive")
    Multiplication(x, factor, y)
  }
}
