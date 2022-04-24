package de.tu_dresden.inf.lat.data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;

public class RecursiveInference<SENTENCE> implements IInference<SENTENCE> {

	private final InferenceKey<SENTENCE> inferenceKey;
	private final List<RecursiveInference<SENTENCE>> inferenceDetails;

	@JsonCreator
	public RecursiveInference(@JsonProperty("conclusion") SENTENCE conclusion,
			@JsonProperty("ruleName") String ruleName, List<RecursiveInference<SENTENCE>> premise) {

		this.inferenceKey = new InferenceKey<SENTENCE>(conclusion, ruleName);
		this.inferenceDetails = new LinkedList<>(premise);
	}

	@Override
	public SENTENCE getConclusion() {
		return inferenceKey.getConclusion();

	}

	@JsonIgnore
	@Override
	public List<? extends SENTENCE> getPremises() {
		return inferenceDetails.stream().map(RecursiveInference::getConclusion).collect(Collectors.toList());
	}

//	@JsonProperty("Conclusion")
	@JsonIgnore
	public String getConclusionStr() {
		return inferenceKey.getConclusion().toString();

	}

//	@JsonProperty("Rule")
	@Override
	public String getRuleName() {
		return inferenceKey.getRuleName();
	}

	@JsonProperty("premises")
	public List<RecursiveInference<SENTENCE>> getPremiseAsRecursiveInferences() {
		return inferenceDetails;
	}

	@JsonIgnore
	@Override
	public String toString() {
		return inferenceKey + "\t <- \t" + String.join(" , ",
				inferenceDetails.stream().map(RecursiveInference::getConclusionStr).collect(Collectors.toList()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inferenceDetails == null) ? 0 : inferenceDetails.hashCode());
		result = prime * result + ((inferenceKey == null) ? 0 : inferenceKey.hashCode());
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
		RecursiveInference<SENTENCE> other = (RecursiveInference<SENTENCE>) obj;
		if (inferenceDetails == null) {
			if (other.inferenceDetails != null)
				return false;
		} else if (!inferenceDetails.equals(other.inferenceDetails))
			return false;
		if (inferenceKey == null) {
			if (other.inferenceKey != null)
				return false;
		} else if (!inferenceKey.equals(other.inferenceKey))
			return false;
		return true;
	}

}
