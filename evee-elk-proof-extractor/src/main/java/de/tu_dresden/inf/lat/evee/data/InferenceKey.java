package de.tu_dresden.inf.lat.evee.data;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;

public class InferenceKey<SENTENCE> {

	private final SENTENCE conclusion;
	private final String ruleName;

	public InferenceKey(SENTENCE conclusion, String ruleName) {
		this.conclusion = conclusion;
		this.ruleName = ruleName;
	}

	@JsonIgnore
	public SENTENCE getConclusion() {
		return conclusion;
	}

	@JsonIgnore
	public String getRuleName() {
		return ruleName;
	}

	@Override
	public String toString() {
		if (conclusion instanceof OWLAxiom)
			return SimpleOWLFormatter.format((OWLAxiom) conclusion) + " " + ruleName;
		return conclusion + " " + ruleName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conclusion == null) ? 0 : conclusion.hashCode());
		result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());
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
		InferenceKey<SENTENCE> other = (InferenceKey<SENTENCE>) obj;
		if (conclusion == null) {
			if (other.conclusion != null)
				return false;
		} else if (!conclusion.equals(other.conclusion))
			return false;
		if (ruleName == null) {
			if (other.ruleName != null)
				return false;
		} else if (!ruleName.equals(other.ruleName))
			return false;
		return true;
	}

}
