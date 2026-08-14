package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.exceptions.ParsingException
import de.tu_dresden.inf.lat.evee.concreteDomains.CDParser

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.FreshClasses

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.model.{IRI, OWLAxiom, OWLClass, OWLDataHasValue, OWLDataProperty, OWLDataPropertyAssertionAxiom, OWLOntology}
import org.semanticweb.owlapi.util.DefaultPrefixManager

import java.io.{BufferedReader, File, FileReader}
import scala.collection.JavaConverters.setAsJavaSetConverter
import scala.reflect.internal.util.FreshNameCreator

class MultConstraintParser(ontology: OWLOntology) extends CDParser[MultConstraint] {

  val numberParser: (String => Double) = _.toDouble

  val COMMENT = "#"
  val PREFIX = "PREFIX"

  val manager = ontology.getOWLOntologyManager
  val factory = manager.getOWLDataFactory

  val prefixManager = new DefaultPrefixManager()
  //val manchesterParser =
  //  new ManchesterOWLSyntaxParserImpl(new OWLAPIConfigProvider, factory)

  def parse(file: File): Map[OWLClass, MultConstraint] = {
    parse(new BufferedReader(new FileReader(file)))
  }

  def cdExpressionsFromOntology(): Map[OWLClass, MultConstraint] = {

    println("Looking for cds in ontology...")

    val fresh = new FreshClasses(ontology, "_CD")

    var axioms2Add = Set[OWLAxiom]()

    var result = Map[OWLClass, MultConstraint]()

    ontology.getAxioms(Imports.INCLUDED).forEach{_ match {
      case ax: OWLDataPropertyAssertionAxiom if ax.getProperty().isInstanceOf[OWLDataProperty] =>
        println("property assertion: "+ax)
        val ind = ax.getSubject
        val prp = ax.getProperty
        val value = ax.getObject
        if(value.isFloat || value.isDouble || value.isInteger) {
          val cl = fresh.next()
          val axiom = factory.getOWLClassAssertionAxiom(cl, ind)
          axioms2Add += (axiom)
          val number =
            if(value.isFloat) {
              new BigFraction(value.parseFloat())
            } else if(value.isDouble)
              new BigFraction(value.parseDouble())
            else {
              new BigFraction(value.parseInteger())
            }
          result += ((cl, new MultEqual(prp.asInstanceOf[OWLDataProperty], number)))
        }
      case ax: OWLDataPropertyAssertionAxiom => println("ignored property assertion: "+ax)
      println(ax.getProperty)
      println(ax.getProperty.getClass)
      case _ => ;
    }
    }

    ontology.getNestedClassExpressions().forEach(_ match {
      case exp: OWLDataHasValue if exp.getProperty().isInstanceOf[OWLDataProperty] =>
        println("property expression: "+exp)
        val filler = exp.getFiller()
        if(filler.isDouble || filler.isFloat || filler.isInteger) {
          val cl = fresh.next()
          val axiom = factory.getOWLEquivalentClassesAxiom(cl,exp)
          axioms2Add += (axiom)

          val number =
            if(filler.isFloat) {
              new BigFraction(filler.parseFloat())
            } else if(filler.isDouble)
              new BigFraction(filler.parseDouble())
            else {
              new BigFraction(filler.parseInteger())
            }

          result += ((cl, new MultEqual(exp.getProperty().asInstanceOf[OWLDataProperty],
            new BigFraction(filler.parseDouble()))))
        }
      case _ => ;
    })

    axioms2Add.foreach(println)
    println()

    ontology.getOWLOntologyManager.addAxioms(ontology,axioms2Add.asJava)

    ontology.getAxioms().forEach(println)

    result
  }


  def parse(reader: BufferedReader): Map[OWLClass, MultConstraint] = {
    parse(reader.lines()) ++ cdExpressionsFromOntology()
  }

  val reComment = raw"#.*".r
  val rePrefix = (PREFIX+raw"\s*(\w*):\s+<?([^>]+)>?").r

  val strReIRI = raw"(<[^>]+>|[^\s]+)"
  val strReNumber = raw"(-?[0-9]+(?:\.[0-9]*)?)"

  val reEqual =   (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*=\s*" + strReNumber).r
  val reLess = (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*<\s*" + strReNumber).r
  val reMult =    (strReIRI + raw"\s*:\s*" + strReIRI + raw"\s*\*\s*" + strReNumber + raw"\s*=\s*" + strReIRI).r

  def parse(lines: java.util.stream.Stream[String]): Map[OWLClass,MultConstraint] = {

    var constraints = Map[OWLClass, MultConstraint]()

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
          //println(clazz, CD2Equal(property,constant))
          constraints += ((clazz, MultEqual(property,new BigFraction(constant))))

        case reLess(classStr, propertyStr, constantStr) =>
          val clazz = factory.getOWLClass(getIRI(classStr))
          val property = factory.getOWLDataProperty(getIRI(propertyStr))
          val constant = numberParser(constantStr)
          //println((clazz, CD2GreaterThan(property, constant)))
          constraints += ((clazz, MultLessThan(property, new BigFraction(constant))))

        case reMult(classString, property1Str, factorStr, property2Str) =>
          val clazz = factory.getOWLClass(getIRI(classString))
          val property1 = factory.getOWLDataProperty(getIRI(property1Str))
          val factor = numberParser(factorStr)
          val property2 = factory.getOWLDataProperty(getIRI(property2Str))
          //println((clazz, CD2Sum(property1, diff, property2)))
          constraints += ((clazz, Multiplication(property1, new BigFraction(factor), property2)))

        case _ =>
          throw new ParsingException("Line "+lineNr+": unexpected line: '"+line+"'")
      }

    }

    constraints
  }

  def getIRI(str: String): IRI =
    prefixManager.getIRI(str)

}
