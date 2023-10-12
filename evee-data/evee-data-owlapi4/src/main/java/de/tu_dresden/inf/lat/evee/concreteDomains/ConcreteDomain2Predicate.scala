package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.model.OWLDataProperty

trait CD2Predicate extends CDConstraint
trait CD2UnaryPredicate extends CD2Predicate


/**
 * property > bound
 */
case class CD2GreaterThan(property: OWLDataProperty, bound: Double)
  extends CD2UnaryPredicate {
  override def toString() = property + " > " + bound

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property = value
 */
case class CD2Equal(property: OWLDataProperty, value: Double)
  extends CD2UnaryPredicate {
  override def toString() = property + " = " + value

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property)

  override def isInconsistent: Boolean = false
}

/**
 * property1 + diff = property2
 */
case class CD2Sum(property1: OWLDataProperty, diff: Double, property2: OWLDataProperty)
  extends CD2Predicate {
  override def toString() = {
    if(diff>=0)
      property1 + " + " + diff + " = " + property2
    else
      property1 + " - " + (-diff) + " = " + property2
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = Set(property1, property2)

  override def isInconsistent: Boolean = property1.equals(property2) && !Math.abs(diff).equals(0.0)
}

object CD2Contradiction extends CD2Predicate {
  override def toString() = "⊥"

  override def getOWLDataProperties: Set[OWLDataProperty] = Set.empty

  override def isInconsistent: Boolean = true
}