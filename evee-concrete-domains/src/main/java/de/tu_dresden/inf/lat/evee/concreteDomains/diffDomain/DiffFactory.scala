package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.{CDFactory, ConstraintParser, CDReasoner}
import org.semanticweb.owlapi.model.OWLOntology
import de.tu_dresden.inf.lat.evee.concreteDomains.elkCD.ELKCDReasoner
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser.ExtendedOntologyParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._

object DiffFactory extends CDFactory[DiffConstraint] {

  def getParser(ontology: OWLOntology): ConstraintParser[DiffConstraint] =
    new DiffConstraintParser(ontology)

  def getReasoner(): CDReasoner[DiffConstraint] =
    new DiffReasoner()

  def getELKReasoner(ontology: OWLOntology): ELKCDReasoner[DiffConstraint] = {
    val extOnt = ExtendedOntologyParser.toExtendedOntology(ontology).asInstanceOf[ExtendedOntology[DiffConstraint]]
    getELKReasoner(extOnt)
  }
}
