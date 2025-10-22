package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

/**
 * Weighted linearity score = {@value LinearityEvaluator#w1} *
 * {@link  de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.StructuralLinearityEvaluator Structural Linearity} +
 * {@value LinearityEvaluator#w2} *
 * {@link  de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.BranchingLinearityEvaluator Branching Linearity}
 *
 * @author Christian Alrabbaa
 */

public class LinearityEvaluator<T> implements IProofEvaluator<T> {
    private final BranchingLinearityEvaluator<T> be;
    private final StructuralLinearityEvaluator<T> se;
    private static final double w1 = 0.5, w2 = 0.5;

    public LinearityEvaluator(){
        be = new BranchingLinearityEvaluator<>();
        se = new StructuralLinearityEvaluator<>();
    }
    @Override
    public double evaluate(IProof<T> proof) throws ProofException {
        return (w1 * this.se.evaluate(proof)) + (w2 * this.be.evaluate(proof));
    }

    @Override
    public String getDescription() {
        return "Weighted Linearity";
    }
}
