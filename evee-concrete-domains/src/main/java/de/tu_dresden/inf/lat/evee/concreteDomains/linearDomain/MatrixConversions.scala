package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import org.apache.commons.math3.FieldElement
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayFieldVector, ArrayRealVector, FieldVector, RealMatrix, RealVector}
import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

object MatrixConversions {

  /**
   * the column of each variable of the constraints, which is the order of elimination: first the variables in the
   * given order, then the others sorted by IRI
   */
  def getKeyMap[T] (constraints: Iterable[LinearConstraint[T]], order: Seq[OWLDataProperty] = Seq()): Map[OWLDataProperty, Integer] = {
    val variables = constraints.flatMap(_.lhs.keys).toSet
    val ordered = order.filter(variables).distinct ++ (variables -- order).toSeq.sortBy(_.getIRI.toString)
    ordered.zipWithIndex.map { case (variable, column) => variable -> Integer.valueOf(column) }.toMap
  }

  /**
   * convert a constraint to a row vector with arbitrary precision, where the
   * constant is the last entry
   */
  def toBigRow[T <: FieldElement[T]] (constraint: LinearConstraint[T], keyMap: Map[OWLDataProperty, Integer]): FieldVector[T] = {
      val columns = keyMap.size + 1
      val row = new ArrayFieldVector[T](constraint.rhs.getField, columns)
      constraint.lhs.keys.foreach(property =>
        row.setEntry(keyMap(property), constraint.lhs(property))
      )
      row.setEntry(columns - 1, constraint.rhs)
      row
  }

  /**
   * convert a row vector back to a linear constraint, omitting zero entries
   */
  def toLinearConstraint[T <: FieldElement[T]] (row: FieldVector[T], keyMap: Map[OWLDataProperty, Integer]) = {
    val rhs = row.getEntry(row.getDimension-1)
    val zero = rhs.getField.getZero
    val lhs = keyMap.mapValues(row.getEntry(_)).
      filterNot{ case (_,v) => v.equals(zero) }
    LinearConstraint(lhs, rhs)
  }

  /**
   * converts constraints to matrix, where the constants are the last column
   */
  @deprecated
  def toMatrix(constraints: Iterable[LinearConstraint[Double]]): RealMatrix = {
    val constraintKeyMap = getKeyMap(constraints)

    val columns = constraintKeyMap.keys.size +1
    val rows = constraints.size

    val sortedConstraints = constraints.toList

    val matrix = new Array2DRowRealMatrix(rows,columns)
    val constants = new ArrayRealVector(rows)

    for(row <- 0 to rows-1) {
      val constraint = sortedConstraints(row)
      constraint.lhs.keys.foreach(property =>
        matrix.setEntry(row, constraintKeyMap(property), constraint.lhs(property))
      )
      matrix.setEntry(row, columns-1, constraint.rhs)
    }
    matrix
  }


  @deprecated
  def toMatrixEquationSystem(constraints: Iterable[LinearConstraint[Double]]): (RealMatrix,RealVector) = {
    val constraintKeyMap = getKeyMap(constraints)

    val columns = constraintKeyMap.keys.size + 1
    val rows = constraints.size

    val sortedConstraints = constraints.toList

    val matrix = new Array2DRowRealMatrix(rows,columns)
    val constants = new ArrayRealVector(rows)

    for(row <- 0 to rows-1) {
      val constraint = sortedConstraints(row)
      constraint.lhs.keys.foreach(property =>
        matrix.setEntry(row, constraintKeyMap(property), constraint.lhs(property))
      )
      constants.setEntry(row, constraint.rhs)
    }
    (matrix,constants)
  }
}
