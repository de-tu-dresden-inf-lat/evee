/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;

/**
 * Check whether the axiom type of the conclusion does not occur among the
 * premises.
 * 
 * @author stefborg
 *
 */
public class C9AxiomTypeDiffEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		if (inf.getPremises().stream().map(ax -> ax.getAxiomType())
				.noneMatch(t -> t.equals(inf.getConclusion().getAxiomType()))) {
			return 1d;
		} else {
			return 0d;
		}
	}

	@Override
	public String getDescription() {
		return "C9: New axiom type";
	}

}
