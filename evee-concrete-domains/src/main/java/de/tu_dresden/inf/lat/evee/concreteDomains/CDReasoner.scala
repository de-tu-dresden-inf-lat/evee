package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.INonEntailmentExplainer
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IProof, IProofGenerator}
import org.semanticweb.owlapi.model.OWLDataProperty

trait CDReasoner[CD_CONSTRAINT <: CDConstraint] {
  def consistent(predicates: Iterable[CD_CONSTRAINT]): Boolean

  /**
   * Whether every individual that satisfies the premises also satisfies the conclusion. Data properties are partial
   * functions, and a constraint only holds if all its data properties have values. So, unless the premises are
   * inconsistent, every data property of the conclusion has to occur in the premises, e.g., def(x) follows from x = 1,
   * but not from the empty set.
   */
  def implies(premises: Iterable[CD_CONSTRAINT], conclusion: CD_CONSTRAINT): Boolean
}

/** rule names shared by the proofs of all concrete domains */
object CDRules {
  /** def(x) holds because a premise mentions x, so x has a value */
  val DEFINED = "Defined"
}

trait CDProofGenerator[CD_CONSTRAINT <: CDConstraint] extends IProofGenerator[CD_CONSTRAINT, Iterable[CD_CONSTRAINT]] {
  def getCDReasoner: CDReasoner[CD_CONSTRAINT]

  @throws[ProofGenerationException]
  def proveInconsistency(): IProof[CD_CONSTRAINT]
}

trait CDCounterexampleGenerator[CD_CONSTRAINT <: CDConstraint, CD_VALUE] extends
  INonEntailmentExplainer[Iterable[CD_CONSTRAINT], OWLDataProperty, Iterable[CD_CONSTRAINT], Map[OWLDataProperty, CD_VALUE]] {

}
