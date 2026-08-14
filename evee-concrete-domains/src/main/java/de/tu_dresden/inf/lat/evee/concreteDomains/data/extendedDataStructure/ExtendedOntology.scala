package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import org.semanticweb.owlapi.model.{OWLClass, OWLOntology}

case class ExtendedOntology[CD_CONSTRAINT <: CDConstraint](ontology: OWLOntology, constraintMap: Map[OWLClass, CD_CONSTRAINT]) {

  def constraintFor(clazz: OWLClass): CD_CONSTRAINT =
    constraintMap(clazz)

  def constraintNames: Set[OWLClass] =
    constraintMap.keySet
}
