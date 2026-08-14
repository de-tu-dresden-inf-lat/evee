package de.tu_dresden.inf.lat.evee.concreteDomains.tools

import org.semanticweb.owlapi.model.{OWLClass, OWLOntology, IRI}
import org.semanticweb.owlapi.model.parameters.Imports

import scala.collection.JavaConverters.asScalaSetConverter

class FreshClasses(ontology: OWLOntology, prefix: String) {
  val names = ontology.getClassesInSignature(Imports.INCLUDED).asScala

  val factory = ontology.getOWLOntologyManager.getOWLDataFactory()

  var count = 0

  def next(): OWLClass = {
    var result: OWLClass = null

    do {
      val name = prefix+count
      count+=1
      result = factory.getOWLClass(IRI.create(name))
    } while (names.contains(result))

    result
  }
}
