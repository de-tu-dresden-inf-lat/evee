package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException
import de.tu_dresden.inf.lat.evee.proofs.data.{Inference, Proof}
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}

import  de.tu_dresden.inf.lat.evee.concreteDomains.data.cdGeneral.ReasoningTaskTracker
import  de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName

import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools

//import de.tu_dresden.lat.z3.Z3Checker

import org.semanticweb.owlapi.model.OWLDataProperty

import java.util
import java.util.stream.Stream
import scala.collection.JavaConverters
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

abstract class ConstraintPropagationReasoner[Constraint <: CDConstraint, DOMAIN] extends CDReasonerWithStatistics[Constraint]
  with CDProofGenerator[Constraint] with CDCounterexampleGenerator[Constraint, DOMAIN] {

  protected var lastReasoningInput: Iterable[Constraint] = Set()
  protected var lastReasoningOutput = Set[Constraint]()
  protected var lastInferences = Set[IInference[Constraint]]()
  var lastNetwork: ConstraintNetwork[Constraint]

  protected var continueEvenIfInconsistent = false

  protected var completeReasoning=false

  protected  var noInconsistencyInference=false

  def setContinueEvenIfInconsistent() = {
    this.continueEvenIfInconsistent=true
  }

  def setNoInconsistencyInference() = {
    noInconsistencyInference=true;
  }

  def setCompleteReasoning(): Unit

  def saturate(predicates: Iterable[Constraint]): Set[Constraint]

  var predicates: Iterable[Constraint] = _

  override def setOntology(predicates: Iterable[Constraint]): Unit = {
    this.predicates = predicates
  }

  def getProof(conclusion:  Constraint): IProof[Constraint] 

  def consistent(predicates: Iterable[Constraint]): Boolean

  def implies(premises: Iterable[Constraint], conclusion: Constraint): Boolean 

  override def getCDReasoner: CDReasoner[Constraint] = this

  def proveInconsistency(): IProof[Constraint]

  override def supportsProof(axiom: Constraint): Boolean = true

  override def successful(): Boolean = true

  var negativePredicates: Iterable[Constraint] = _

  override def setObservation(observation: Iterable[Constraint]): Unit = {
    this.negativePredicates = observation
  }

  var relevantDataProperties: Set[OWLDataProperty] = _

  override def setSignature(signature: util.Collection[OWLDataProperty]): Unit = {
    this.relevantDataProperties = signature.toSet
  }

  def generateExplanations(): Stream[Map[OWLDataProperty, DOMAIN]] 

  def satisfies(constraint: Constraint, assignment: Map[OWLDataProperty, DOMAIN]): Boolean

  override def supportsExplanation(): Boolean = true

  protected def assertionTest(assignment: Map[OWLDataProperty, DOMAIN]) = {
    if (Tools.areAssertionsActivated()) {
      if (!predicates.forall(satisfies(_, assignment)))
        throw new ModelGenerationException("Some positive constraints are not satisfied by the assignment!")
      if (negativePredicates.exists(satisfies(_, assignment)))
        throw new ModelGenerationException("Some negative constraint is satisfied the by the assignment!")
    }
  }

  override def ignoresPartsOfOntology(): Boolean = false
}
