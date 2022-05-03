/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLTools;

/**
 * Count the number of OWLEntities in the conclusion that do no occur in the
 * premises (excluding top and bottom).
 * 
 * @author stefborg
 *
 */
public class C8SignatureDifferenceEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		Set<OWLEntity> sig = OWLTools.getSignature(inf.getConclusion());
		sig.removeAll(OWLTools.getSignature(inf.getPremises()));
		return (double) sig.size();
	}

	@Override
	public String getDescription() {
		return "C8: Number of new entities";
	}

}
