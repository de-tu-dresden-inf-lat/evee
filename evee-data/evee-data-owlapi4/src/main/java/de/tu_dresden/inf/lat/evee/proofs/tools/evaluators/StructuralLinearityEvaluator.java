package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Structural Linearity = Total number of levels / (Max number of nodes at any level * Total number of nodes)
 * The value 1 means the proof is perfectly linear, lesser values mean less linear.
 *
 * @author Christian Alrabbaa
 */
public class StructuralLinearityEvaluator<T> implements IProofEvaluator<T> {
    @Override
    public double evaluate(IProof<T> proof) throws ProofException {
        double totalLevels = getDepth(proof) + 1;
        int maxNodesInLevels = getMaxNodesInLevels(proof);
        int totalNodes = proof.getNumberOfAxioms();

        return totalLevels / (maxNodesInLevels * totalNodes);
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

    private int getMaxNodesInLevels(IProof<T> proof) {
        List<T> currentLevelAxioms = Collections.singletonList(proof.getFinalConclusion());

        return getMaxNodesInLevels(proof, currentLevelAxioms, 1);
    }

    /**
     * Return the maximum number of nodes across all levels of the proof. Reused nodes are counted multiple times.
     * */
    private int getMaxNodesInLevels(IProof<T> proof, List<T> currentLevelNodes, int maxNodes) {
        if(currentLevelNodes.isEmpty())
            return maxNodes;
        maxNodes = Math.max(maxNodes, currentLevelNodes.size());

        List<T> nextLevelNodes = currentLevelNodes.stream().map(proof::getInferences)
                        .flatMap(Collection::stream)
                        .map(IInference::getPremises)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        return getMaxNodesInLevels(proof, nextLevelNodes, maxNodes);
    }

    @Override
    public String getDescription() {
        return "Structural Linearity";
    }
}
