package de.tu_dresden.inf.lat.evee.data;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

public class RecursiveProof<SENTENCE> implements IProof<SENTENCE> {

	private final List<RecursiveInference<SENTENCE>> inferences;

	private final MultiMap<SENTENCE, IInference<SENTENCE>> conclusions2inferences;
	private final MultiMap<SENTENCE, IInference<SENTENCE>> premises2inferences;

	private final SENTENCE finalConclusion;

	@JsonCreator
	public RecursiveProof(@JsonProperty("finalConclusion") SENTENCE finalConclusion,
			@JsonProperty("inferences") List<RecursiveInference<SENTENCE>> inferences) {
		this.finalConclusion = finalConclusion;
		this.inferences = new LinkedList<>(inferences);

		conclusions2inferences = new MultiMap<>();
		premises2inferences = new MultiMap<>();

		for (IInference<SENTENCE> inference : inferences) {
			conclusions2inferences.add(inference.getConclusion(), inference);
			for (SENTENCE premise : inference.getPremises()) {
				premises2inferences.add(premise, inference);
			}
		}
	}

	public RecursiveProof(SENTENCE finalConclusion) {
		this.finalConclusion = finalConclusion;
		inferences = new LinkedList<>();
		conclusions2inferences = new MultiMap<>();
		premises2inferences = new MultiMap<>();
	}

	@Override
	public SENTENCE getFinalConclusion() {
		return finalConclusion;
	}

	@Override
	public Collection<IInference<SENTENCE>> getInferences(SENTENCE conclusion) {
		return conclusions2inferences.get(conclusion);
	}

	@Override
	public Collection<IInference<SENTENCE>> getInferencesWithPremise(SENTENCE premise) {
		return premises2inferences.get(premise);
	}

	@Override
	public boolean hasInferenceFor(SENTENCE conclusion) {
		return conclusions2inferences.hasKey(conclusion);
	}

	@Override
	public boolean hasInferenceWithPremise(SENTENCE premise) { return premises2inferences.hasKey(premise); }

//	public RecursiveProof() {
//		inferences = new LinkedList<>();
//	}

	@Override
	public List<IInference<SENTENCE>> getInferences() {
		return Collections.unmodifiableList(inferences);
	}

	@Override
	public void addInference(IInference<SENTENCE> inference) {
		assert inference instanceof RecursiveInference;
		inferences.add((RecursiveInference<SENTENCE>) inference);
		conclusions2inferences.add(inference.getConclusion(), inference);
		for (SENTENCE premise : inference.getPremises()) {
			premises2inferences.add(premise, inference);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInferences(Collection<IInference<SENTENCE>> inferences) {
		Field field;
		try {
			field = RecursiveProof.class.getField("inferences");

			Type genericFieldType = field.getGenericType();

			if (genericFieldType instanceof RecursiveInference)
				inferences.stream().map(RecursiveInference.class::cast).forEach(x -> this.inferences.add(x));
			else
				assert false : "inferences are of type " + genericFieldType
						+ "\n Only Recursive Inferences are allowed";

		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@JsonIgnore
	@Override
	public int getNumberOfRuleApplications() {
		assert false : "Not implemented";
		return 0;
	}

	@JsonIgnore
	@Override
	public int getSizeOfLargestInferencePremise() {
		assert false : "Not implemented";
		return 0;
	}

	@Override
	public String toString() {

		// TODO Fix
		StringBuffer string = new StringBuffer();
		inferences.forEach(x -> {
			string.append(x.toString());
		});
//		inferences.forEach(string::append);
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
		RecursiveProof<SENTENCE> other = (RecursiveProof<SENTENCE>) obj;
		if (inferences == null) {
			if (other.inferences != null)
				return false;
		} else if (!inferences.equals(other.inferences))
			return false;
		return true;
	}

	@Override
	public int getNumberOfAxioms() {
		// TODO Auto-generated method stub
		return 0;
	}

}
