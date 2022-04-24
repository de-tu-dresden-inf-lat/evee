/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators;

import java.util.HashSet;
import java.util.Set;

import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.proofs.tools.ProofTools;

/**
 * Counts the size of the proof as the number of axioms contained in it, where
 * axioms that are used several times are only counted once.
 * 
 * @author stefborg
 *
 */
public class HypergraphSizeEvaluator<S> implements IProofEvaluator<S> {

	@Override
	public double evaluate(IProof<S> proof) {
		Set<S> sentences = new HashSet<S>();
		proof.getInferences().stream().map(ProofTools::getSentences).forEach(sentences::addAll);
		return (double) sentences.size();
	}

	@Override
	public String getDescription() {
		return "Hypergraph size";
	}

}
