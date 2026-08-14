package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.data.extendedDataStructure.ExtendedOntology

import de.tu_dresden.inf.lat.evee.elkCD.ELKCDReasoner

import org.semanticweb.owlapi.model.OWLOntology

import java.io.File

trait CDFactory[CD <: CDConstraint] {

  def getParser(owlOntology: OWLOntology): CDParser[CD]

  def getReasoner(): CDReasoner[CD]

  def getELKReasoner(extendedOntology: ExtendedOntology[CD]) =
    new ELKCDReasoner[CD](extendedOntology, getReasoner())

  def getELKReasoner(ontology: OWLOntology, cdMappingFile: File): ELKCDReasoner[CD] =
    getELKReasoner(new ExtendedOntology[CD](ontology, (getParser(ontology)).parse(cdMappingFile)))

}
