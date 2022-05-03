/**
 * 
 */
package de.tu_dresden.inf.lat.evee.proofs.interfaces;

/**
 * @author stefborg
 *
 */
@FunctionalInterface
public interface IInferenceEvaluator<S> {

	public Double evaluate(IInference<S> inf);

	default public String getDescription() {
		return "";
	}

}
