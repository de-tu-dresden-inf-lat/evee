package de.tu_dresden.inf.lat.proofs.interfaces;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;

public interface IProofGenerator<SENTENCE, ONTOLOGY> extends IHasProgressTracker, IIsCancellable {

	void setOntology(ONTOLOGY ontology);

	boolean supportsProof(SENTENCE axiom);

	IProof<SENTENCE> getProof(SENTENCE axiom) throws ProofGenerationException;

}
