package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Branching Linearity = (1 / TotalInternalNodes) * sumOf((outDegree - 1) / max(1, maxOutDegree - 1))
 * Linear proofs yield values closer to 0; bushier proofs yield values closer to 1.
 *
 * @author Christian Alrabbaa
 */
public class BranchingLinearityEvaluator<T> implements IProofEvaluator<T> {

    @Override
    public double evaluate(IProof<T> proof) throws ProofException {
        if(proof.getSizeOfLargestInferencePremise() <= 1)
            return 0;//proof is linear

        Set<T> internalNodes = getInternalNodes(proof);

        double sum = 0, maxOutDegree = proof.getSizeOfLargestInferencePremise();
        for(T axiom: internalNodes){
            sum += (getDegreeOf(axiom, proof) - 1)/Math.max(1,maxOutDegree - 1);
        }

        return (1.0/ internalNodes.size()) * sum;
    }

    private double getDegreeOf(T axiom, IProof<T> proof) {
        assert proof.getInferences(axiom).size() == 1:"Something is wrong! The axiom is provable in multiple ways in " +
                "the proof";

        return proof.getInferences(axiom).stream().map(IInference::getPremises).mapToLong(Collection::size).sum();
    }

    private Set<T> getInternalNodes(IProof<T> proof) {
        return proof.getInferences().stream()
                .filter(inf -> !inf.getPremises().isEmpty())
                .map(IInference::getConclusion)
                .collect(Collectors.toSet());
    }

    @Override
    public String getDescription() {
        return "Branching Linearity";
    }
}
