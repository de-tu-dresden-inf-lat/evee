package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain


import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

/**
 * multiplication domain supports:
 * - equality
 * - greater than
 * - x * q = y
 **/


trait MultConstraint extends CDConstraint
trait MultUnaryConstraint extends MultConstraint


/**
 * property < bound
 */
case class MultLessThan(property: OWLDataProperty, bound: BigFraction)
  extends MultUnaryConstraint {
  override def toString() = property + " < " + Numbers.format(bound)

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false

  override def size: Int = 3
}

/**
 * property = value
 */
case class MultEqual(property: OWLDataProperty, value: BigFraction)
  extends MultUnaryConstraint {
  override def toString() = property + " = " + Numbers.format(value)

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false

  override def size: Int = 3
}

/**
 * property1 * factor = property2
 *
 * Side condition: factor > 0
 */
case class Multiplication(property1: OWLDataProperty, factor: BigFraction, property2: OWLDataProperty)
  extends MultConstraint {

  if(factor.getNumerator.signum()!=1)
    throw new IllegalArgumentException("Factor has to be larger than 0")

  override def toString() = {
    if (property1.equals(property2) && factor.equals(BigFraction.ONE))
      "def(" + property1 + ")"
    else
      Numbers.format(factor) + " * " + property1 + " = " + property2
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property1, property2)

  override def isInconsistent: Boolean = property1.equals(property2) && !factor.getNumerator().equals(factor.getDenominator())

  /** the factor is a number before the first property, without an operator, as in linear constraints */
  override def size: Int = if (property1.equals(property2) && factor.equals(BigFraction.ONE)) 2 else 4
}

object MultContradiction extends MultConstraint {
  override def toString() = "⊥"

  override def getOWLDataProperties: Set[OWLDataProperty] = Set.empty

  override def isInconsistent: Boolean = true

  override def size: Int = 1
}