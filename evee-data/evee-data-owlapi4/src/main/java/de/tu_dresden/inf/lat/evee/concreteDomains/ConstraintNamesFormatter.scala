package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model._

import scala.collection.JavaConverters.{collectionAsScalaIterableConverter, setAsJavaSetConverter}

/**
 * @author Christian Alrabbaa
 */

object ConstraintNamesFormatter {

  private val factory:OWLDataFactory = OWLManager.getOWLDataFactory
  private val innerNamesRegx = "(<[^<>]+>)".r
  private val cDConstraintNameRegx = "(<\\[[^\\[\\]]+]>)".r

  def format(axiom: OWLAxiom):String = {
    if(axiom.isOfType(AxiomType.SUBCLASS_OF))
      return formatSubClassOfAxiom(axiom.asInstanceOf[OWLSubClassOfAxiom])
    if(axiom.isOfType(AxiomType.EQUIVALENT_CLASSES))
      return formatEquivalentClassesAxiom(axiom.asInstanceOf[OWLEquivalentClassesAxiom])
    //return it without formatting to remember to implement the rest
    axiom.toString
  }

  private def formatEquivalentClassesAxiom(axiom: OWLEquivalentClassesAxiom): String = {
    SimpleOWLFormatter.format(
      factory.getOWLEquivalentClassesAxiom(
      axiom.getClassExpressions.asScala.map(formatClassExpression).toSet.asJava))
  }

  private def formatSubClassOfAxiom(axiom: OWLSubClassOfAxiom): String = {
    SimpleOWLFormatter.format(
      factory.getOWLSubClassOfAxiom(
        formatClassExpression(axiom.getSubClass),
        formatClassExpression(axiom.getSuperClass)))
  }

  def formatClassExpression(clsExp: OWLClassExpression): OWLClassExpression = {
    clsExp match {
      case cls:OWLClass =>
        formatClassName(cls)

      case exRes: OWLObjectSomeValuesFrom =>
        factory.getOWLObjectSomeValuesFrom(exRes.getProperty, formatClassExpression(exRes.getFiller))

      case conj: OWLObjectIntersectionOf =>
        val conjuncts = conj.asConjunctSet().asScala.map(formatClassExpression).toSet
        factory.getOWLObjectIntersectionOf(conjuncts.asJava)

      case _ => clsExp
    }
  }

  private def formatClassName(cls: OWLClass): OWLClassExpression = {
    cls.toString match {
      case cDConstraintNameRegx(name) =>
        var res = name
        for (x <- innerNamesRegx.findAllIn(res)) {
          res = res.replace(x, SimpleOWLFormatter.format(
            factory.getOWLClass(
              IRI.create(
                x.substring(1, x.length - 1)))))
        }
        //Added "#" to get around SimpleOWLFormatter.format
        factory.getOWLClass(IRI.create("#"+res.substring(1,res.length-1)))
      case _ =>cls
      }
  }
}
