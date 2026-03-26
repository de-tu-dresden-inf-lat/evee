package de.tu_dresden.inf.lat.evee.proofs.tools.measures;

import java.util.List;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IRecursiveMeasure;

public class TreeSizeMeasure<SENTENCE> implements IRecursiveMeasure<SENTENCE> {

	@Override
	public double leafValue(SENTENCE sentence) {
		return 1d;
	}

	@Override
	public double edgeValue(IInference<SENTENCE> inference, List<Double> childValues) {
		double sum = 0d;
		for (Double d : childValues) {
			sum += d;
		}
		return 1d + sum;
	}

	@Override
	public String getDescription() {
		return "Tree size";
	}

}
