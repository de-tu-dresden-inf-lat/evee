package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser.LinearConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser.MultConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLAnnotationSubject
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLClass

import scala.collection.JavaConverters._
import java.io.File


object ExtendedOntologyParser {

  val CONCRETE_DOMAIN_ANNOTATION_PROP_IRI: IRI = IRI.create(CDAnnotationNames.CONCRETE_DOMAIN_ANNOTATION_PROP)
  val CONSTRAINT_ANNOTATION_PROP_IRI: IRI = IRI.create(CDAnnotationNames.CONSTRAINT_ANNOTATION_PROP)

  /**
   * parses an OWLOntology containing concrete domain constraints as annotations 
   * into ExtendedOntology by parsing these Annotation into a Constraint Map
  **/
  def toExtendedOntology(ontology: OWLOntology): ExtendedOntology[CDConstraint] = {
    val domain = getConcreteDomainName(ontology)
    toExtendedOntology(domain, ontology)
  }

  def toExtendedOntology(concreteDomain: ConcreteDomainName, ontology: OWLOntology): ExtendedOntology[CDConstraint] =
    ExtendedOntology(ontology, concreteDomain, toConstraintMap(concreteDomain, ontology))

  /**
   * The constraints of the classes of the ontology and its imports. Each constraint is read with the prefixes of the
   * ontology that contains it. A class can have at most one constraint; constraints are combined by conjunctions of
   * their classes (ObjectIntersectionOf). Without a concrete domain, there are no constraints.
   */
  def toConstraintMap(concreteDomain: ConcreteDomainName, ontology: OWLOntology): Map[OWLClass, CDConstraint] = {
    if (concreteDomain == null)
      return Map.empty
    val annotations = ontology.getImportsClosure.asScala.toSeq.flatMap(o =>
      o.getAxioms(AxiomType.ANNOTATION_ASSERTION).asScala.toSeq
        .filter(_.getProperty.getIRI == CONSTRAINT_ANNOTATION_PROP_IRI)
        .map(axiom => (axiom, o)))
    annotations.groupBy(_._1.getSubject).map { case (subject, found) =>
      val constraints = found.map(_._1.getValue match {
        case literal: OWLLiteral => literal.getLiteral
        case other => throw new ParsingException(s"The constraint of $subject has to be a string, not $other")
      }).distinct.sorted
      if (constraints.size > 1)
        throw new ParsingException(s"$subject has the constraints " + constraints.map("\"" + _ + "\"").mkString(", ") +
          ", but a class can have at most one; use one class per constraint and combine them with ObjectIntersectionOf")
      parser(concreteDomain, found.head._2).classConstraint(subject, constraints.head)
    }
  }

  private def parser(concreteDomain: ConcreteDomainName, ontology: OWLOntology): ConstraintParser[CDConstraint] =
    concreteDomain match {
      case ConcreteDomainName.QDiff => new DiffConstraintParser(ontology)
      case ConcreteDomainName.QMult => new MultConstraintParser(ontology)
      case ConcreteDomainName.QLinear => new LinearConstraintParser(ontology)
    }

  private def concreteDomainAnnotations(ontology: OWLOntology) =
    ontology.getAnnotations.asScala.toSeq.filter(_.getProperty.getIRI == CONCRETE_DOMAIN_ANNOTATION_PROP_IRI)

  /** whether the ontology names its concrete domain by an annotation cd:concreteDomain */
  def hasConcreteDomain(ontology: OWLOntology): Boolean = concreteDomainAnnotations(ontology).nonEmpty

  /** the concrete domain that the annotation cd:concreteDomain of the ontology names */
  def getConcreteDomainName(ontology: OWLOntology): ConcreteDomainName =
    concreteDomainAnnotations(ontology).map(_.getValue).distinct match {
      case Seq() => throw new ParsingException(
        s"The ontology has no annotation cd:concreteDomain, which names its concrete domain (${ConcreteDomainName.NAMES})")
      case Seq(literal: OWLLiteral) => ConcreteDomainName.getConcreteDomainName(literal.getLiteral)
      case Seq(other) => throw new ParsingException(
        s"The annotation cd:concreteDomain of the ontology has to be a string (${ConcreteDomainName.NAMES}), not $other")
      case several => throw new ParsingException("The ontology has the annotations cd:concreteDomain " +
        several.map { case literal: OWLLiteral => "\"" + literal.getLiteral + "\""; case other => other }.mkString(", ") +
        ", but it can use only one concrete domain")
    }
}
