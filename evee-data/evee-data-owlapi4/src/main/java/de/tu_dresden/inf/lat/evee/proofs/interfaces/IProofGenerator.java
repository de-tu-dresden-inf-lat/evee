package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;

public interface IProofGenerator<SENTENCE, ONTOLOGY> extends IHasProgressTracker, IIsCancellable {

	void setOntology(ONTOLOGY ontology);

	boolean supportsProof(SENTENCE axiom);

	IProof<SENTENCE> getProof(SENTENCE axiom) throws ProofGenerationException;

}
