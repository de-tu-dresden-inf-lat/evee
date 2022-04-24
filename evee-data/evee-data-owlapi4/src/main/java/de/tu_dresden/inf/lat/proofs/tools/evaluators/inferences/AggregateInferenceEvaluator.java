/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.tools.evaluators.inferences;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IInferenceEvaluator;

/**
 * @author stefborg
 *
 */
public class AggregateInferenceEvaluator<S> implements IInferenceEvaluator<S> {

	private List<IInferenceEvaluator<S>> evaluators;
	private Function<DoubleSummaryStatistics, Double> aggregator;
	private String aggregatorDescription;

	public AggregateInferenceEvaluator() {
		this(DoubleSummaryStatistics::getSum, "sum");
	}

	public AggregateInferenceEvaluator(Function<DoubleSummaryStatistics, Double> aggregator,
			String aggregatorDescription) {
		this.evaluators = new ArrayList<>();
		this.aggregator = aggregator;
		this.aggregatorDescription = aggregatorDescription;
	}

	public void addEvaluator(IInferenceEvaluator<S> eval) {
		evaluators.add(eval);
	}

	@Override
	public Double evaluate(IInference<S> inf) {
		return aggregator.apply(evaluators.stream().mapToDouble(eval -> eval.evaluate(inf)).summaryStatistics());
	}

	@Override
	public String getDescription() {
		return "Aggregate (" + aggregatorDescription + ") of ["
				+ evaluators.stream().map(e -> e.getDescription()).collect(Collectors.joining(",")) + "]";
	}

}
