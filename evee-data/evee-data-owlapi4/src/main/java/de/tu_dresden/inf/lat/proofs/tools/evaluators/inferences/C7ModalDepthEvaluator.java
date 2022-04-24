/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IInferenceEvaluator;

/**
 * Calculate the maximal modal depth of the premises.
 * 
 * @author stefborg
 *
 */
public class C7ModalDepthEvaluator implements IInferenceEvaluator<OWLAxiom> {

	@Override
	public Double evaluate(IInference<OWLAxiom> inf) {
		return inf.getPremises().stream().map(C7ModalDepthEvaluator::modalDepth).reduce(0d, Double::max);
	}

	private static Double modalDepth(OWLAxiom ax) {
		return ax.getNestedClassExpressions().stream().map(C7ModalDepthEvaluator::modalDepth).reduce(0d, Double::max);
	}

	private static Double modalDepth(OWLClassExpression c) {
		if (c instanceof OWLNaryBooleanClassExpression) {
			return ((OWLNaryBooleanClassExpression) c).getOperands().stream().map(C7ModalDepthEvaluator::modalDepth)
					.reduce(0d, Double::max);
		}
		if (c instanceof OWLObjectComplementOf) {
			return modalDepth(((OWLObjectComplementOf) c).getOperand());
		}
		if (c instanceof OWLQuantifiedObjectRestriction) {
			return 1 + modalDepth(((OWLQuantifiedObjectRestriction) c).getFiller());
		}
		if (c instanceof OWLClass) {
			return 0d;
		}
		throw new UnsupportedOperationException("Unsupported class expression type:\n" + c);
	}

	@Override
	public String getDescription() {
		return "C7: Modal depth";
	}

}
