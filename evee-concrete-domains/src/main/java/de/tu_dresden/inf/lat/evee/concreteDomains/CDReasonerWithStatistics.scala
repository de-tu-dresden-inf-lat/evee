package de.tu_dresden.inf.lat.evee.concreteDomains

import  de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral.ReasoningTaskTracker

abstract class CDReasonerWithStatistics[CD_CONSTRAINT <: CDConstraint] extends CDReasoner[CD_CONSTRAINT] {

  private var numberOfConstraints = List[Int]()
  private var numberOfVariables = List[Int]()
  private var statisticsEnabled = false

  private var trackers = List[ReasoningTaskTracker]()

  def statisticsEnabled(value: Boolean): Unit = {
    statisticsEnabled = value
  }

  protected def collectStatistics(positive: Iterable[CD_CONSTRAINT], negative: Iterable[CD_CONSTRAINT]) : Unit = {
    if (statisticsEnabled) {
      numberOfConstraints ::= positive.size + negative.size
      numberOfVariables ::= positive.flatMap(_.getOWLDataProperties).toSet.union(negative.flatMap(_.getOWLDataProperties).toSet).size
    }
  }

  def getMaxNumberOfConstraints: Int = numberOfConstraints.reduceOption(math.max).getOrElse(0)

  def getAvgNumberOfConstraints: Double = {
    if (numberOfConstraints.isEmpty)
      return 0d
    numberOfConstraints.sum / numberOfConstraints.size
  }

  def getMaxNumberOfVariables: Int = numberOfVariables.reduceOption(math.max).getOrElse(0)

  def getAvgNumberOfVariables: Double = {
    if (numberOfVariables.isEmpty)
      return 0d
    numberOfVariables.sum / numberOfVariables.size
  }

  def getNumberOfProblemsSolved: Int = numberOfConstraints.size

  def getTrackersList: List[ReasoningTaskTracker] = trackers
  def addTracker(t:ReasoningTaskTracker): Unit = if(statisticsEnabled) trackers ::= t
}
