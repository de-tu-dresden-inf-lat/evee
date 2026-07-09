package de.tu_dresden.inf.lat.evee.proofs.tools.evaluators;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DirectedCutwidthEvaluator<T> implements IProofEvaluator<T> {
    @Override
    public double evaluate(IProof<T> proof) throws ProofException {
        return directedCutWidth(proof.getFinalConclusion(),proof,0);
    }

    @Override
    public String getDescription() {
        return "Directed Cutwidth";
    }

    public int directedCutWidth(T node, IProof<T> treeNodes, int depth) {
        List<T> children = treeNodes.getInferences(node).stream()
                        .map(IInference::getPremises)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        int childCount = children.size();
        List<Integer> childWidths = new ArrayList<>();

        for (T child : children) {
            childWidths.add(directedCutWidth(child, treeNodes, depth + 1));
        }

        Collections.sort(childWidths);
        int result = childCount;

        for (int i = 0; i < childCount; i++) {
            int width = childCount - i - 1 + childWidths.get(i);
            result = Math.max(result, width);
        }

        return result;
    }
}
