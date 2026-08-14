package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.{CDFactory, CDParser, CDReasoner}
import org.semanticweb.owlapi.model.OWLOntology

object MultFactory extends CDFactory[MultConstraint] {

  override def getParser(owlOntology: OWLOntology): CDParser[MultConstraint] =
    new MultParser(owlOntology)

  override def getReasoner(): CDReasoner[MultConstraint] =
    new MultReasoner()
}
