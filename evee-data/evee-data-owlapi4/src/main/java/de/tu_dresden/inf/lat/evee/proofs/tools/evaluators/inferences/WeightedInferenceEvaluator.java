/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.inferences;

import java.util.DoubleSummaryStatistics;
import java.util.function.Function;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;

/**
 * @author stefborg
 *
 */
public class WeightedInferenceEvaluator<S> extends AggregateInferenceEvaluator<S> {

	public WeightedInferenceEvaluator() {
		super();
	}

	public WeightedInferenceEvaluator(Function<DoubleSummaryStatistics, Double> aggregator,
			String aggregatorDescription) {
		super(aggregator, aggregatorDescription);
	}

	public void addEvaluator(IInferenceEvaluator<S> eval, Double weight) {
		super.addEvaluator(new IInferenceEvaluator<S>() {
			@Override
			public Double evaluate(IInference<S> inf) {
				return weight * eval.evaluate(inf);
			}

			@Override
			public String getDescription() {
				return eval.getDescription() + "(weight " + String.format("%0.2f", weight) + ")";
			}
		});
	}

	@Override
	public String getDescription() {
		return "Weighted " + super.getDescription();
	}

}
