package de.tu_dresden.inf.lat.evee.proofs.tools.measures;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IRecursiveMeasure;

public class OWLAxiomSizeWeightedTreeSizeMeasure implements IRecursiveMeasure<OWLAxiom> {

	private OWLAxiomSizeMeasurer axiomSizeMeasurer = new OWLAxiomSizeMeasurer();

	@Override
	public double leafValue(OWLAxiom sentence) {
		return sentence.accept(axiomSizeMeasurer);
	}

	@Override
	public double edgeValue(IInference<OWLAxiom> inference, List<Double> childValues) {
		double sum = 0d;
		for (Double d : childValues) {
			sum += d;
		}
		return inference.getConclusion().accept(axiomSizeMeasurer) + sum;
	}

	@Override
	public String getDescription() {
		return "Weighted tree size";
	}

	
	
}
