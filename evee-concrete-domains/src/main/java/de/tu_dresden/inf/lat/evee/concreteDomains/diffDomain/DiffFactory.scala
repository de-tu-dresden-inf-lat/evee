package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.{CDFactory, CDParser, CDReasoner}
import org.semanticweb.owlapi.model.OWLOntology

object DiffFactory extends CDFactory[DiffConstraint] {

  override def getParser(ontology: OWLOntology): CDParser[DiffConstraint] =
    new DiffConstraintParser(ontology)

  override def getReasoner(): CDReasoner[DiffConstraint] =
    new DiffReasoner()
}
