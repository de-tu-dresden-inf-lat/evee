package de.tu_dresden.inf.lat.evee.proofs.proofGenerators;

import java.util.HashMap;
import java.util.Map;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

public class CachingProofGenerator<SENTENCE, THEORY> implements IProofGenerator<SENTENCE, THEORY> {
	private IProofGenerator<SENTENCE, THEORY> internal;

	private Map<SENTENCE, IProof<SENTENCE>> cache = new HashMap<>();

	public CachingProofGenerator(IProofGenerator<SENTENCE, THEORY> proofGenerator) {
		this.internal = proofGenerator;
	}

	@Override
	public void addProgressTracker(IProgressTracker tracker) {
		internal.addProgressTracker(tracker);
	}

	@Override
	public void cancel() {
		internal.cancel();
	}

	@Override
	public void setOntology(THEORY theory) {
		internal.setOntology(theory);
		cache.clear();
	}

	@Override
	public boolean successful() {
		return internal.successful();
	}

	@Override
	public boolean supportsProof(SENTENCE axiom) {
		return internal.supportsProof(axiom);
	}

	@Override
	public IProof<SENTENCE> getProof(SENTENCE axiom) throws ProofGenerationException {
		if (cache.containsKey(axiom))
			return cache.get(axiom);
		else {
			IProof<SENTENCE> proof = internal.getProof(axiom);
			if (internal.successful()) {
				cache.put(axiom, proof);
			}
			return proof;
		}
	}
}
