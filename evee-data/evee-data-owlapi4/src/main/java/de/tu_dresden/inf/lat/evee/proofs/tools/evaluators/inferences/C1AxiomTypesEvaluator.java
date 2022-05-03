/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;

/**
 * Count the number of axiom types in the inference.
 * 
 * @author stefborg
 *
 */
public class C1AxiomTypesEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		Set<AxiomType> types = new HashSet<>();
		types.add(inf.getConclusion().getAxiomType());
		for (OWLAxiom ax : inf.getPremises()) {
			types.add(ax.getAxiomType());
		}
		return (double) types.size();
	}

	@Override
	public String getDescription() {
		return "C1: Number of axiom types";
	}

}
