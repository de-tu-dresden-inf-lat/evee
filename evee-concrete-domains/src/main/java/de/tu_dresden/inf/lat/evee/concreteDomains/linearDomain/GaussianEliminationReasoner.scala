package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasonerWithStatistics
import de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral.ReasoningTaskTracker
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

import org.apache.commons.math3.fraction.BigFraction
import org.apache.commons.math3.linear.FieldVector
import org.semanticweb.owlapi.model.OWLDataProperty

import java.math.BigInteger
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
  /** the columns of the variables sorted by IRI, which fix the sign of the canonical form */
  private var columnsByIRI: Seq[Int] = _
  /** the given rows by their canonical form, with the factor that gives it, so that their multiples are written as given */
  private var givenRows: Map[FieldVector[BigFraction], (FieldVector[BigFraction], BigFraction)] = _
  private var considerAllPivotRows: Boolean = false
  private var variableOrder: Seq[OWLDataProperty] = Seq()

  def considerAllPivotRows(value: Boolean): Unit = {
    considerAllPivotRows = value
  }

  /** the order in which the variables are eliminated; variables not in it come afterwards, sorted by IRI */
  def variableOrder(order: Seq[OWLDataProperty]): Unit = {
    variableOrder = order
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

    res
  }

  override def implies(premises: Iterable[LinearConstraint[BigFraction]], conclusion: LinearConstraint[BigFraction]): Boolean = {
    implies(premises, conclusion, Option.empty)
  }

  def implies(premises: Iterable[LinearConstraint[BigFraction]], conclusion: LinearConstraint[BigFraction], tracker: Option[ImplicationTracker]): Boolean = {
    val start = System.nanoTime()
    // the data properties of the conclusion need values; for def(x), elimination alone would derive it from nothing
    val valuesExist = conclusion.getOWLDataProperties.subsetOf(premises.flatMap(_.getOWLDataProperties).toSet)
    val res =
      if (valuesExist) !satisfiable(premises, Iterable(conclusion), tracker)
      else !satisfiable(premises, Iterable.empty, tracker)
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Implication"
    rTT.cDReasoningTime = time

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

    keyMap = MatrixConversions.getKeyMap(predicates ++ targets, variableOrder)
    numVars = keyMap.size
    rows = predicates.map(MatrixConversions.toBigRow(_, keyMap))
    targetRows = targets.map(MatrixConversions.toBigRow(_, keyMap))
    pivotRowLog = List[FieldVector[BigFraction]]()
    columnsByIRI = keyMap.toSeq.sortBy(_._1.getIRI.toString).map(_._2.intValue)
    // of several given rows with the same canonical form, the first one
    givenRows = (rows ++ targetRows).foldLeft(Map[FieldVector[BigFraction], (FieldVector[BigFraction], BigFraction)]()) {
      (map, row) =>
        val (canonicalRow, factor) = canonical(row)
        if (map.contains(canonicalRow)) map else map + (canonicalRow -> (row, factor))
    }

    if(tracker.isDefined)
      tracker.get.setVarsMap(keyMap);


    var i = 0
    while (i < numVars) {
//      for (row <- rows)
//      for (targetRow <- targetRows)

      // Find the next column with non-zero entries and classify the rows accordingly.
      var (pivotRows, rowsWithZeroEntries, newI) = findFirstNonZero(rows, i, false)
      i = newI
      var (targetRowsWithNonZeroEntries, targetRowsWithZeroEntries, _) = findFirstNonZero(targetRows, i, true)
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

    val factor = row.getEntry(i).negate.divide(pivotRow.getEntry(i)) // f = -y_i/x_i
    if (factor.equals(BigFraction.ZERO)) {
      return row
    }

    val sum = row.add(pivotRow.mapMultiply(factor)) // y' = y + f*x
//    print("Reduced: ")

    assert(sum.getEntry(i).equals(BigFraction.ZERO))

    val (normalized, factor2) = normalize(sum)

    if (tracker.isDefined)
      tracker.get.addInference(normalized, pivotRow, row, factor.multiply(factor2), factor2)

    assert(normalized.getEntry(i).equals(BigFraction.ZERO))

    normalized
  }

  /**
   * The representative of the constraint of the row, and the factor that gives it: the given row if the row is a
   * multiple of one, otherwise the canonical form. So multiples of a constraint become the same row in every order of
   * elimination, and the given constraints keep the form in which they were written.
   */
  def normalize(row: FieldVector[BigFraction]): (FieldVector[BigFraction], BigFraction) = {
    val (canonicalRow, factor) = canonical(row)
    givenRows.get(canonicalRow) match {
      // row * factor = canonicalRow = given * givenFactor
      case Some((given, givenFactor)) => (given, factor.divide(givenFactor))
      case None => (canonicalRow, factor)
    }
  }

  /**
   * The row with integer entries without a common divisor whose first non-zero coefficient, with the variables sorted
   * by IRI, is positive (for 0 = c, the constant), and the factor that gives it. The zero row stays as it is.
   */
  private def canonical(row: FieldVector[BigFraction]): (FieldVector[BigFraction], BigFraction) = {
    val entries = (columnsByIRI :+ numVars).map(row.getEntry).filterNot(_.equals(BigFraction.ZERO))
    if (entries.isEmpty)
      return (row, BigFraction.ONE)
    // of fractions in lowest terms, the greatest common divisor is that of the numerators divided by the least common
    // multiple of the denominators
    val lcm = entries.map(_.getDenominator).foldLeft(BigInteger.ONE)((a, b) => a.divide(a.gcd(b)).multiply(b))
    val gcd = entries.map(_.getNumerator).foldLeft(BigInteger.ZERO)(_ gcd _)
    val sign = if (entries.head.compareTo(BigFraction.ZERO) < 0) BigInteger.ONE.negate else BigInteger.ONE
    val factor = new BigFraction(lcm.multiply(sign), gcd)
    (row.mapMultiply(factor), factor)
  }
}