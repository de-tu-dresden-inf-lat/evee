package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import de.tu_dresden.inf.lat.evee.general.interfaces.IHasProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;

public interface ISimpleProofGenerator<SYMBOL,SENTENCE,THEORY> extends IHasProgressTracker {

    void setOntology(THEORY ontology);

    IProof<SENTENCE> proveSubsumption(SYMBOL lhs, SYMBOL rhs) throws ProofGenerationException;

    IProof<SENTENCE> proveEquivalence(SYMBOL lhs, SYMBOL rhs) throws ProofGenerationException;
}
