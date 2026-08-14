package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasonerWithStatistics
import  de.tu_dresden.inf.lat.evee.data.cdGeneral.ReasoningTaskTracker
import  de.tu_dresden.inf.lat.evee.data.names.ConcreteDomainName

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

import de.tu_dresden.lat.z3.Z3Checker

import org.apache.commons.math3.fraction.BigFraction
import org.apache.commons.math3.linear.FieldVector
import org.semanticweb.owlapi.model.OWLDataProperty

import scala.collection.JavaConverters
import scala.collection.mutable.ListBuffer

class GaussianEliminationReasoner extends CDReasonerWithStatistics[LinearConstraint[BigFraction]] {//with CDProver[LinearConstraint[BigFraction]]
  private var numVars: Int = _
  // needed to translate between LinearConstraints and rows in both directions
  // (e.g., for proofs)
  private var keyMap: Map[OWLDataProperty, Integer] = _
  private var rows: Iterable[FieldVector[BigFraction]] = _
  private var targetRows: Iterable[FieldVector[BigFraction]] = _
  private var pivotRowLog: List[FieldVector[BigFraction]] = _
  private var considerAllPivotRows: Boolean = false

  def considerAllPivotRows(value: Boolean): Unit = {
    considerAllPivotRows = value
  }

  override def consistent(predicates: Iterable[LinearConstraint[BigFraction]]): Boolean = {
    consistent(predicates, Option.empty)
  }

  def consistent(predicates: Iterable[LinearConstraint[BigFraction]], tracker: Option[ImplicationTracker]): Boolean = {
    val start = System.nanoTime()
    val res = satisfiable(predicates, Iterable.empty, tracker)
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Consistency"
    rTT.cDReasoningTime = time

    if (Tools.isZ3CheckEnabled)
      Z3Checker.checkConsistency(res, JavaConverters.asJavaCollection(predicates), ConcreteDomainName
        .LinearConstraints, rTT)

    res
  }

  override def implies(premises: Iterable[LinearConstraint[BigFraction]], conclusion: LinearConstraint[BigFraction]): Boolean = {
    implies(premises, conclusion, Option.empty)
  }

  def implies(premises: Iterable[LinearConstraint[BigFraction]], conclusion: LinearConstraint[BigFraction], tracker: Option[ImplicationTracker]): Boolean = {
    val start = System.nanoTime()
    val res = !satisfiable(premises, Iterable(conclusion), tracker)
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Implication"
    rTT.cDReasoningTime = time

    if (Tools.isZ3CheckEnabled)
      Z3Checker.checkImplication(res, JavaConverters.asJavaCollection(premises), conclusion, ConcreteDomainName
        .LinearConstraints, rTT)

    res
  }

  def satisfiable(positive: Iterable[LinearConstraint[BigFraction]], negative: Iterable[LinearConstraint[BigFraction]], tracker: Option[ImplicationTracker] = None) : Boolean = {
    collectStatistics(positive, negative)
    gaussianElimination(positive, negative, tracker)
    if (!rows.forall(_.getEntry(numVars).equals(BigFraction.ZERO))) {
      // one of the positive constraints was reduced to 0 = c with non-zero c
      return false
    }
    for (neg <- targetRows) {
      if ((0 to numVars).forall(neg.getEntry(_).equals(BigFraction.ZERO))) {
        // one of the negative constraints was reduced to 0 ≠ 0
        return false
      }
    }
    true
  }

