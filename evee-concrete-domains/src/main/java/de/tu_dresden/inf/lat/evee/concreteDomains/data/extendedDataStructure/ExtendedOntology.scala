package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import org.semanticweb.owlapi.model.{OWLClass, OWLOntology}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName

case class ExtendedOntology[+CD_CONSTRAINT <: CDConstraint](ontology: OWLOntology, concreteDomain: ConcreteDomainName, constraintMap: Map[OWLClass, CD_CONSTRAINT]) {

  def constraintFor(clazz: OWLClass): CD_CONSTRAINT =
    constraintMap(clazz)

  def constraintNames: Set[OWLClass] =
    constraintMap.keySet
}
