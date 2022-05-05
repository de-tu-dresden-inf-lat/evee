package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import java.util.Collections;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;

public interface IProofEvaluator<SENTENCE> {
	double evaluate(IProof<SENTENCE> proof) throws ProofException;

	default double evaluate(IInference<SENTENCE> inference) throws ProofException {
		return evaluate(new Proof<SENTENCE>(inference.getConclusion(), Collections.singleton(inference)));
	}

	default String getDescription() {
		return "";
	}
}
