/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author stefborg
 * 
 *         Computes the sum of all values obtained for individual inferences
 *         from the measure described in [Horridge, Bail, Parsia, Sattler:
 *         Toward cognitive support for OWL justifications. Knowledge-Based
 *         Systems 53, 66--79, 2013].
 *
 */
public class JustificationComplexityEvaluator extends WeightedInferenceEvaluator<OWLAxiom> {

	public JustificationComplexityEvaluator() {
		super();
		addEvaluator(new C1AxiomTypesEvaluator(), 100d);
		addEvaluator(new C2ClassConstructorsEvaluator(), 10d);
		addEvaluator(new C3UniversalImplicationEvaluator(), 50d);
		addEvaluator(new C4SynonymOfThingEvaluator(), 50d);
		addEvaluator(new C5SynonymOfNothingEvaluator(), 50d);
		addEvaluator(new C6DomainAndNoExistentialEvaluator(), 50d);
		addEvaluator(new C7ModalDepthEvaluator(), 50d);
		addEvaluator(new C8SignatureDifferenceEvaluator(), 50d);
		addEvaluator(new C9AxiomTypeDiffEvaluator(), 50d);
		addEvaluator(new C10ClassConstructorDiffEvaluator(), 50d); // in the paper the weight is 1 !?
		addEvaluator(new C11LaconicGCICountEvaluator(), 100d);
		addEvaluator(new C12AxiomPathsEvaluator(), 10d);
	}

	@Override
	public String getDescription() {
		return "Justification complexity";
	}

}
