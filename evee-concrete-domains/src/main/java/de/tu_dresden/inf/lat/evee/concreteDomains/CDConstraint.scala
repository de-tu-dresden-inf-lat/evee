package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.model.OWLDataProperty

trait CDConstraint {
  def getOWLDataProperties: Set[OWLDataProperty]

  def isInconsistent: Boolean

  /**
   * the number of symbols of the constraint, counted like the size of an OWL axiom: data properties, numbers other
   * than factors 1 and -1, operators and the relation, e.g. 5 for x + 5 = y and 7 for 3*x + 4*y = 22; 2 for def(x)
   * and 1 for a contradiction
   */
  def size: Int
}

