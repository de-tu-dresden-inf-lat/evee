package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{OWLClass, OWLOntology}

/**
 * Loads the example ontologies in src/test/resources/examples. They were converted from the test data of the
 * original concrete domain prototype: the constraints are attached to classes via cd:constraint annotations.
 */
object TestOntologies {

  def load(path: String): OWLOntology = {
    val stream = getClass.getResourceAsStream("/examples/" + path)
    if (stream == null)
      throw new IllegalArgumentException("test ontology not found: " + path)
    try OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(stream)
    finally stream.close()
  }

  /** the class with the given name in the default namespace of the ontology */
  def owlClass(ontology: OWLOntology, name: String): OWLClass = {
    val format = ontology.getOWLOntologyManager.getOntologyFormat(ontology).asPrefixOWLDocumentFormat()
    ontology.getOWLOntologyManager.getOWLDataFactory.getOWLClass(format.getIRI(":" + name))
  }
}
