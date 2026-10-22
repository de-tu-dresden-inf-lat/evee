package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintParser, CDConstraint}
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.{IRI, OWLClass, OWLDataProperty, OWLOntology}
import org.semanticweb.owlapi.util.DefaultPrefixManager

import java.io.{BufferedReader, File, FileReader}
import org.semanticweb.owlapi.model.OWLAnnotationSubject
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom

/**
 * Parser for the concrete domain QLinear: linear equations such as 2*x+-1*y=5, with exact rational coefficients
 */
class LinearConstraintParser(val ontology: OWLOntology) extends ConstraintParser[LinearConstraint[BigFraction]] {

  val numberParser: String => BigFraction = Numbers.parse

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory

  val reCoefficient = (strReNumber + raw"\s*\*\s*" + strReIRI).r
  val reConstant = (raw"=\s*" + strReNumber).r

  def toConstraintMap(constraints: Map[OWLAnnotationSubject, String]): Map[OWLClass, LinearConstraint[BigFraction]] = {

   constraints.map{ case (subject, constraint) => {

      if(!subject.isIRI())
         throw new ParsingException(s"$subject could not be parsed to OWLClass: not a valid IRI")

      val owlClass = factory.getOWLClass(subject.asInstanceOf[IRI])
      owlClass -> toConstraint(constraint)

    }}.toMap[OWLClass, LinearConstraint[BigFraction]]
  }

  def toConstraint(constraintStr: String): LinearConstraint[BigFraction] = {
    val equation = constraintStr.split("=")
    if(equation.length != 2)
      throw new ParsingException(s"could not parse constraint $constraintStr")

      val rhs = numberParser(equation(1))
      
      val lhs = equation(0).split("(?=[+-])").map{
        summand => summand match {
          case reCoefficient(coef, prop) =>
              factory.getOWLDataProperty(getIRI(prop)) -> numberParser(coef)
          case _ => 
            throw new ParsingException(s"could not parse constraint $constraintStr")
        }
      }.toMap[OWLDataProperty, BigFraction]

    LinearConstraint(lhs, rhs)
  }

}
