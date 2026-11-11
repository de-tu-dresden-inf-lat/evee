package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

trait ConstraintNetwork[X <: CDConstraint] {
  def generateSolution(negativePredicates: Iterable[X]) : Map[OWLDataProperty, BigFraction]

  def derivationStructure(): Set[IInference[X]]

  def derivedPredicates(): Set[X]

  def propagate()

  def fill(predicates: Iterable[X])

  def setContinueEvenIfInconsistent()

}
