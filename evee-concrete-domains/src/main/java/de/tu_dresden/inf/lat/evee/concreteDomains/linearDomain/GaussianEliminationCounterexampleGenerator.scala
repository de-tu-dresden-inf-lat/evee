package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.LinearConstraint
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException

import de.tu_dresden.lat.concreteDomains.CDCounterexampleGenerator

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

import org.apache.commons.math3.fraction.BigFraction
import org.apache.commons.math3.linear.FieldVector
import org.semanticweb.owlapi.model.OWLDataProperty

import java.util
import java.util.stream.Stream
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

class GaussianEliminationCounterexampleGenerator
  (reasoner: GaussianEliminationReasoner = new GaussianEliminationReasoner())
  extends CDCounterexampleGenerator[LinearConstraint[BigFraction], BigFraction] {

  private var positiveConstraints: Iterable[LinearConstraint[BigFraction]] = _
  private var negativeConstraints: Iterable[LinearConstraint[BigFraction]] = _
  private var relevantDataProperties: Set[OWLDataProperty] = _

  override def setObservation(observation: Iterable[LinearConstraint[BigFraction]]): Unit = {
    negativeConstraints = observation
  }

  override def setSignature(signature: util.Collection[OWLDataProperty]): Unit = {
    relevantDataProperties = signature.toSet
  }

  override def setOntology(ontology: Iterable[LinearConstraint[BigFraction]]): Unit = {
    positiveConstraints = ontology
  }

  override def generateExplanations(): Stream[Map[OWLDataProperty, BigFraction]] = {
    // We assume that all positive constraints are consistent with each other,
    // and that they don't imply the negative constraints, so there actually
    // exists a solution.

    reasoner.considerAllPivotRows(false)
    val (keyMap, pivotRows, targetRows) = reasoner.gaussianElimination(positiveConstraints, negativeConstraints, None)

    println("Finding solution ...")

    val partialSolution = solveNegativePart(targetRows)
    println("Current partial solution: " + partialSolution)

    val solution = solvePositivePart(pivotRows, partialSolution)
    println("Full solution: " + solution)
    println("Positive constraints:")
    positiveConstraints.foreach(println)
    println("Negative constraints:")
    negativeConstraints.foreach(println)

    // translate solution mapping to OWLDataProperties, possibly filling in missing values as BigFraction.ZERO,
    // and filter by the given signature
    val assignment = keyMap.mapValues(varIndex => solution.getOrElse(varIndex, BigFraction.ZERO))

    assertionTest(assignment)

    val map = assignment.filterKeys(prop => relevantDataProperties.contains(prop))
    println(map)

    // TODO return multiple alternative solutions?
    Stream.of[Map[OWLDataProperty, BigFraction]](map)
  }

  private def satisfies(constraint: LinearConstraint[BigFraction], assignment: Map[OWLDataProperty, BigFraction]): Boolean = {
    val sum = constraint.lhs
      .map { case (prop, factor) => factor.multiply(assignment(prop)) }
      .reduceOption((a, b) => a.add(b))
      .getOrElse(BigFraction.ZERO)
    sum.equals(constraint.rhs)
  }

  private def solveNegativePart(targetRows: Iterable[FieldVector[BigFraction]]): Map[Int, BigFraction] = {
    var remainingRows = targetRows
    var solution = Map[Int, BigFraction]()

    while (remainingRows.nonEmpty) {
      val numVars = remainingRows.head.getDimension-1
      var (varIndex, value) = (0, BigFraction.ZERO)

      // Find out which rows contains only a single variable.
      println("Find next valuation given reduced negative constraints:")
      for (targetRow <- remainingRows) {
        MatrixConversions.printRow(targetRow)
      }
      val constraints = findUniqueConstrainedVariables(remainingRows)
      // Select such a variable.
      val i = constraints.values.find(_.isDefined)
      println("Selected unique constrained variable: " + i)
      if (i.isDefined) {
        varIndex = i.get.get
        // If such a variable x with index i exists, get all constraints of the
        // form a*x = b.
        val constraintsForI = remainingRows.filter(constraints(_).equals(i.get))
        // Collect all values b/a.
        val disallowedValues = constraintsForI.map(v => v.getEntry(v.getDimension-1).divide(v.getEntry(i.get.get)))
        // Select smallest non-negative integer that is not disallowed.
        value = findFirstAllowedValue(disallowedValues)
        // Remove all constraints for x (which are satisfied after setting x to 'value').
        remainingRows = remainingRows.filter(row => !constraintsForI.exists(_.equals(row)))
      } else {
        // If there is no such variable, choose an arbitrary non-eliminated
        // variable to set to BigFraction.ZERO.
        val (_, _, index) = reasoner.findFirstNonZero(remainingRows, 0, false)
        varIndex = index
        value = BigFraction.ZERO
      }

      if (varIndex == numVars) {
        // No variable has been found -> all remaining target rows are trivially satisfied
        remainingRows = Iterable.empty
      } else {
        println("Adding " + varIndex + " -> " + value + " to solution and updating negative constraints")
        // Add this to the solution mapping.
        solution += (varIndex -> value)
        // Insert the new value for x into all remaining rows.
        remainingRows.foreach(insert(_, varIndex, value))
      }
    }

    solution
  }

  private def solvePositivePart(pivotRows: Iterable[FieldVector[BigFraction]], partialSolution: Map[Int, BigFraction]): Map[Int, BigFraction] = {
    var solution = partialSolution

    for (pivotRow <- pivotRows) {
      val numVars = pivotRow.getDimension-1
      println("Extending solution to pivot row:")
      MatrixConversions.printRow(pivotRow)
      // find all non-zero entries whose variables have no assignment in 'solution' yet
      var freeVariables = List.range(0, numVars).filter(!pivotRow.getEntry(_).equals(BigFraction.ZERO)).filter(!solution.contains(_))
      println("Free variables: " + freeVariables)
      if (freeVariables.size > 1) {
        // set all but one of the free variables to 0
        for (varIndex <- freeVariables.tail) {
          println("Adding " + varIndex + " -> 0 to solution")
          solution += (varIndex -> BigFraction.ZERO)
        }
      }
      if (freeVariables.nonEmpty) {
        // determine the value of the remaining free variable
        // (if there is no free variable, we can assume that the equation is already solved)
        val varIndex = freeVariables.head
        val difference = solution
          .map { case (index, value) => value.multiply(pivotRow.getEntry(index)) }
          .fold(BigFraction.ZERO)((a, b) => a.add(b))
        val newValue = pivotRow
          .getEntry(numVars)
          .subtract(difference)
          .divide(pivotRow.getEntry(varIndex))
        println("Adding " + varIndex + " -> " + newValue + " to solution")
        solution += (varIndex -> newValue)
      }
    }

    solution
  }

  private def findFirstAllowedValue(disallowedValues: Iterable[BigFraction]): BigFraction = {
    for (v <- 0 to disallowedValues.size) {
      val f = new BigFraction(v)
      if (!disallowedValues.exists(_.equals(f)))
        return f
    }
    assert(assertion = false, "Could not find a value between 0 and " + disallowedValues.size + " while avoiding " + disallowedValues)
    BigFraction.ZERO
  }

  private def findUniqueConstrainedVariables(rows: Iterable[FieldVector[BigFraction]]): Map[FieldVector[BigFraction], Option[Int]] = {
    rows.map(row => row -> findUniqueConstrainedVariable(row)).toMap
  }

  private def findUniqueConstrainedVariable(row: FieldVector[BigFraction]): Option[Int] = {
    println("Finding unique constrained variable in:")
    MatrixConversions.printRow(row)
    var res: Option[Int] = None
    val dim = row.getDimension
    for (i <- 0 until row.getDimension-1) {
      if (!row.getEntry(i).equals(BigFraction.ZERO)) {
        if (res.isDefined) {
          println("Another variable with non-zero coefficient: " + i)
          return None
        } else {
          println("Variable with non-zero coefficient: " + i)
          res = Some(i)
        }
      }
    }
    res
  }

  private def insert(row: FieldVector[BigFraction], varIndex: Int, value: BigFraction): Unit = {
    val newResult = row.getEntry(row.getDimension-1).subtract(value.multiply(row.getEntry(varIndex)))
    row.setEntry(varIndex, BigFraction.ZERO)
    row.setEntry(row.getDimension-1, newResult)
  }

  override def supportsExplanation(): Boolean = true

  private def assertionTest(assignment: Map[OWLDataProperty, BigFraction]) = {
    if (Tools.areAssertionsActivated()) {
      if (!positiveConstraints.forall(satisfies(_, assignment)))
        throw new ModelGenerationException("Some positive constraints are not satisfied by the assignment!")
      if (negativeConstraints.exists(satisfies(_, assignment)))
        throw new ModelGenerationException("Some negative constraint is satisfied the by the assignment!")
    }
  }

  override def successful(): Boolean =
    true // TODO something more reasonable

  override def ignoresPartsOfOntology(): Boolean = ???
}
