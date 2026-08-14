package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference
import org.semanticweb.owlapi.model.OWLDataProperty

trait ConstraintNetwork[X <: CDConstraint] {
  def generateSolution(negativePredicates: Iterable[X]) : Map[OWLDataProperty, Double]

  def derivationStructure(): Set[IInference[X]]

  def derivedPredicates(): Set[X]

  def propagate()

  def fill(predicates: Iterable[X])

  def setContinueEvenIfInconsistent()

}