  def gaussianElimination(predicates: Iterable[LinearConstraint[BigFraction]], targets: Iterable[LinearConstraint[BigFraction]], tracker: Option[ImplicationTracker]):
    (Map[OWLDataProperty, Integer], Iterable[FieldVector[BigFraction]], Iterable[FieldVector[BigFraction]]) = {

    keyMap = MatrixConversions.getKeyMap(predicates ++ targets)
    numVars = keyMap.size
    rows = predicates.map(MatrixConversions.toBigRow(_, keyMap))
    targetRows = targets.map(MatrixConversions.toBigRow(_, keyMap))
    pivotRowLog = List[FieldVector[BigFraction]]()

    if(tracker.isDefined)
      tracker.get.setVarsMap(keyMap);

    //println("Variables: " + numVars)
    //println("KeyMap: " + keyMap)

    var i = 0
    while (i < numVars) {
      //println("i = " + i)
      //println("Rows: " + rows.size)
//      for (row <- rows)
//        MatrixConversions.printRow(row)
      //println("Target: " + targetRows.size)
//      for (targetRow <- targetRows)
//        MatrixConversions.printRow(targetRow)

      // Find the next column with non-zero entries and classify the rows accordingly.
      var (pivotRows, rowsWithZeroEntries, newI) = findFirstNonZero(rows, i, false)
      i = newI
      var (targetRowsWithNonZeroEntries, targetRowsWithZeroEntries, _) = findFirstNonZero(targetRows, i, true)
      //println("i' = " + i)
      if (i < numVars) {
        // There must be at least one pivot row since i < numVars
        assert(pivotRows.nonEmpty)
        // Rows with 0 in the i-th column remain unchanged for now.
        rows = rowsWithZeroEntries
        targetRows = targetRowsWithZeroEntries
        do {
          var pivotRow = pivotRows.head
          if (!considerAllPivotRows)
            pivotRowLog = pivotRow :: pivotRowLog
//          print("Pivot: ")
//          MatrixConversions.printRow(pivotRow)

          // Reduce all the remaining possible pivot rows w.r.t. the current one.
          pivotRows = pivotRows.tail
          rows ++= pivotRows.map(reduce(_, pivotRow, i, tracker))

          // Reduce all target rows w.r.t. the current pivot row.
          targetRows ++= targetRowsWithNonZeroEntries.map(reduce(_, pivotRow, i, tracker))

          //Added this to make the loop break when there is a timeOut
          if(Thread.currentThread().isInterrupted)
            pivotRows = List()

        } while (pivotRows.nonEmpty && considerAllPivotRows) // If we don't want to track all inferences, one pivot row is enough.
        i = i+1
      }
    }

    assert(rows.forall(row => {
      for (j <- 0 until numVars) {
        if (!row.getEntry(j).equals(BigFraction.ZERO))
          false
      }
      true
    }))

    (keyMap, pivotRowLog, targetRows)
  }

  def findFirstNonZero(rows: Iterable[FieldVector[BigFraction]], i: Int, fixColumn: Boolean): (Iterable[FieldVector[BigFraction]], Iterable[FieldVector[BigFraction]], Int) = {
    var pivotRows = ListBuffer[FieldVector[BigFraction]]()
    for (j <- i until numVars) {
      for (row <- rows) {
        if (!row.getEntry(j).equals(BigFraction.ZERO)) {
          pivotRows += row
        }
      }
      if (pivotRows.nonEmpty) {
        val ret = pivotRows.toSet
        val diff = rows.filterNot(pivotRows.contains(_)).toSet // rows.toSet.diff(ret) does not work! (if rows is a HashTrieSet??)
        return (ret, diff, j)
      }
      if (fixColumn)
        return (Iterable.empty, rows, j)
    }
    (Iterable.empty, rows, numVars)
  }

  def reduce(row: FieldVector[BigFraction], pivotRow: FieldVector[BigFraction], i: Int,  tracker: Option[ImplicationTracker]): FieldVector[BigFraction] = {
//    print("Reducing ")
//    MatrixConversions.printRow(row)

    val factor = row.getEntry(i).negate.divide(pivotRow.getEntry(i)) // f = -y_i/x_i
//    println("Factor: " + factor.doubleValue)
    if (factor.equals(BigFraction.ZERO)) {
      return row
    }

    val sum = row.add(pivotRow.mapMultiply(factor)) // y' = y + f*x
//    print("Reduced: ")
//    MatrixConversions.printRow(sum)

    assert(sum.getEntry(i).equals(BigFraction.ZERO))

    val (normalized, factor2) = normalize(sum, i)

    if (tracker.isDefined)
      tracker.get.addInference(normalized, pivotRow, row, factor.multiply(factor2), factor2)

    assert(normalized.getEntry(i).equals(BigFraction.ZERO))

    normalized
  }

  def normalize(row: FieldVector[BigFraction], i: Int): (FieldVector[BigFraction], BigFraction) = {
    for (j <- i until numVars) {
      if (!row.getEntry(j).equals(BigFraction.ZERO)) {
        val factor = row.getEntry(j).reciprocal()
        return (row.mapMultiply(factor), factor)
      }
    }
    return (row, BigFraction.ONE)
  }
}