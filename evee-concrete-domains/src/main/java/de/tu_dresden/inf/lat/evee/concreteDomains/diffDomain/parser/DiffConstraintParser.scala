package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.parser

import de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain.{DiffConstraint, DiffEqual, DiffGreaterThan, DiffSum}
import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.CDParser

import org.semanticweb.owlapi.model.{IRI, OWLClass, OWLOntology}
import org.semanticweb.owlapi.util.DefaultPrefixManager

import java.io.{BufferedReader, File, FileReader}

/**
 * Parser for the second concrete domain
 *
 * TODO some of the predicates are actually supported by OWL, and may thus appear int he ontology in the usual way.
 * TODO we should thus be able to use them.
 */
class DiffConstraintParser(ontology: OWLOntology) extends CDParser[DiffConstraint] {

  val numberParser: (String => Double) = _.toDouble

  val COMMENT = "#"
  val PREFIX = "PREFIX"

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory

  val prefixManager = new DefaultPrefixManager()
  //val manchesterParser =
  //  new ManchesterOWLSyntaxParserImpl(new OWLAPIConfigProvider, factory)

  override def parse(file: File): Map[OWLClass, DiffConstraint] = {
    parse(new BufferedReader(new FileReader(file)))
  }

  def parse(reader: BufferedReader): Map[OWLClass, DiffConstraint] = {
    parse(reader.lines())
  }

  val reComment = raw"#.*".r
  val rePrefix = (PREFIX+raw"\s*(\w*):\s+<?([^>]+)>?").r

  val strReIRI = raw"(<[^>]+>|[^\s]+)"
  val strReNumber = raw"(-?[0-9]+(?:\.[0-9]*)?)"

  val reEqual =   (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*=\s*" + strReNumber).r
  val reGreater = (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*>\s*" + strReNumber).r
  val reDiff =    (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*\+\s*" + strReNumber + raw"\s*=\s*" + strReIRI).r

  def parse(lines: java.util.stream.Stream[String]): Map[OWLClass,DiffConstraint] = {

    var constraints = Map[OWLClass, DiffConstraint]()

    var lineNr = 0

    lines.forEach { line =>

      lineNr += 1

      line.trim match {

        case "" => ;

        case reComment(_*) => ;

        case rePrefix(prefix, iri) =>
          prefixManager.setPrefix(prefix, iri)

        case reEqual(classStr, propertyStr, constantStr) =>
          val clazz = factory.getOWLClass(getIRI(classStr))
          val property = factory.getOWLDataProperty(getIRI(propertyStr))
          val constant = numberParser(constantStr)
          //println(clazz, DiffEqual(property,constant))
          constraints += ((clazz, DiffEqual(property,constant)))

        case reGreater(classStr, propertyStr, constantStr) =>
          val clazz = factory.getOWLClass(getIRI(classStr))
          val property = factory.getOWLDataProperty(getIRI(propertyStr))
          val constant = numberParser(constantStr)
          //println((clazz, DiffGreaterThan(property, constant)))
          constraints += ((clazz, DiffGreaterThan(property, constant)))

        case reDiff(classString, property1Str, diffStr, property2Str) =>
          val clazz = factory.getOWLClass(getIRI(classString))
          val property1 = factory.getOWLDataProperty(getIRI(property1Str))
          val diff = numberParser(diffStr)
          val property2 = factory.getOWLDataProperty(getIRI(property2Str))
          //println((clazz, DiffSum(property1, diff, property2)))
          constraints += ((clazz, DiffSum(property1, diff, property2)))

        case _ =>
          throw new ParsingException("Line "+lineNr+": unexpected line: '"+line+"'")
      }

    }

    constraints
  }

  def getIRI(str: String): IRI =
    prefixManager.getIRI(str)


}
