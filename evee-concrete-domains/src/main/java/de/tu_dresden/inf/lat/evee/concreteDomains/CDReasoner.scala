package de.tu_dresden.inf.lat.evee.concreteDomains

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.INonEntailmentExplainer
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException
import de.tu_dresden.inf.lat.evee.proofs.interfaces.{IProof, IProofGenerator}
import org.semanticweb.owlapi.model.OWLDataProperty

// TODO adapt this interface using "setPremises", to make it easier to reuse computations to check different conclusions for the same set of premises
trait CDReasoner[CD_CONSTRAINT <: CDConstraint] {
  def consistent(predicates: Iterable[CD_CONSTRAINT]): Boolean
  def implies(premises: Iterable[CD_CONSTRAINT], conclusion: CD_CONSTRAINT): Boolean
}

trait CDProofGenerator[CD_CONSTRAINT <: CDConstraint] extends IProofGenerator[CD_CONSTRAINT, Iterable[CD_CONSTRAINT]] {
  def getCDReasoner: CDReasoner[CD_CONSTRAINT]

  @throws[ProofGenerationException]
  def proveInconsistency(): IProof[CD_CONSTRAINT]
}

trait CDCounterexampleGenerator[CD_CONSTRAINT <: CDConstraint, CD_VALUE] extends
  INonEntailmentExplainer[Iterable[CD_CONSTRAINT], OWLDataProperty, Iterable[CD_CONSTRAINT], Map[OWLDataProperty, CD_VALUE]] {

}
