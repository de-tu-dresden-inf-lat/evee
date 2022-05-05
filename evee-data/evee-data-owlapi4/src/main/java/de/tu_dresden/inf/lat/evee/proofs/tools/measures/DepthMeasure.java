package de.tu_dresden.inf.lat.evee.proofs.tools.measures;

import java.util.List;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IRecursiveMeasure;

public class DepthMeasure<SENTENCE> implements IRecursiveMeasure<SENTENCE> {

	@Override
	public double leafValue(SENTENCE sentence) {
		return 1d;
	}

	@Override
	public double edgeValue(IInference<SENTENCE> inference, List<Double> childValues) {
		double max = 0d;
		for (Double d : childValues) {
			max = Math.max(max, d);
		}
		return 1d + max;
	}

	@Override
	public String getDescription() {
		return "Depth";
	}

}
