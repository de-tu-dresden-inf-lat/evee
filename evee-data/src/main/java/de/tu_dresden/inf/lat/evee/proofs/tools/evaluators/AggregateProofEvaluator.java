/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import java.util.DoubleSummaryStatistics;
import java.util.function.Function;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInferenceEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.ProofTools;

/**
 * @author stefborg
 *
 */
public class AggregateProofEvaluator<S> implements IProofEvaluator<S> {

	private IInferenceEvaluator<S> inferenceEvaluator;
	private Function<DoubleSummaryStatistics, Double> aggregator;
	private String aggregatorDescription;

	public AggregateProofEvaluator(IInferenceEvaluator<S> inferenceEvaluator) {
		this(inferenceEvaluator, DoubleSummaryStatistics::getSum, "sum");
	}

	public AggregateProofEvaluator(IInferenceEvaluator<S> inferenceEvaluator,
			Function<DoubleSummaryStatistics, Double> aggregator, String aggregatorDescription) {
		this.inferenceEvaluator = inferenceEvaluator;
		this.aggregator = aggregator;
		this.aggregatorDescription = aggregatorDescription;
	}

	protected void setInferenceEvaluator(IInferenceEvaluator<S> inferenceEvaluator) {
		this.inferenceEvaluator = inferenceEvaluator;
	}

	@Override
	public double evaluate(IProof<S> proof) {
		DoubleSummaryStatistics stats = proof.getInferences().stream().filter(inf -> !ProofTools.isAsserted(inf))
				.mapToDouble(inferenceEvaluator::evaluate).summaryStatistics();
		if (stats.getCount() == 0) {
			stats.accept(0d);
		}
		return aggregator.apply(stats);
	}

	@Override
	public String getDescription() {
		return "Aggregate (" + aggregatorDescription + ") of " + inferenceEvaluator.getDescription();
	}

}
