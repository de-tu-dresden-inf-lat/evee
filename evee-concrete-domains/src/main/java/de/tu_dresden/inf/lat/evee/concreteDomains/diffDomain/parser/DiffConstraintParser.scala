package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.{DiffConstraint, DiffEqual, DiffGreaterThan, DiffSum}
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, ConstraintTokens}
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLOntology

/**
 * Parser for the concrete domain QDiff. A constraint has one of the forms x = c, x > c, x + c = y or x - c = y,
 * where x and y are data properties and c is a number.
 */
class DiffConstraintParser(val ontology: OWLOntology) extends ConstraintParser[DiffConstraint] {

  override protected def parse(tokens: ConstraintTokens): DiffConstraint = {
    val x = property(tokens)
    if (tokens.accept("="))
      DiffEqual(x, tokens.number())
    else if (tokens.accept(">"))
      DiffGreaterThan(x, tokens.number())
    else {
      val sign =
        if (tokens.accept("+")) BigFraction.ONE
        else if (tokens.accept("-")) BigFraction.MINUS_ONE
        else tokens.fail("'=', '>', '+' or '-'")
      val difference = tokens.number().multiply(sign)
      tokens.expect("=")
      DiffSum(x, difference, property(tokens))
    }
  }
}
