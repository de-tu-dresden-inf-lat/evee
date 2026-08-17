package de.tu_dresden.inf.lat.evee.concreteDomains.multDomain

import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof
import de.tu_dresden.inf.lat.evee.concreteDomains.{ConstraintNetwork, ConstraintPropagationReasoner}

import  de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral.ReasoningTaskTracker
import  de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

//import de.tu_dresden.lat.z3.Z3Checker

import org.apache.commons.math3.fraction.BigFraction
import org.semanticweb.owlapi.model.OWLDataProperty

import java.util.stream.Stream
import scala.collection.JavaConverters
import scala.collection.JavaConverters.seqAsJavaListConverter

object MultRules {
  val RULE_INFER_BOUND_FROM_CONSTANT = "Introduce >"
  val RULE_INFER_WEAKER_BOUND = "Weaken >"
  val RULE_INFER_FROM_CONTRADICTION = "From ⊥"
  val RULE_DIFFERENT_CONSTANTS = "Different constants"
  val RULE_CONSTANT_TOO_LARGE = "Constant too large"
  val RULE_DIFFERENT_FACTORS = "Different factors"
  val RULE_ZERO = "Tautology"
  val RULE_FACTOR_FROM_CONSTANTS = "Constant quotient"
  val RULE_TRANSITIVITY = "Product of factors"
  val RULE_PROPAGATE_EQUAL = "Propagate ="
  val RULE_PROPAGATE_LESS_THAN = "Propagate <"
  val RULE_INVERSE = "Invert"
}


class MultReasoner extends ConstraintPropagationReasoner[MultConstraint,BigFraction] {

  override var lastNetwork: ConstraintNetwork[MultConstraint] = new MultNetworkSimple()

  override def setCompleteReasoning(): Unit =
    throw new AssertionError("Not implemented!")

  override def saturate(predicates: Iterable[MultConstraint]): Set[MultConstraint] = {

    lastReasoningInput = predicates

    lastNetwork = 
      new MultNetworkSimple()
    if(continueEvenIfInconsistent)
      lastNetwork.setContinueEvenIfInconsistent()
    lastNetwork.fill(predicates)
    lastNetwork.propagate()
    lastReasoningOutput = lastNetwork.derivedPredicates()
    lastInferences = lastNetwork.derivationStructure()

    lastReasoningOutput
  }


  override def getProof(conclusion:  MultConstraint): IProof[MultConstraint] = {
    saturate(this.predicates)

    var inferences = lastInferences.toList

    conclusion match {
      case MultLessThan(property, bound) =>
        //println("Additional proof for "+conclusion+ " may be necessary")
        lastReasoningOutput.foreach(p => p match {
          case MultEqual(property2, var2) if property2.equals(property) && var2.compareTo(bound) < 0=>
            inferences ::= new Inference[MultConstraint](conclusion, MultRules.RULE_INFER_BOUND_FROM_CONSTANT, List(p).asJava)
          case MultLessThan(property2, var2) if property2.equals(property) && var2.compareTo(bound) <= 0 =>
            inferences ::= new Inference[MultConstraint](conclusion, MultRules.RULE_INFER_WEAKER_BOUND, List(p).asJava)
          case other => false
        })
      case _ => ;
    }

    if(lastReasoningOutput.contains(MultContradiction) && !noInconsistencyInference)
      inferences ::= new Inference[MultConstraint](conclusion, MultRules.RULE_INFER_FROM_CONTRADICTION, List(MultContradiction).asJava)

    new Proof(conclusion, inferences.asJava)
  }

  override def consistent(predicates: Iterable[MultConstraint]): Boolean = {
    collectStatistics(predicates, Iterable.empty)

    val start = System.nanoTime()
    val res = !saturate(predicates).contains(MultContradiction)
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Consistency"
    rTT.cDReasoningTime = time

    res

  }

  override def implies(premises: Iterable[MultConstraint], conclusion: MultConstraint): Boolean = {
    collectStatistics(premises, Set(conclusion))

    val start = System.nanoTime()
    val saturated = saturate(premises)

    val res = saturated.contains(MultContradiction) || ( conclusion match {
      case MultLessThan(property, bound) =>
        saturated.exists(_ match {
          case MultEqual(property2, var2) if property2.equals(property) => var2.compareTo(bound) < 0
          case MultLessThan(property2, var2) if property2.equals(property) => var2.compareTo(bound) <= 0
          case _ => false
        })
      case _ => saturated.contains(conclusion)
    })
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Implication"
    rTT.cDReasoningTime = time

    // if (Tools.isZ3CheckEnabled)
    //   Z3Checker.checkImplication(res, JavaConverters.asJavaCollection(premises), conclusion, ConcreteDomainName
    //     .QGreater, rTT)

    res
  }

  override def proveInconsistency(): IProof[MultConstraint] = getProof(MultContradiction)

  override def generateExplanations(): Stream[Map[OWLDataProperty, BigFraction]] =
  throw new AssertionError("Not Implemented!")

  def satisfies(constraint: MultConstraint, assignment: Map[OWLDataProperty, BigFraction]): Boolean = {
    constraint match {
      case MultEqual(prop, value) => assignment(prop).equals(value)
      case MultLessThan(prop, value) => assignment(prop).compareTo(value)<0// > value
      case Multiplication(prop1, value, prop2) => assignment(prop1).multiply(value).equals(assignment(prop2))
      case MultContradiction => false
    }
  }

}
