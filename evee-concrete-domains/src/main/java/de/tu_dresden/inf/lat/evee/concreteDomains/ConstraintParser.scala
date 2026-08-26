package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.util.DefaultPrefixManager



import java.io.File
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom

trait ConstraintParser[+CD <: CDConstraint] {

  val strReIRI = raw"(<[^>]+>|[^\s]+)"
  val strReNumber = raw"([+-]?[0-9]+(?:\.[0-9]*)?)"
  val prefixManager = new DefaultPrefixManager()

  def toConstraintMap(constraintsStr: Map[OWLAnnotationSubject, String]): Map[OWLClass, CD]
  def toConstraint(constraintStr: String): CD

  def toAnntoation(constraint: CDConstraint): OWLAnnotationAssertionAxiom

  def getIRI(str: String): IRI =
    prefixManager.getIRI(str)

}
