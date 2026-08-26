package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDFactory, ConstraintParser, CDReasoner}
import org.semanticweb.owlapi.model.OWLOntology
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser.MultConstraintParser

object MultFactory extends CDFactory[MultConstraint] {

  def getParser(owlOntology: OWLOntology): ConstraintParser[MultConstraint] =
    new MultConstraintParser(owlOntology)

  def getReasoner(): CDReasoner[MultConstraint] =
    new MultReasoner()

  def getELKReasoner(ontology: OWLOntology): ELKCDReasoner[MultConstraint] = {
    val extOnt = ExtendedOntologyParser.toExtendedOntology(ontology).asInstanceOf[ExtendedOntology[MultConstraint]]
    getELKReasoner(extOnt)
  }
}
