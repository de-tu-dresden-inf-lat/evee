package de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral

/**
 * @author Christian Alrabbaa
 */
class ReasoningTaskTracker() {

  var reasoningTaskType:String = "Not-Set"
  var cDReasoningTime: Long = -1L
  var z3ReasoningTime: Long = -1L

  var z3resultEqualsOurs: Boolean = false

}