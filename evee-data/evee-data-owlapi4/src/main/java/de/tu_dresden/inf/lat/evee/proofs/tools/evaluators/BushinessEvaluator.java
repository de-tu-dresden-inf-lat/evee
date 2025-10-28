package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

import java.util.Collection;

/**
 * BushinessEvaluator = Tree size / Total number of levels
 * The value 1 means the proof is perfectly linear, greater values mean more bushy.
 *
 * @author Christian Alrabbaa
 */
public class BushinessEvaluator<T> implements IProofEvaluator<T> {

    @Override
    public double evaluate(IProof<T> proof) throws ProofException {
        double totalLevels = getDepth(proof) + 1;
        double totalNodes = getTreeSize(proof);
        return totalNodes/totalLevels;
    }

    private double getTreeSize(IProof<T> proof) {
        return (int) proof.getInferences().stream().map(IInference::getPremises).mapToLong(Collection::size).sum() + 1;
    }


    @Override
    public String getDescription() {
        return "Bushiness Score";
    }

    private double getDepth(IProof<T> proof) {
        return getDepth(proof, proof.getFinalConclusion(), 0);
    }

    private int getDepth(IProof<T> proof, T currentAxiom, int depth) {
        int maxDepth = depth;
        for (IInference<T> inf: proof.getInferences(currentAxiom)){
            for (T axiom:inf.getPremises()){
                maxDepth = Math.max(maxDepth, getDepth(proof, axiom, depth+1));
            }
        }
        return maxDepth;
    }
}
