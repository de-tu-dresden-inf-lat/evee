package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.{DiffConstraint, DiffEqual, DiffGreaterThan, DiffSum}
import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.general.data.exceptions

import org.semanticweb.owlapi.model.{IRI, OWLClass, OWLOntology}
import org.semanticweb.owlapi.util.DefaultPrefixManager

import org.semanticweb.owlapi.model.OWLAnnotationSubject
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom

/**
 * Parser for the concrete domain QDiff
 */
class DiffConstraintParser(ontology: OWLOntology) extends ConstraintParser[DiffConstraint] {

  val numberParser: (String => Double) = _.toDouble

  val reEqual =   (strReIRI + raw"\s*=\s*" + strReNumber).r
  val reGreater = (strReIRI + raw"\s*>\s*" + strReNumber).r
  val reDiff =    (strReIRI + raw"\s*\+\s*" + strReNumber + raw"\s*=\s*" + strReIRI).r

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory

    def toConstraintMap(constraints: Map[OWLAnnotationSubject,String]): Map[OWLClass, DiffConstraint] = {

    constraints.map{ case (subject, constraint) => 

        if(!subject.isIRI())
          throw new ParsingException(s"$subject could not be parsed to OWLClass: not a valid IRI")

        val owlClass = factory.getOWLClass(subject.asInstanceOf[IRI])

        owlClass -> toConstraint(constraint)
      
    }.toMap[OWLClass, DiffConstraint]

  }

  def toConstraint(constraintStr: String): DiffConstraint = {
    constraintStr match {
      case reEqual(propertyStr, constantStr) =>
        val property = factory.getOWLDataProperty(getIRI(propertyStr))
        val constant = numberParser(constantStr)
        DiffEqual(property,constant)

      case reGreater(propertyStr, constantStr) =>
        val property = factory.getOWLDataProperty(getIRI(propertyStr))
        val constant = numberParser(constantStr)
       DiffGreaterThan(property, constant)

      case reDiff(property1Str, diffStr, property2Str) =>
        val property1 = factory.getOWLDataProperty(getIRI(property1Str))
        val diff = numberParser(diffStr)
        val property2 = factory.getOWLDataProperty(getIRI(property2Str))
       DiffSum(property1, diff, property2)

      case _ =>
        throw new ParsingException(s"could not parse constraint $constraintStr")
    }
  }

  def toAnntoation(constraint: CDConstraint): OWLAnnotationAssertionAxiom = {
    return null //TODO implement
  }


}
