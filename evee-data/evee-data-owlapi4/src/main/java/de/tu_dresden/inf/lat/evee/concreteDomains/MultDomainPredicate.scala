package de.tu_dresden.inf.lat.evee.concreteDomains

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

/**
 * The ConcreteDomain2 supports:
 * - equality
 * - greater than
 * - x * q = y
 **/


trait CDMultPredicate extends CDConstraint
trait CDMultUnaryPredicate extends CDMultPredicate


/**
 * property > bound
 */
case class CDMultGreaterThan(property: OWLDataProperty, bound: BigFraction)
  extends CDMultUnaryPredicate {
  override def toString() = property + " > " + bound.doubleValue()

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property = value
 */
case class CDMultEqual(property: OWLDataProperty, value: BigFraction)
  extends CDMultUnaryPredicate {
  override def toString() = property + " = " + value.doubleValue()

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property1 * factor = property2
 *
 * Side condition: factor > 0
 */
case class CDMultiplication(property1: OWLDataProperty, factor: BigFraction, property2: OWLDataProperty)
  extends CDMultPredicate {

  if(factor.getNumerator.signum()!=1)
    throw new IllegalArgumentException("Factor has to be larger than 0")

  override def toString() = {
    factor.doubleValue()+" * "+property1+" = "+property2
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property1, property2)

  override def isInconsistent: Boolean = property1.equals(property2) && !factor.getNumerator().equals(factor.getDenominator())
}

object CDMultContradiction extends CDMultPredicate {
  override def toString() = "⊥"

  override def getOWLDataProperties: Set[OWLDataProperty] = Set.empty

  override def isInconsistent: Boolean = true
}