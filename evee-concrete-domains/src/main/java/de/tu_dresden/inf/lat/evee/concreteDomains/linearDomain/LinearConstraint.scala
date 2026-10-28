package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import org.semanticweb.owlapi.model.OWLDataProperty
import org.apache.commons.math3.fraction.BigFraction

/**
 * This is another concrete domain in which every constraint is a linear equation
 */

case class LinearConstraint[T](lhs: Map[OWLDataProperty, T], rhs: T) extends CDConstraint {
  /** whether this is def(x), represented as 0 * x = 0 */
  def isDef: Boolean = lhs.size == 1 && formatNumber(lhs.values.head) == "0" && formatNumber(rhs) == "0"

  override def toString(): String = {
    if (isDef)
      return "def(" + lhs.keys.head + ")"
    val l = lhs.keys.toList.sortWith(_.toString < _.toString).map{key => format(key, lhs(key))}
      .map(_.replaceAll("\\s","")).mkString(" + ").replaceAll("\\+ -","\u2212 ")//Works better with GraphViz
    val r = " = " + formatNumber(rhs)
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
    formatNumber(value) match {
      case "1" => prop.toString
      case "-1" => "-" + prop.toString
      case number => number + "*" + prop
    }
  }

  private def formatNumber(value: T): String = value match {
    case fraction: BigFraction => Numbers.format(fraction)
    case other => other.toString.replaceAll("\\s", "")
  }

}