package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.util.DefaultPrefixManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.formats.PrefixDocumentFormat

import java.io.File

trait ConstraintParser[+CD <: CDConstraint] {

  def ontology: OWLOntology

  val prefixFormat = 
      ontology.getOWLOntologyManager.getOntologyFormat(ontology) match {
        case format: PrefixDocumentFormat => format
        case _ => throw new ParsingException("Error: Cannot parse prefixes")
      }

  val strReIRI = raw"(<[^>]+>|[^\s]+)"
  val strReNumber = raw"([+-]?[0-9]+(?:\.[0-9]*)?)"
  val prefixManager = new DefaultPrefixManager()

  def toConstraintMap(constraintsStr: Map[OWLAnnotationSubject, String]): Map[OWLClass, CD]
  def toConstraint(constraintStr: String): CD

  def toAnntoation(constraint: CDConstraint): OWLAnnotationAssertionAxiom

  def getIRI(str: String): IRI = {
    prefixFormat.getIRI(str) match {
      case null => throw new ParsingException(s"Error: Cannot parse prefix $str")
      case iri => iri
    }
  }

}
