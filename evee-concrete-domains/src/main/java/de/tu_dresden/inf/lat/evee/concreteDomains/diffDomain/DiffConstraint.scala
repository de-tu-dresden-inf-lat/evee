package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers.FractionOps
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

/**
 * The domain D_Q,diff supports:
 * - equality
 * - greater than
 * - x + q = y
 **/

trait DiffConstraint extends CDConstraint
trait DiffUnaryConstraint extends DiffConstraint


/**
 * property > bound
 */
case class DiffGreaterThan(property: OWLDataProperty, bound: BigFraction)
  extends DiffUnaryConstraint {
  override def toString() = property + " > " + Numbers.format(bound)

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false

  override def size: Int = 3
}

/**
 * property = value
 */
case class DiffEqual(property: OWLDataProperty, value: BigFraction)
  extends DiffUnaryConstraint {
  override def toString() = property + " = " + Numbers.format(value)

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false

  override def size: Int = 3
}

/**
 * property1 + diff = property2
 */
case class DiffSum(property1: OWLDataProperty, diff: BigFraction, property2: OWLDataProperty)
  extends DiffConstraint {
  override def toString() = {
    if (property1.equals(property2) && diff.equals(BigFraction.ZERO))
      "def(" + property1 + ")"
    else if(diff >= BigFraction.ZERO)
      property1 + " + " + Numbers.format(diff) + " = " + property2
    else
      property1 + " - " + Numbers.format(-diff) + " = " + property2
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property1, property2)

  override def isInconsistent: Boolean = property1.equals(property2) && !diff.equals(BigFraction.ZERO)

  override def size: Int = if (property1.equals(property2) && diff.equals(BigFraction.ZERO)) 2 else 5
}

object DiffContradiction extends DiffConstraint {
  override def toString() = "⊥"

  override def getOWLDataProperties: Set[OWLDataProperty] = Set.empty

  override def isInconsistent: Boolean = true

  override def size: Int = 1
}