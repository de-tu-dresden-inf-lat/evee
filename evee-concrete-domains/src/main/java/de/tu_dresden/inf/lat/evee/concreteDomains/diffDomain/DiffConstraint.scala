package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import org.semanticweb.owlapi.model.OWLDataProperty

/**
 * The ConcreteDomain2 supports:
 * - equality
 * - greater than
 * - x + q = y
 **/

trait DiffConstraint extends CDConstraint
trait DiffUnaryConstraint extends DiffConstraint


/**
 * property > bound
 */
case class DiffGreaterThan(property: OWLDataProperty, bound: Double)
  extends DiffUnaryConstraint {
  override def toString() = property + " > " + bound

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property = value
 */
case class DiffEqual(property: OWLDataProperty, value: Double)
  extends DiffUnaryConstraint {
  override def toString() = property + " = " + value

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property1 + diff = property2
 */
case class DiffSum(property1: OWLDataProperty, diff: Double, property2: OWLDataProperty)
  extends DiffConstraint {
  override def toString() = {
    if(diff>=0)
      property1 + " + " + diff + " = " + property2
    else
      property1 + " - " + (-diff) + " = " + property2
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property1, property2)

  override def isInconsistent: Boolean = property1.equals(property2) && !Math.abs(diff).equals(0.0)
}

object DiffContradiction extends DiffConstraint {
  override def toString() = "⊥"

  override def getOWLDataProperties: Set[OWLDataProperty] = Set.empty

  override def isInconsistent: Boolean = true
}