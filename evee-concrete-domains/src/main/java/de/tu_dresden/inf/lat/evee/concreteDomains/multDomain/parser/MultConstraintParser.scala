package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.multDomain._
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.FreshClasses

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLDataHasValue, OWLDataProperty, OWLDataPropertyAssertionAxiom, OWLOntology}

import java.io.{BufferedReader, File, FileReader}
import scala.collection.JavaConverters.setAsJavaSetConverter
import scala.reflect.internal.util.FreshNameCreator
import org.semanticweb.owlapi.model.OWLAnnotationSubject
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom

class MultConstraintParser(ontology: OWLOntology) extends ConstraintParser[MultConstraint] {

  val reEqual = (strReIRI + raw"\s*=\s*" + strReNumber).r
  val reLess = (strReIRI + raw"\s*<\s*" + strReNumber).r
  val reMult = (strReIRI + raw"\s*\*\s*" + strReNumber + raw"\s*=\s*" + strReIRI).r

  val numberParser: (String => Double) = _.toDouble

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory


  def toConstraintMap(constraints: Map[OWLAnnotationSubject,String]): Map[OWLClass, MultConstraint] = {
    constraints.map{ case (subject, constraint) => {

        if(!subject.isIRI())
          throw new ParsingException(s"$subject could not be parsed to OWLClass: not a valid IRI")

        val owlClass = factory.getOWLClass(subject.asInstanceOf[IRI])

        owlClass -> toConstraint(constraint)
      }
    }.toMap[OWLClass, MultConstraint]
    
  }

  def toConstraint(constraintStr: String): MultConstraint = {

    constraintStr match {

      case reEqual(classStr, propertyStr, constantStr) =>
        val property = factory.getOWLDataProperty(getIRI(propertyStr))
        val constant = numberParser(constantStr)
        MultEqual(property,new BigFraction(constant))

      case reLess(propertyStr, constantStr) =>
        val property = factory.getOWLDataProperty(getIRI(propertyStr))
        val constant = numberParser(constantStr)
        MultLessThan(property, new BigFraction(constant))

      case reMult(property1Str, factorStr, property2Str) =>
        val property1 = factory.getOWLDataProperty(getIRI(property1Str))
        val factor = numberParser(factorStr)
        val property2 = factory.getOWLDataProperty(getIRI(property2Str))
        Multiplication(property1, new BigFraction(factor), property2)

      case _ =>
        throw new ParsingException(s"could not parse constraint $constraintStr")

    }

  }

  def toAnntoation(constraint: CDConstraint): OWLAnnotationAssertionAxiom = {
    return null //TODO implement
  }
}
