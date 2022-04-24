package de.tu_dresden.inf.lat.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.proofs.tools.ProofTools;

/**
 * Computes the size of justification underlying the proof, i.e. the number of
 * leaves.
 * 
 * @author stefborg
 *
 */
public class JustificationSizeEvaluator<S> implements IProofEvaluator<S> {

	@Override
	public double evaluate(IProof<S> proof) {
		return (double) proof.getInferences().stream().filter(ProofTools::isAsserted).distinct().count();
	}

	@Override
	public String getDescription() {
		return "Justification size";
	}

}
