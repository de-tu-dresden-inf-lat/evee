package de.tu_dresden.inf.lat.evee.proofs.tools;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

/**
 * Extract justifications from the derivation structures provided by the proof generator.
 *
 * Note: if the derivation structures are incomplete (for instance, because they just return one proof),
 * then also the set of justifications will be incomplete.
 *
 */
public class JustificationGenerator<SENTENCE,ONTOLOGY> {
    private IProofGenerator<SENTENCE,ONTOLOGY> proofGenerator;

    public JustificationGenerator(IProofGenerator proofGenerator) {
        this.proofGenerator=proofGenerator;
    }

    public void setOntology(ONTOLOGY ontology) {
        this.proofGenerator.setOntology(ontology);
    }

    public Set<Set<SENTENCE>> getJustifications(SENTENCE sentence) throws ProofGenerationException {
        IProof<SENTENCE> proof = proofGenerator.getProof(sentence);
        Queue<SENTENCE> toProcess = new LinkedList<>();
        toProcess.add(sentence);
        return getJustifications(proof, toProcess);
    }

    /**
     * Get all justifications that include the given sentence.
     *
     * TODO there is probably a more efficient way to do this
     */
    public Set<Set<SENTENCE>> getJustifications(SENTENCE sentence, SENTENCE include) throws ProofGenerationException {
        Set<Set<SENTENCE>> result = new HashSet<>();
        for(Set<SENTENCE> just:getJustifications(sentence))
            if(just.contains(include))
                result.add(just);
        return result;
    }

    /**
     * Probably not optimal - just implemented first approach that came to mind.
     */
    private Set<Set<SENTENCE>> getJustifications(IProof<SENTENCE> proof, Queue<SENTENCE> toProcess) {
        if(toProcess.isEmpty())
            return new HashSet<>();
        else {
            SENTENCE head = toProcess.poll();
            Set<Set<SENTENCE>> result = new HashSet<>();
            for(IInference<SENTENCE> inference: proof.getInferences(head)) {
                if(ProofTools.isAsserted(inference)) {
                    for(Set<SENTENCE> justification: getJustifications(proof, toProcess)){
                        justification.add(head);
                        result.add(justification);
                    }
                } else {
                    toProcess.addAll(inference.getPremises());
                    result.addAll(getJustifications(proof,toProcess));

                    // we poll once for every premise, instead of just remove the premises
                    // otherwise, we might accidentally remove duplicate occurrences of a premise
                    for(SENTENCE prem:inference.getPremises()){
                        toProcess.poll();
                    }
                }
            }
            toProcess.add(head);
            return result;
        }
    }



}
