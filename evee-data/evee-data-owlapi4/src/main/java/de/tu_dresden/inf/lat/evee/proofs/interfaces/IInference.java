package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonGeneric2StringConverter;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonString2AxiomConverter;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Christian Alrabbaa
 *
 */
@JsonDeserialize(as = Inference.class)
@JsonPropertyOrder({ "conclusion", "ruleName", "premises" })
public interface IInference<SENTENCE> {

	@JsonSerialize(converter = JsonGeneric2StringConverter.class)
	@JsonDeserialize(converter = JsonString2AxiomConverter.class)
	SENTENCE getConclusion();

	String getRuleName();

	@JsonSerialize(contentConverter = JsonGeneric2StringConverter.class)
	@JsonDeserialize(contentConverter = JsonString2AxiomConverter.class)
	List<? extends SENTENCE> getPremises();
}
