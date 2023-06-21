package de.tu_dresden.inf.lat.evee.proofs.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

/**
 * @author Christian Alrabbaa
 *
 */
public class Proof<SENTENCE> implements IProof<SENTENCE> {

	private final SetMultimap<SENTENCE, IInference<SENTENCE>> conclusions2inferences;
	private final List<IInference<SENTENCE>> inferences;

	private final SENTENCE finalConclusion;

	@JsonCreator
	public Proof(@JsonProperty("finalConclusion") SENTENCE finalConclusion,
			@JsonProperty("inferences") Collection<IInference<SENTENCE>> inferences) {
		this.finalConclusion = finalConclusion;
		this.inferences = new LinkedList<>(inferences);

		conclusions2inferences = HashMultimap.create();

		for (IInference<SENTENCE> inference : inferences) {
			conclusions2inferences.put(inference.getConclusion(), inference);
		}
	}

	public Proof(SENTENCE finalConclusion) {
		this.finalConclusion = finalConclusion;
		inferences = new LinkedList<>();
		conclusions2inferences = HashMultimap.create();
	}

	@Override
	public SENTENCE getFinalConclusion() {
		return finalConclusion;
	}

	@Override
	public List<IInference<SENTENCE>> getInferences() {
		return Collections.unmodifiableList(inferences);
	}

	@Override
	public Set<IInference<SENTENCE>> getInferences(SENTENCE conclusion) {
		return conclusions2inferences.get(conclusion);
	}

	@Override
	public void addInference(IInference<SENTENCE> inference) {
		inferences.add(inference);
		conclusions2inferences.put(inference.getConclusion(), inference);
	}

	@Override
	public void addInferences(Collection<IInference<SENTENCE>> inferences) {
		inferences.forEach(this::addInference);
	}

	@Override
	public boolean hasInferenceFor(SENTENCE conclusion) {
		return conclusions2inferences.containsKey(conclusion);
	}

	@JsonIgnore
	@Override
	public int getNumberOfRuleApplications() {
		return inferences.size();
	}

	@JsonIgnore
	@Override
	public int getNumberOfAxioms() {
		return conclusions2inferences.keySet().size();
	}

	@Override
	public int getSizeOfLargestInferencePremise() {
		return inferences.stream().mapToInt(inf -> inf.getPremises().size()).max()
				.orElseThrow(NoSuchElementException::new);
	}

	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		inferences.forEach(string::append);
		return string.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inferences == null) ? 0 : inferences.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proof<SENTENCE> other = (Proof<SENTENCE>) obj;
		if (inferences == null) {
			if (other.inferences != null)
				return false;
		} else if (!inferences.equals(other.inferences))
			return false;
		return true;
	}

}
