package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Numbers
import org.semanticweb.owlapi.model.OWLDataProperty
import org.apache.commons.math3.fraction.BigFraction

/**
 * This is another concrete domain in which every constraint is a linear equation
 */

case class LinearConstraint(lhs: Map[OWLDataProperty, BigFraction], rhs: BigFraction) extends CDConstraint {
  /** whether this is def(x), represented as 0 * x = 0 */
  def isDef: Boolean = lhs.size == 1 && Numbers.format(lhs.values.head) == "0" && Numbers.format(rhs) == "0"

  override def toString(): String = {
    if (isDef)
      return "def(" + lhs.keys.head + ")"
    val l = lhs.keys.toList.sortWith(_.toString < _.toString).map{key => format(key, lhs(key))}
      .map(_.replaceAll("\\s","")).mkString(" + ").replaceAll("\\+ -","\u2212 ")//Works better with GraphViz
    val r = " = " + Numbers.format(rhs)
    if (l.nonEmpty) l + r else 0 + r
  }

  override def getOWLDataProperties: Set[OWLDataProperty] = lhs.keys.toSet

  /** each variable, its factor unless it is 1 or -1, the n - 1 signs between the terms, = and the constant */
  override def size: Int =
    if (isDef) 2
    else if (lhs.isEmpty) 3
    else lhs.values.map(v => if (Set("1", "-1").contains(Numbers.format(v))) 1 else 2).sum + lhs.size - 1 + 2

  override def isInconsistent: Boolean = lhs.values.forall(_.equals(BigFraction.ZERO)) && !rhs.equals(BigFraction.ZERO)

  def format(prop: OWLDataProperty, value: BigFraction): String = {
    Numbers.format(value) match {
      case "1" => prop.toString
      case "-1" => "-" + prop.toString
      case number => number + "*" + prop
    }
  }

}

object LinearConstraint {
  /** 0 = 1 */
  val contradiction: LinearConstraint = LinearConstraint(Map.empty, BigFraction.ONE)
}
