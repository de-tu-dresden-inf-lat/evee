package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser

import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner

import org.semanticweb.owlapi.model.OWLOntology

import java.io.File

trait CDFactory[CD <: CDConstraint] {

  def getParser(owlOntology: OWLOntology): ConstraintParser[CD]

  def getReasoner(): CDReasoner[CD]

  def getELKReasoner(extendedOntology: ExtendedOntology[CD]) =
    new ELKCDReasoner[CD](extendedOntology, getReasoner())

  def getELKReasoner(ontology: OWLOntology): ELKCDReasoner[CD]

}
