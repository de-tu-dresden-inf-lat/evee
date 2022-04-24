/**
 * 
 */
package de.tu_dresden.inf.lat.proofs.interfaces;

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
