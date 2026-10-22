package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.io.StringDocumentSource
import org.semanticweb.owlapi.model.{IRI, OWLClass, OWLOntology, OWLOntologyManager}

/**
 * Test ontologies: the example ontologies in src/test/resources/examples, and small ontologies written in OWL
 * functional syntax. The example ontologies were converted from the test data of the original concrete domain
 * prototype: the constraints are attached to classes via cd:constraint annotations.
 */
object TestOntologies {

  /** namespace of the small test ontologies, abbreviated by the default prefix ":" */
  val NS = "http://ex.org/o#"

  private val Prefixes =
    "Prefix(:=<" + NS + ">)\n" +
    "Prefix(cd:=<http://tu-dresden.de/inf/lat/evee/cd#>)\n"

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

  /** the class with the given name in the namespace of the small test ontologies */
  def cls(name: String): OWLClass = OWLManager.getOWLDataFactory.getOWLClass(IRI.create(NS + name))

  /**
   * A small test ontology with IRI http://ex.org/o, the given concrete domain, the given constraints
   * (class name -> constraint) and further content (axioms, imports) in functional syntax.
   */
  def ontology(domain: String, constraints: Seq[(String, String)], content: String*): OWLOntology =
    ontology(OWLManager.createOWLOntologyManager(), "http://ex.org/o", domain, constraints, content: _*)

  def ontology(manager: OWLOntologyManager, iri: String, domain: String, constraints: Seq[(String, String)],
               content: String*): OWLOntology = {
    val text = new StringBuilder(Prefixes).append("Ontology(<").append(iri).append(">\n")
    content.filter(_.startsWith("Import(")).foreach(text.append(_).append("\n"))
    if (domain != null)
      text.append("Annotation(cd:concreteDomain \"").append(domain).append("\")\n")
    for ((name, constraint) <- constraints) {
      text.append("Declaration(Class(:").append(name).append("))\n")
      text.append("AnnotationAssertion(cd:constraint :").append(name).append(" \"").append(constraint).append("\")\n")
    }
    content.filterNot(_.startsWith("Import(")).foreach(text.append(_).append("\n"))
    text.append(")\n")
    manager.loadOntologyFromOntologyDocument(new StringDocumentSource(text.toString))
  }
}
