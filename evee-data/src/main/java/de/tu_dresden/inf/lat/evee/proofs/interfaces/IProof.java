package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import java.util.Collection;
import java.util.List;

import de.tu_dresden.inf.lat.evee.proofs.json.JsonGeneric2StringConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonString2AxiomConverter;

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
	 * Return all inferences that have the given sentence as a conclusion.
	 * @param conclusion The conclusion of the inferences
	 * @return A collection of inferences with the same conclusion
	 */
	Collection<IInference<SENTENCE>> getInferences(SENTENCE conclusion);

	/**
	 * Add a new inference step to this proof.
	 * @param inference The inference
	 */
	void addInference(IInference<SENTENCE> inference);

	/**
	 * Add new inference steps to this proof.
	 * @param inferences A collection of inferences
	 */
	void addInferences(Collection<IInference<SENTENCE>> inferences);

	/**
	 * Check if this proof contains an inference that derives the given sentence.
	 * @param conclusion The conclusion of the inference
	 * @return Whether such an inference exists
	 */
	boolean hasInferenceFor(SENTENCE conclusion);

	/**
	 * Return a new copy of the proof that does not contain duplicate inferences.
	 * @return The deduplicated proof
	 */
	IProof<SENTENCE> withoutDuplicateInferences();

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
