package de.tu_dresden.inf.lat.proofs.interfaces;

import java.util.List;

public interface IRecursiveMeasure<SENTENCE> {

	// value for a single axiom without premises
	double leafValue(SENTENCE ax);

	// recursive computation of the value
	// important: second argument is a multiset!
	double edgeValue(IInference<SENTENCE> inference, List<Double> childValues);
	
	String getDescription();
}
