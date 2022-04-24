package de.tu_dresden.inf.lat.proofs.tools;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofEvaluator;
import de.tu_dresden.inf.lat.proofs.interfaces.IRecursiveMeasure;

import java.util.*;
import java.util.stream.Collectors;

public class RecursiveProofEvaluator<SENTENCE> implements IProofEvaluator<SENTENCE> {

    private final IRecursiveMeasure<SENTENCE> measure;

    public RecursiveProofEvaluator(IRecursiveMeasure<SENTENCE> measure) {
        this.measure=measure;
    }

    @Override
    public double evaluate(IInference<SENTENCE> inference) {
        List<Double> premises =
                inference.getPremises()
                        .stream()
                        .map(measure::leafValue)
                        .collect(Collectors.toList());
        return measure.edgeValue(inference, premises);
    }

    @Override
    public double evaluate(IProof<SENTENCE> proof) throws ProofException {
        return evaluate(
                proof,
                proof.getFinalConclusion(),
                new HashSet<SENTENCE>(), // set of processed nodes makes sure we always terminate
                new HashMap<SENTENCE,Double>()); // cache makes sure this runs linear in size of proof
    }

    /**
     * We allow here for inferences whose premises have no inferences themselves, and treat those as the leafs.
     * If a sentence has more than one inference, we built the sum.
     */
    private double evaluate(
            IProof<SENTENCE> proof, SENTENCE sentence, Set<SENTENCE> processed, Map<SENTENCE,Double> cache)
            throws ProofException {

        if(processed.contains(sentence))
            throw new ProofException.CircularProofException("circular argument on "+sentence);
        else if(!proof.hasInferenceFor(sentence))
            return measure.leafValue(sentence);
        else if(cache.containsKey(sentence))
            return cache.get(sentence);
        else {
            // careful: we modify the argument 'processed'
            processed.add(sentence); // same sentence cannot be added twice due above if
            double value = 0;
            for(IInference<SENTENCE> inference: proof.getInferences(sentence)) {
                List<Double> values = new ArrayList<>(inference.getPremises().size());
                for(SENTENCE premise: inference.getPremises()){
                    values.add(evaluate(proof,premise,processed,cache));
                }
                value += measure.edgeValue(inference, values);
            }
            processed.remove(sentence); //
            cache.put(sentence, value);
            return value;
        }
    }

	@Override
	public String getDescription() {
		return measure.getDescription();
	}
}
