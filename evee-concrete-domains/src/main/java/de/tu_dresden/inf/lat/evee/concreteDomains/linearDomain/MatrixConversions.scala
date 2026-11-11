package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import org.apache.commons.math3.fraction.{BigFraction, BigFractionField}
import org.apache.commons.math3.linear.{ArrayFieldVector, FieldVector}
import org.semanticweb.owlapi.model.OWLDataProperty

object MatrixConversions {

  /**
   * the column of each variable of the constraints, which is the order of elimination: first the variables in the
   * given order, then the others sorted by IRI
   */
  def getKeyMap(constraints: Iterable[LinearConstraint], order: Seq[OWLDataProperty] = Seq()): Map[OWLDataProperty, Integer] = {
    val variables = constraints.flatMap(_.lhs.keys).toSet
    val ordered = order.filter(variables).distinct ++ (variables -- order).toSeq.sortBy(_.getIRI.toString)
    ordered.zipWithIndex.map { case (variable, column) => variable -> Integer.valueOf(column) }.toMap
  }

  /**
   * convert a constraint to a row vector with arbitrary precision, where the
   * constant is the last entry
   */
  def toBigRow(constraint: LinearConstraint, keyMap: Map[OWLDataProperty, Integer]): FieldVector[BigFraction] = {
      val columns = keyMap.size + 1
      val row = new ArrayFieldVector[BigFraction](BigFractionField.getInstance, columns)
      constraint.lhs.keys.foreach(property =>
        row.setEntry(keyMap(property), constraint.lhs(property))
      )
      row.setEntry(columns - 1, constraint.rhs)
      row
  }

  /**
   * convert a row vector back to a linear constraint, omitting zero entries
   */
  def toLinearConstraint(row: FieldVector[BigFraction], keyMap: Map[OWLDataProperty, Integer]): LinearConstraint = {
    val rhs = row.getEntry(row.getDimension-1)
    val lhs = keyMap.mapValues(row.getEntry(_)).
      filterNot{ case (_,v) => v.equals(BigFraction.ZERO) }
    LinearConstraint(lhs, rhs)
  }
}
