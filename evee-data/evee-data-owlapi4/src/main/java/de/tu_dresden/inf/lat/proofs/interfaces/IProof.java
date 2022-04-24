package de.tu_dresden.inf.lat.proofs.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.proofs.json.JsonGeneric2StringConverter;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.tu_dresden.inf.lat.proofs.data.Proof;
import de.tu_dresden.inf.lat.proofs.json.JsonAxiom2StringConverter;
import de.tu_dresden.inf.lat.proofs.json.JsonString2AxiomConverter;

/**
 * @author Christian Alrabbaa
 *
 */
@JsonDeserialize(as = Proof.class)
public interface IProof<SENTENCE> {

	// Note: we use a list, as the order of inferences might be relevant,
	// if we choose the linear representation of proofs.
	List<IInference<SENTENCE>> getInferences();

	@JsonSerialize(converter = JsonGeneric2StringConverter.class)
	@JsonDeserialize(converter = JsonString2AxiomConverter.class)
	SENTENCE getFinalConclusion();

	/**
	 * Return all inferences that have the given axiom as a conclusion.
	 */

	Collection<IInference<SENTENCE>> getInferences(SENTENCE conclusion);

	void addInference(IInference<SENTENCE> inference);

	void addInferences(Collection<IInference<SENTENCE>> inference);

	boolean hasInferenceFor(SENTENCE conclusion);

	/**
	 * The Number of Rule application is equal to the number of inferences involved
	 * in a proof.
	 */
	@JsonIgnore
	int getNumberOfRuleApplications();

	/**
	 * The number of axioms involved in a proof. Single axioms can have multiple inferences.
	 */
	@JsonIgnore
	int getNumberOfAxioms();

	/**
	 * Return the size of the largest premise in all inferences involved in a proof.
	 */
	@JsonIgnore
	int getSizeOfLargestInferencePremise();
}
