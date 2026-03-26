/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

/**
 * Counts how many inferences are redundant, i.e., deduce an axiom that was
 * already deduced by another inference. If an inference appears twice in the
 * proof, it is only counted once.
 * 
 * @author stefborg
 *
 */
public class RedundancyEvaluator<S> implements IProofEvaluator<S> {

	@Override
	public double evaluate(IProof<S> proof) {
		return (double) proof.getInferences().stream().distinct().map(IInference::getConclusion)
				.collect(Collectors.groupingBy(ax -> ax, Collectors.counting())).entrySet().stream()
				.map(entry -> entry.getValue() - 1).mapToLong(v -> v).sum();
	}

	@Override
	public String getDescription() {
		return "Number of distinct redundant inferences";
	}

}
