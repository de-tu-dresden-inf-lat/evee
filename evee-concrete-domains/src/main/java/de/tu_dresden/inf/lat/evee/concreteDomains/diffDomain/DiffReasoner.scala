package de.tu_dresden.inf.lat.evee.concreteDomains.diffDomain

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import de.tu_dresden.inf.lat.evee.concreteDomains.{CDCounterexampleGenerator, CDProofGenerator, CDReasoner, ConstraintPropagationReasoner}

import de.tu_dresden.inf.lat.evee.data.cdGeneral.ReasoningTaskTracker
import de.tu_dresden.inf.lat.evee.data.names.ConcreteDomainName

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

//import de.tu_dresden.lat.z3.Z3Checker

import org.semanticweb.owlapi.model.OWLDataProperty

import java.util
import java.util.stream.Stream
import scala.collection.JavaConverters
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object DiffRules {
  val RULE_INFER_BOUND_FROM_CONSTANT = "Introduce >"
  val RULE_INFER_WEAKER_BOUND = "Weaken >"
  val RULE_INFER_FROM_CONTRADICTION = "From ⊥"
  val RULE_DIFFERENT_CONSTANTS = "Different constants"
  val RULE_CONSTANT_TOO_SMALL = "Constant too small"
  val RULE_DIFFERENT_DIFFERENCES = "Different differences"
  val RULE_ZERO = "Tautology"
  val RULE_DIFFERENCE_FROM_CONSTANTS = "Constant difference"
  val RULE_TRANSITIVITY = "Sum of differences"
  val RULE_PROPAGATE_EQUAL = "Propagate ="
  val RULE_PROPAGATE_GREATER_THAN = "Propagate >"
  val RULE_INVERSE = "Invert"
}


class DiffReasoner extends ConstraintPropagationReasoner[DiffConstraint,Double] {

  override var lastNetwork: ConstraintNetwork[DiffConstraint] = new DiffNetworkSimple()

  override def setCompleteReasoning(): Unit = {
    assert(false)
    lastNetwork = new DiffNetworkComplete()
    completeReasoning=true;
  }

  override def saturate(predicates: Iterable[DiffConstraint]): Set[DiffConstraint] = {

      lastReasoningInput = predicates

      lastNetwork = if(completeReasoning)
        new DiffNetworkComplete()
      else
        new DiffNetworkSimple()
      if(continueEvenIfInconsistent)
        lastNetwork.setContinueEvenIfInconsistent()
      lastNetwork.fill(predicates)
      lastNetwork.propagate()
      lastReasoningOutput = lastNetwork.derivedPredicates()
      lastInferences = lastNetwork.derivationStructure()


    lastReasoningOutput
  }

  override def getProof(conclusion: DiffConstraint): IProof[DiffConstraint] = {
    saturate(this.predicates)

    var inferences = lastInferences.toList

    conclusion match {
      case DiffGreaterThan(property, bound) =>
        //println("Additional proof for "+conclusion+ " may be necessary")
        lastReasoningOutput.foreach(p => p match {
          case DiffEqual(property2, var2) if property2.equals(property) && var2 > bound =>
            inferences ::= new Inference[DiffConstraint](conclusion, DiffRules.RULE_INFER_BOUND_FROM_CONSTANT, List(p).asJava)
          case DiffGreaterThan(property2, var2) if property2.equals(property) && var2 >= bound =>
            inferences ::= new Inference[DiffConstraint](conclusion, DiffRules.RULE_INFER_WEAKER_BOUND, List(p).asJava)
          case other => false
        })
      case _ => ;
    }

    if(lastReasoningOutput.contains(DiffContradiction) && !noInconsistencyInference)
      inferences ::= new Inference[DiffConstraint](conclusion, DiffRules.RULE_INFER_FROM_CONTRADICTION, List(DiffContradiction).asJava)

    new Proof(conclusion, inferences.asJava)
  }

  override def consistent(predicates: Iterable[DiffConstraint]): Boolean = {
    collectStatistics(predicates, Iterable.empty)

    val start = System.nanoTime()
    val res = !saturate(predicates).contains(DiffContradiction)
    val time = System.nanoTime() - start

    val rTT = new ReasoningTaskTracker()
    addTracker(rTT)
    rTT.reasoningTaskType = "Consistency"
    rTT.cDReasoningTime = time

    // if (Tools.isZ3CheckEnabled)
    //   Z3Checker.checkConsistency(res, JavaConverters.asJavaCollection(predicates), ConcreteDomainName.QGreater, rTT)

    res
  }

  override def implies(premises: Iterable[DiffConstraint], conclusion: DiffConstraint): Boolean = {
    collectStatistics(premises, Set(conclusion))

    val start = System.nanoTime()
    val saturated = saturate(premises)

    val res = saturated.contains(DiffContradiction) || ( conclusion match {
     case DiffGreaterThan(property, bound) =>
       saturated.exists(_ match {
         case DiffEqual(property2, var2) if property2.equals(property) => var2 > bound
         case DiffGreaterThan(property2, var2) if property2.equals(property) => var2 >= bound
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


  override def proveInconsistency(): IProof[DiffConstraint] = getProof(DiffContradiction)

  override def generateExplanations(): Stream[Map[OWLDataProperty, Double]] = {
    saturate(predicates)
    val solution = lastNetwork.generateSolution(negativePredicates)
    lastReasoningInput = Set() // reset this to avoid reusing the ConstraintNetwork again (we have just destroyed it)

    assertionTest(solution)
    val map = solution.filterKeys(prop => relevantDataProperties.contains(prop))
    Stream.of[Map[OWLDataProperty, Double]](map)
  }

  def satisfies(constraint: DiffConstraint, assignment: Map[OWLDataProperty, Double]): Boolean = {
    constraint match {
      case DiffEqual(prop, value) => assignment(prop) == value
      case DiffGreaterThan(prop, value) => assignment(prop) > value
      case DiffSum(prop1, value, prop2) => assignment(prop1) + value == assignment(prop2)
      case DiffContradiction => false
    }
  }

}
