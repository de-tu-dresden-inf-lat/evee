/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;

/**
 * Count the number of concept constructors in the conclusion that do not occur
 * in the premises.
 * 
 * @author stefborg
 *
 */
public class C10ClassConstructorDiffEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		Set<ClassExpressionType> types = OWLTools.getClassExpressionTypes(inf.getConclusion());
		types.removeAll(OWLTools.getClassExpressionTypes(inf.getPremises()));
		return (double) types.size();
	}

	@Override
	public String getDescription() {
		return "C10: Number of new constructors";
	}

}
