package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD

import scala.collection.mutable.ListBuffer

/**
 * @author Christian Alrabbaa
 */

abstract class ReasonerWithStatistics extends Reasoner {

  private val owlClassificationTimes = ListBuffer[Long]()
  private val owlReasonerCalls = ListBuffer[Int]()

  private var statisticsEnabled = false

  protected def collectStatistics(owlClsTime : Long, owlReaCalls : Int)
  : Unit = {
    if(!statisticsEnabled)
      return

    owlClassificationTimes.append(owlClsTime)
    owlReasonerCalls.append(owlReaCalls)
  }

  def getMaxClassificationTime() : Long = owlClassificationTimes.reduceOption((a,b)=>Math.max(a,b)).getOrElse(-1L)

  def getMaxNumberOfReasonerCalls() : Int = owlReasonerCalls.reduceOption((a,b)=>Math.max(a,b)).getOrElse(-1)

  def statisticsEnabled(value: Boolean): Unit = {
    statisticsEnabled = value
  }
}
