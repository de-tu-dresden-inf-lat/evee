package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser.LinearConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser.MultConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.preprocess.RedundancyRemover
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

  def toExtendedOntology(concreteDomain: ConcreteDomainName, ontology: OWLOntology): ExtendedOntology[CDConstraint] = {
    if (concreteDomain == null)
      return new ExtendedOntology(ontology, null, Map.empty[OWLClass, CDConstraint]) // no concrete domain, using default ELK for reasoning

    val constraintParser: ConstraintParser[CDConstraint] = concreteDomain match {
      case ConcreteDomainName.QDiff   => new DiffConstraintParser(ontology)
      case ConcreteDomainName.QMult   => new MultConstraintParser(ontology)
      case ConcreteDomainName.QLinear  => new LinearConstraintParser(ontology)
    }

    val constraints = constraintParser.toConstraintMap(getConstraints(ontology))

    val extOnt = ExtendedOntology(ontology, concreteDomain, constraints)
    extOnt
   // RedundancyRemover.getInstance().makeConstraintNamesUnique(extOnt)  //TODO: maybe remove ??? test!
  }

  def getConcreteDomainName(ontology: OWLOntology): ConcreteDomainName = {
    val domainStr =   
        ontology.getAnnotations.asScala
                    .find(_.getProperty().getIRI() == CONCRETE_DOMAIN_ANNOTATION_PROP_IRI)
                        .flatMap { annotation =>
                                  annotation.getValue match {
                                    case literal: OWLLiteral => Some(literal.getLiteral)
                                    case _ => None
                                    }
                        }.getOrElse(throw new ParsingException(s"could not find concrete domain annotation in ontology"))
                                
    ConcreteDomainName.getConcreteDomainName(domainStr)  
  }

  /**
   * The text of the constraint of each annotated subject. A class can have at most one constraint; constraints are
   * combined by conjunctions of their classes (ObjectIntersectionOf).
   */
  def getConstraints(ontology: OWLOntology): Map[OWLAnnotationSubject,String] = {
    val annotations = ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).asScala.toSeq
      .filter(_.getProperty.getIRI == CONSTRAINT_ANNOTATION_PROP_IRI)
    annotations.groupBy(_.getSubject).map { case (subject, axioms) =>
      val constraints = axioms.map(_.getValue match {
        case literal: OWLLiteral => literal.getLiteral
        case other => throw new ParsingException(s"The constraint of $subject has to be a string, not $other")
      }).distinct.sorted
      if (constraints.size > 1)
        throw new ParsingException(s"$subject has the constraints " + constraints.map("\"" + _ + "\"").mkString(", ") +
          ", but a class can have at most one; use one class per constraint and combine them with ObjectIntersectionOf")
      subject -> constraints.head
    }
  }


}
