package de.tu_dresden.inf.lat.evee.proofs.data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;

/**
 * @author Christian Alrabbaa
 *
 */
public class Inference<SENTENCE> implements IInference<SENTENCE> {

	private final SENTENCE conclusion;
	private final String ruleName;
	private final List<? extends SENTENCE> premises; // we want to keep the ordering fixed, hence List

	@JsonCreator
	public Inference(@JsonProperty("conclusion") SENTENCE conclusion, @JsonProperty("ruleName") String ruleName,
			@JsonProperty("premises") List<? extends SENTENCE> premises) {
		this.conclusion = conclusion;
		this.ruleName = ruleName;
		this.premises = new LinkedList<>(premises);
	}

	@Override
	public SENTENCE getConclusion() {
		return conclusion;
	}

	@Override
	public String getRuleName() {
		return ruleName;
	}

	@Override
	public List<? extends SENTENCE> getPremises() {
		return premises;
	}

	// @JsonProperty("Premise")
	// public String getPremiseAsStrings() {
	// return premises.toString();
	// }

	@Override
	public String toString() {
		return premises.stream().map(this::format).collect(Collectors.toList()) + "\n--------------------- " + ruleName
				+ "\n" + format(conclusion) + "\n\n";
		/*
		 * return format(conclusion) + "\nwas derived from " + premises.stream().map(x
		 * -> format(x)).collect(Collectors.toList()) + "\nusing " + ruleName + "\n";
		 */
	}

	private String format(SENTENCE sentence) {
		if (sentence instanceof OWLAxiom) {
			return SimpleOWLFormatter.format((OWLAxiom) sentence);
		} else
			return sentence.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conclusion == null) ? 0 : conclusion.hashCode());
		result = prime * result + ((premises == null) ? 0 : premises.hashCode());
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
		Inference<SENTENCE> other = (Inference<SENTENCE>) obj;
		if (conclusion == null) {
			if (other.conclusion != null)
				return false;
		} else if (!conclusion.equals(other.conclusion))
			return false;
		if (premises == null) {
			if (other.premises != null)
				return false;
		} else if (!premises.equals(other.premises))
			return false;
		if (ruleName == null) {
			if (other.ruleName != null)
				return false;
		} else if (!ruleName.equals(other.ruleName))
			return false;
		return true;
	}

}
