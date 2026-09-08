package de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.DiffConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser.DiffConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser.{DoubleLinearConstraintParser,LinearConstraintParser}
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser.MultConstraintParser
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure._
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.{CDAnnotationNames, ConcreteDomainName}
import de.tu_dresden.inf.lat.evee.concreteDomains.preprocess.RedundancyRemover

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.AxiomType;

import scala.collection.JavaConverters._


import java.io.File
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLAnnotationSubject

object ExtendedOntologyParser {

  val CONCRETE_DOMAIN_ANNOTATION_PROP_IRI: IRI = IRI.create(CDAnnotationNames.CONCRETE_DOMAIN_ANNOTATION_PROP)
  val CONSTRAINT_ANNOTATION_PROP_IRI: IRI = IRI.create(CDAnnotationNames.CONSTRAINT_ANNOTATION_PROP)

  /**
   * parses an OWLOntology containing concrete domain constraints as annotations 
   * into ExtendedOntology by parsing these Annotation into a Constraint Map
  **/
  def toExtendedOntology(ontology: OWLOntology): ExtendedOntology[CDConstraint] = {
    val domain = getConcreteDomainName(ontology) //TODO: behaviour if domainStr is "" (now: exception) -> try/catch here??
    toExtendedOntology(domain, ontology)
  }

  def toExtendedOntology(concreteDomain: ConcreteDomainName, ontology: OWLOntology): ExtendedOntology[CDConstraint] = {
    val constraintParser: ConstraintParser[CDConstraint] = concreteDomain match {
      case ConcreteDomainName.QDiff   => new DiffConstraintParser(ontology)
      case ConcreteDomainName.QMult   => new MultConstraintParser(ontology)
      case ConcreteDomainName.QLinear  => new DoubleLinearConstraintParser(ontology)
    }

    val constraints = constraintParser.toConstraintMap(getConstraints(ontology))

    val extOnt = ExtendedOntology(ontology, concreteDomain, constraints)

    RedundancyRemover.getInstance().makeConstraintNamesUnique(extOnt)  //TODO: maybe remove ??? test!
  }

  def getConcreteDomainName(ontology: OWLOntology): ConcreteDomainName = {
    val domainStr =   
        ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).asScala
                    .find(_.getProperty().getIRI() == CONCRETE_DOMAIN_ANNOTATION_PROP_IRI)
                        .map(_.getValue)
                          .flatMap(x => Option(x.asLiteral().orElse(null)))
                            .map(_.getLiteral)
                              .getOrElse("")
    
    ConcreteDomainName.getConcreteDomainName(domainStr)  
  }

  def getConstraints(ontology: OWLOntology): Map[OWLAnnotationSubject,String] = {
    ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION).asScala
      .filter(_.getProperty.getIRI == CONSTRAINT_ANNOTATION_PROP_IRI)
        .flatMap { axiom =>
            val literal = axiom.getValue.asLiteral()

            if (literal.isPresent)
              Some(axiom.getSubject -> literal.get.getLiteral)
            else
              None
          }.toMap
  }


}
