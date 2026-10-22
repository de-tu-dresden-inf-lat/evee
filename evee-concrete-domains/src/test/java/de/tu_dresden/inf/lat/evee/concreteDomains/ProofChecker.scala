package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IInference, IProof}
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools
import org.junit.Assert.assertTrue
import org.semanticweb.owlapi.model.parameters.{AxiomAnnotations, Imports}
import org.semanticweb.owlapi.model.{OWLAxiom, OWLClass, OWLClassExpression, OWLOntology, OWLSubClassOfAxiom}

import scala.collection.JavaConverters._

/**
 * Checks that a proof is correct, as CorrectnessEvaluator in evee-data does: the final conclusion is the goal, all
 * asserted sentences are actually asserted, the final conclusion has a complete and acyclic derivation, and every
 * other inference is sound. Ported from ProofEvaluator in the original concrete domain prototype.
 *
 * Instead of returning only a verdict, the checker lists the problems it finds, so that test failures say what is
 * wrong with a proof.
 */
class ProofChecker[T](goal: T, isAsserted: T => Boolean, isSound: IInference[T] => Boolean) {

  def problems(proof: IProof[T]): List[String] = {
    val inferences = proof.getInferences.asScala.toList
    val (asserted, derived) = inferences.partition(ProofTools.isAsserted(_))

    val wrongGoal =
      if (proof.getFinalConclusion == goal) Nil
      else List(s"the final conclusion ${proof.getFinalConclusion} is not the goal $goal")
    val notAsserted = asserted.map(_.getConclusion).filterNot(isAsserted).map(s => s"$s is not asserted")
    val incomplete =
      if (derivable(inferences).contains(proof.getFinalConclusion)) Nil
      else List(s"the final conclusion ${proof.getFinalConclusion} has no complete and acyclic derivation")
    val unsound = derived.filterNot(isSound).map(inference => s"unsound inference: $inference")

    wrongGoal ++ notAsserted ++ incomplete ++ unsound
  }

  def assertCorrect(proof: IProof[T]): Unit = {
    val found = problems(proof)
    assertTrue("the proof is not correct:\n" + found.mkString("\n") + "\nproof:\n" + proof, found.isEmpty)
  }

  /**
   * The sentences with a finite derivation tree, i.e., the least fixpoint of the inferences. Unlike the recursive
   * check in CorrectnessEvaluator, this takes polynomial time also on proofs with many alternative inferences.
   */
  private def derivable(inferences: List[IInference[T]]): Set[T] = {
    var result = Set[T]()
    var changed = true
    while (changed) {
      val next = inferences.filter(_.getPremises.asScala.forall(result)).map(_.getConclusion).toSet
      changed = !next.subsetOf(result)
      result ++= next
    }
    result
  }
}

object ProofChecker {

  /**
   * A checker for proofs of a constraint from the given premises, as produced by a CDProofGenerator.
   *
   * Since features are partial, a constraint only holds if its variables have values; e.g., x + 0 = x does not follow
   * from no premises. The constraint reasoners nevertheless use such tautologies without premises for the variables
   * of the input constraints. Hence, an inference without premises is sound if the input constraints imply it.
   */
  def forConstraints[C <: CDConstraint](reasoner: CDReasoner[C], premises: Iterable[C], goal: C): ProofChecker[C] =
    new ProofChecker[C](goal, premises.toSet, inference =>
      if (inference.getPremises.isEmpty) reasoner.implies(premises, inference.getConclusion)
      else reasoner.implies(inference.getPremises.asScala, inference.getConclusion))

  /**
   * A checker for proofs of an OWL axiom from the given ontology that may use concrete domain reasoning. An inference
   * is sound if its conclusion follows from its premises in OWL (checked with HermiT), or if it is a concrete domain
   * inference: all its sentences have the form X ⊑ D for the same X, where each D is a class with a constraint
   * (the conclusion may also have owl:Nothing), and the constraints of the premises imply the constraint of the
   * conclusion (or are inconsistent if the conclusion is X ⊑ owl:Nothing). The constraints of the classes among the
   * top-level conjuncts of X also hold for X, so they are added to the premises.
   *
   * @param constraints the constraint of each class, including fresh classes that only occur in proofs
   */
  def forOWL[C <: CDConstraint](ontology: OWLOntology, constraints: Map[OWLClass, C], reasoner: CDReasoner[C],
                                goal: OWLAxiom): ProofChecker[OWLAxiom] = {

    def asserted(axiom: OWLAxiom): Boolean =
      ontology.containsAxiom(axiom, Imports.INCLUDED, AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS)

    def soundForConcreteDomain(inference: IInference[OWLAxiom]): Boolean = {
      val sentences = inference.getConclusion :: inference.getPremises.asScala.toList
      val subsumptions = sentences.collect { case axiom: OWLSubClassOfAxiom => axiom }
      if (subsumptions.size != sentences.size || subsumptions.map(_.getSubClass).distinct.size != 1)
        return false
      def hasConstraint(c: OWLClassExpression) = !c.isAnonymous && constraints.contains(c.asOWLClass)
      val conclusion :: premises = subsumptions.map(_.getSuperClass)
      if (!premises.forall(hasConstraint) || !(conclusion.isOWLNothing || hasConstraint(conclusion)))
        return false

      val conjunctConstraints = subsumptions.head.getSubClass.asConjunctSet.asScala.filter(hasConstraint)
      val premiseConstraints = (premises ++ conjunctConstraints).map(p => constraints(p.asOWLClass))
      if (conclusion.isOWLNothing) !reasoner.consistent(premiseConstraints)
      else reasoner.implies(premiseConstraints, constraints(conclusion.asOWLClass))
    }

    new ProofChecker[OWLAxiom](goal, asserted,
      inference => ProofTools.isCorrect(inference) || soundForConcreteDomain(inference))
  }
}
