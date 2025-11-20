package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.model.OWLDataProperty

trait CDConstraint {
  def getOWLDataProperties: Set[OWLDataProperty]

  def isInconsistent: Boolean
}

