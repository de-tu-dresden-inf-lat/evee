package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IRecursiveMeasure;

public class MinimalProofExtractor<S> {

	protected IRecursiveMeasure<S> measure;

	public MinimalProofExtractor(IRecursiveMeasure<S> measure) {
		this.measure = measure;
	}

	public IProof<S> extract(IProof<S> proof) throws ProofGenerationFailedException {
		// TODO: replace by TreeMultiMap<Integer, S> to keep it sorted?
		Map<S, Double> currentBestProofValues = new HashMap<>();
		Map<S, IInference<S>> currentBestInferences = new HashMap<>();

		return dijkstraWrapper(proof, currentBestProofValues, currentBestInferences);
	}

	protected IProof<S> dijkstraWrapper(IProof<S> proof, Map<S, Double> currentBestProofValues,
			Map<S, IInference<S>> currentBestInferences) throws ProofGenerationFailedException {
		for (IInference<S> inf : proof.getInferences().stream().filter(inf -> inf.getPremises().isEmpty())
				.collect(Collectors.toList())) {
			// tautologies and TBox axioms have minimal proof size 1
			// (we assume that all TBox axioms are derived by an inference without premises)
			S ax = inf.getConclusion();
			currentBestProofValues.put(ax, measure.leafValue(ax));
			currentBestInferences.put(ax, inf);
		}

		// TODO: implement this directly in the Proof class?
		SetMultimap<S, IInference<S>> premiseMap = HashMultimap.create();
		for (IInference<S> inference : proof.getInferences())
			for (S premise: inference.getPremises())
				premiseMap.put(premise, inference);

		currentBestInferences = dijkstra(premiseMap, proof.getFinalConclusion(), currentBestProofValues,
				currentBestInferences);

		List<IInference<S>> treeProof = new ArrayList<>();
		collectInferences(treeProof, proof.getFinalConclusion(), currentBestInferences);

		return new Proof<S>(proof.getFinalConclusion(), treeProof);
	}

	private Map<S, IInference<S>> dijkstra(SetMultimap<S, IInference<S>> premiseMap, S conclusion, Map<S, Double> currentBestProofValues,
			Map<S, IInference<S>> currentBestInferences) {
		Map<S, Double> finalBestProofValues = new HashMap<>();

		while (!currentBestProofValues.isEmpty()) {
			// choose globally minimal of the current candidates
			S current = getMinimalProofValue(currentBestProofValues);
			if (current.equals(conclusion)) {
				// stop once we have reached the root
				break;
			}
			// we have found the best proof for 'current'
			finalBestProofValues.put(current, currentBestProofValues.get(current));
			currentBestProofValues.remove(current);
			// find inferences that involved 'current'
			Stream<IInference<S>> nextInferences = getNextInferences(premiseMap, current, currentBestProofValues,
					finalBestProofValues);

			nextInferences.forEach(inf -> {

				// construct new proof from new inference candidates
				double newProofSize = measure.edgeValue(inf, inf.getPremises().stream()
						.map(ax -> finalBestProofValues.get(ax)).collect(Collectors.toList()));

				// compare new proof to current best proof
				S ax = inf.getConclusion();
				if ((!currentBestProofValues.containsKey(ax)) || (newProofSize < currentBestProofValues.get(ax))) {
					currentBestProofValues.put(ax, newProofSize);
					currentBestInferences.put(ax, inf);
					// System.out.println("Added new best inference for " + ax + ": " + inf);
				}

			});

		}

		return currentBestInferences;
	}

	private S getMinimalProofValue(Map<S, Double> proofValues) {
		return proofValues.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue())).findFirst()
				.get().getKey();
	}

	private Stream<IInference<S>> getNextInferences(SetMultimap<S, IInference<S>> premiseMap, S current, Map<S, Double> currentBestProofValues,
			Map<S, Double> finalBestProofValues) {
		// find all inferences that involve 'current' as a premise, the conclusion has
		// not yet been processed, and all premises have already been processed
		return premiseMap.get(current).stream()
				.filter(inf -> !finalBestProofValues.containsKey(inf.getConclusion()))
				.filter(inf -> inf.getPremises().stream().allMatch(ax -> finalBestProofValues.containsKey(ax)));
	}

	private void collectInferences(List<IInference<S>> list, S conclusion, Map<S, IInference<S>> bestInferences)
			throws ProofGenerationFailedException {
		
		if (!bestInferences.containsKey(conclusion)) {
			throw new ProofGenerationFailedException(
					"No proof found - input derivation structure does not contain a proof for " + conclusion);
		}

		IInference<S> current = bestInferences.get(conclusion);
		list.add(current);

		for (S premise : current.getPremises()) {
			collectInferences(list, premise, bestInferences);
		}
	}

}
