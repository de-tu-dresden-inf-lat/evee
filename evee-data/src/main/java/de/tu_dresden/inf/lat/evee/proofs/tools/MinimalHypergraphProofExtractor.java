package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class MinimalHypergraphProofExtractor {

	public static <S> IProof<S> makeUnique(IProof<S> proof) {
		Set<IInference<S>> infs = selectMinimalInferenceSet(
				getAllPossibleInferenceSets(proof, proof.getFinalConclusion(), Collections.emptyList()));
		return new Proof<>(proof.getFinalConclusion(), new LinkedList<>(infs));
	}

	private static <S> Set<IInference<S>> selectMinimalInferenceSet(Set<Set<IInference<S>>> inf) {
		return inf.stream().min(Comparator.comparing(Set::size)).orElseThrow(
				() -> new RuntimeException("The given derivation structure should contain at least one proof."));
	}

	private static <S> Set<Set<IInference<S>>> getAllPossibleInferenceSets(IProof<S> proof, S conclusion,
			List<S> trace) {
		if (trace.contains(conclusion)) {
			// avoid loops
			return new HashSet<>();
		}
		List<S> newTrace = new LinkedList<>(trace);
		newTrace.add(conclusion);

		// collect all subproofs and combine them in all possible ways
		return proof.getInferences(conclusion).stream().distinct()
				.map(inf -> getAllPossibleInferenceSets(proof, inf, newTrace))
				.reduce(MinimalHypergraphProofExtractor::union).orElse(new HashSet<>());
	}

	private static <S> Set<Set<IInference<S>>> getAllPossibleInferenceSets(IProof<S> proof, IInference<S> inf,
			List<S> trace) {
		return addToAll(inf.getPremises().stream().map(premise -> getAllPossibleInferenceSets(proof, premise, trace))
				.reduce(MinimalHypergraphProofExtractor::combineAll)
				.orElse(new HashSet<>(Arrays.asList(new HashSet<>()))), inf);
	}

	private static <S> Set<Set<IInference<S>>> combineAll(Set<Set<IInference<S>>> inf1, Set<Set<IInference<S>>> inf2) {
		Set<Set<IInference<S>>> newInf = new HashSet<>();
		for (Set<IInference<S>> set1 : inf1) {
			for (Set<IInference<S>> set2 : inf2) {
				Set<IInference<S>> newSet = new HashSet<>(set1);
				newSet.addAll(set2);
				newInf.add(newSet);
			}
		}
		return newInf;
	}

	private static <S> Set<Set<IInference<S>>> union(Set<Set<IInference<S>>> inf1, Set<Set<IInference<S>>> inf2) {
		Set<Set<IInference<S>>> newInf = new HashSet<>(inf1);
		newInf.addAll(inf2);
		return newInf;
	}

	private static <S> Set<Set<IInference<S>>> addToAll(Set<Set<IInference<S>>> inf1, IInference<S> inf2) {
		return combineAll(inf1, new HashSet<>(Arrays.asList(new HashSet<>(Arrays.asList(inf2)))));
	}

}
