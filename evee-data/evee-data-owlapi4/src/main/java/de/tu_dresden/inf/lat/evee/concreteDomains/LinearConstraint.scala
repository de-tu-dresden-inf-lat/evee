package de.tu_dresden.inf.lat.evee.concreteDomains

import org.semanticweb.owlapi.model.OWLDataProperty
import org.apache.commons.math3.fraction.BigFraction

case class LinearConstraint[T](lhs: Map[OWLDataProperty, T], rhs: T) extends CDConstraint {
  override def toString() = {
    val l = lhs.keys.toList.sortWith(_.toString < _.toString).map{key => format(key, lhs(key))}
      .map(_.replaceAll("\\s","")).mkString(" + ").replaceAll("\\+ -","\u2212 ")//Works better with GraphViz
    val r = " = " + rhs
    if (l.nonEmpty) l + r else 0 + r
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = lhs.keys.toSet

  // TODO find a better solution -> fix T to BigFraction?
  override def isInconsistent: Boolean = {
    val ZERO = rhs match {
      case _: Double => 0d
      case _: BigFraction => BigFraction.ZERO
    }
    lhs.values.forall(_.equals(ZERO)) && !rhs.equals(ZERO)
  }

  def getType: Class[_ <: T] = {
    rhs.getClass
  }

  def format(prop: OWLDataProperty, value: T): String = {
    value.toString.trim match {
      case "1" => prop.toString
      case "-1" => "-" + prop.toString
      case _ => value + "*" + prop
    }
  }

  /*override def hashCode(): Int = {
    lhs.toList.map(pair => pair._1.toString().hashCode()+pair._2.hashCode()).sum+rhs.hashCode()
  }*/
}