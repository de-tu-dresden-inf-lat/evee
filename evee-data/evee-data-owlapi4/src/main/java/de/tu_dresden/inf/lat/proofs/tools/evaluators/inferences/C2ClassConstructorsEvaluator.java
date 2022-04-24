/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.proofs.tools.OWLTools;

/**
 * Count the number of concept constructors in the inference.
 * 
 * @author stefborg
 *
 */
public class C2ClassConstructorsEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		Set<ClassExpressionType> types = OWLTools.getClassExpressionTypes(inf.getPremises());
		types.addAll(OWLTools.getClassExpressionTypes(inf.getConclusion()));
		return (double) types.size();
	}

	@Override
	public String getDescription() {
		return "C2: Number of constructors";
	}

}
