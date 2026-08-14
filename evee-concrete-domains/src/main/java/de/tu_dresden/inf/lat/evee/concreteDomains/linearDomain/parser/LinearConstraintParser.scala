package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.LinearConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.CDParser

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.{IRI, OWLClass, OWLDataProperty, OWLOntology}
import org.semanticweb.owlapi.util.DefaultPrefixManager

import java.io.{BufferedReader, File, FileReader}

/** **
 * Format:
 *
 * # COMMENT
 *
 * PREFIX ss: IRI
 * ..
 * CLASS_IRI:
 * DOUBLE * DATA_PROPERTY
 * DOUBLE * DATAPROPRTY
 * ..
 * = DOUBLE
 */

class DoubleLinearConstraintParser(ontology: OWLOntology)
extends LinearConstraintParser[Double](
  ontology,
  _.toDouble
)

class LinearConstraintParser[T](ontology: OWLOntology, numberParser: String => T) extends CDParser[LinearConstraint[T]] {

  val COMMENT = "#"
  val PREFIX = "PREFIX"

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory

  val prefixManager = new DefaultPrefixManager()
  //val manchesterParser =
  //  new ManchesterOWLSyntaxParserImpl(new OWLAPIConfigProvider, factory)

  override def parse(file: File): Map[OWLClass, LinearConstraint[T]] = {
    parse(new BufferedReader(new FileReader(file)))
  }

  def parse(reader: BufferedReader): Map[OWLClass, LinearConstraint[T]] = {
    parse(reader.lines())
  }

  val reComment = raw"#.*".r
  val rePrefix = (PREFIX+raw"\s*(\w*):\s+<?([^>]+)>?").r

  val strReIRI = raw"(<[^>]+>|[^\s]+)"
  val strReNumber = raw"(-?[0-9]+(?:\.[0-9]*)?)"

  val reClass = (strReIRI + raw"\s*:").r
  val reCoefficient = (strReNumber + raw"\s*\*\s*" + strReIRI).r
  val reConstant = (raw"=\s*" + strReNumber).r

  def parse(lines: java.util.stream.Stream[String]): Map[OWLClass,LinearConstraint[T]] = {

    var currentClass: Option[OWLClass] = None

    var currentCoefficients = Map[OWLDataProperty, T]()

    var constraints = Map[OWLClass, LinearConstraint[T]]()

    var lineNr = 0

    lines.forEach { line =>

      lineNr += 1

      line.trim match {

        case "" => ;

        case reComment(_*) => ;

        case rePrefix(prefix, iri) =>
          prefixManager.setPrefix(prefix, iri)

        case reClass(classString) =>
          if (!currentClass.isEmpty)
            throw new ParsingException("Line " + lineNr + ": Did not finish parsing class " + currentClass.get)

          currentClass = Some(
            factory.getOWLClass(
              prefixManager
                .getIRI(
                  classString)))

          if (constraints.contains(currentClass.get))
            throw new ParsingException("Line " + lineNr + ": " + currentClass + " has already a constraint assigned!")

        case reCoefficient(num, dataIRI, _*) =>
          if (currentClass.isEmpty)
            throw new ParsingException("Line " + lineNr + ": coefficient before class: " + line)
          val prp = factory.getOWLDataProperty(prefixManager.getIRI(dataIRI))
          if (currentCoefficients.contains(prp))
            throw new ParsingException(
              "Line " + lineNr + ": two coefficients for same data property: " + prp + "\n" +
                "has already assigned: " + currentCoefficients(prp) + "\n" +
                "line: " + line)
          currentCoefficients += (prp -> numberParser(num))

        case reConstant(number) =>
          if (currentClass.isEmpty)
            throw new ParsingException("Line " + lineNr + ": constant before class: " + line)
          else if (currentCoefficients.isEmpty)
            throw new ParsingException("Line " + lineNr + ": constraint without coefficients: " + line)

          val constant = numberParser(number)

          constraints +=
            (currentClass.get
              -> LinearConstraint(currentCoefficients, constant))

          // tidy up
          currentClass = None
          currentCoefficients = Map()

        case _ =>
          throw new ParsingException("Line "+lineNr+": unexpected line: '"+line+"'")
      }

    }

    constraints
  }

    def getIRI(str: String): IRI =
      prefixManager.getIRI(str)


  }
