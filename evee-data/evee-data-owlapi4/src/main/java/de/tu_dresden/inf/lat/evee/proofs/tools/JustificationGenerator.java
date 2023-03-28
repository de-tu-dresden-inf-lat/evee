package de.tu_dresden.inf.lat.evee.proofs.tools;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extract justifications from the derivation structures provided by the proof generator.
 *
 * Note: if the derivation structures are incomplete (for instance, because they just return one proof),
 * then also the set of justifications will be incomplete.
 *
 */
public class JustificationGenerator<SENTENCE,ONTOLOGY> {
    private final IProofGenerator<SENTENCE,ONTOLOGY> proofGenerator;

    public JustificationGenerator(IProofGenerator proofGenerator) {
        this.proofGenerator=proofGenerator;
    }

    public void setOntology(ONTOLOGY ontology) {
        this.proofGenerator.setOntology(ontology);
    }

    public Collection<Set<SENTENCE>> getJustifications(SENTENCE sentence) throws ProofGenerationException {
        IProof<SENTENCE> proof = proofGenerator.getProof(sentence);

//        System.out.println("Proof: ");
//        System.out.println(proof);

        justificationBuffer.clear();

        return getJustifications(proof, proof.getFinalConclusion(), new HashSet<>());
    }

    private Map<SENTENCE, Set<Set<SENTENCE>>> justificationBuffer = new HashMap<>();

    /**
     * Problem with this implementation: if the derivation structure contains a lot of cycles,
     * the caching mechanism will not fire and may lead to an exponential execution tree
     */
    private Set<Set<SENTENCE>> getJustifications(IProof<SENTENCE> proof, SENTENCE sentence, Set<SENTENCE> branch) {
        if(justificationBuffer.containsKey(sentence)){
//            System.out.println("justification already known for "+sentence);
            return justificationBuffer.get(sentence);
        } else if(branch.contains(sentence)) {
//            System.out.println("Cycle in branch - returning no justification for "+sentence);
            return Collections.emptySet();
        }
        else {
            Set<Set<SENTENCE>> justifications = new HashSet<>();
            boolean cycle = false;
            for(IInference<SENTENCE> inference: proof.getInferences(sentence)){
//                System.out.println("Justifications based on ");
//                System.out.println(inference);
                if (ProofTools.isAsserted(inference)) {
                    justifications.add(Collections.singleton(sentence));
//                    System.out.println("For "+sentence);
//                    System.out.println("A singleton");
                }
                else {
                    List<Set<Set<SENTENCE>>> premiseJustifications = new LinkedList<>();
                    Set<SENTENCE> branch2 = new HashSet<>(branch);
                    branch2.add(sentence);
                    inference.getPremises()
                            .forEach(s -> premiseJustifications.add(getJustifications(proof, s, branch2)));
//                    System.out.println("For " + sentence);
                    if(premiseJustifications.contains(Collections.emptySet())){
//                        System.out.println("cycle found - not caching since justification may be incomplete");
                        cycle=true;
                    } else {
//                        System.out.println("Union over " + premiseJustifications);
                        justifications.addAll(GeneralTools.unions(premiseJustifications));
//                        System.out.println("Which is " + GeneralTools.unions(premiseJustifications));
                    }
                }
            }
//            System.out.println("Putting as justification for "+sentence+": "+justifications);
            if(!cycle)
                justificationBuffer.put(sentence, justifications);
            return justifications;
        }
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

    public Set<Set<SENTENCE>> getJustifications2(SENTENCE sentence, SENTENCE include) throws ProofGenerationException {
        Set<Set<SENTENCE>> result = new HashSet<>();
        for(Set<SENTENCE> just:getJustifications2(sentence))
            if(just.contains(include))
                result.add(just);
        return result;
    }
    public Set<Set<SENTENCE>> getJustifications2(SENTENCE sentence) throws ProofGenerationException {
        IProof<SENTENCE> proof = proofGenerator.getProof(sentence);

        if(proof.getInferences(sentence).isEmpty()){
            System.out.println("No proof, no justification");
            return Collections.emptySet();
        }

        System.out.println(proof);

        Set<IInference<SENTENCE>> inferences = new HashSet<>(proof.getInferences());
        //ProofTools.fillReachableInferences(proof, sentence, inferences);

        System.out.println("Starting with "+inferences.size());

        SetMultimap<SENTENCE,Set<SENTENCE>> sentence2justification = HashMultimap.create();

        Set<IInference<SENTENCE>> toProcess = new HashSet<>(inferences);

        Set<SENTENCE> changed = new HashSet<>();
        Set<IInference<SENTENCE>> remove = new HashSet<>();

        for(IInference<SENTENCE> inference: inferences){
            SENTENCE conclusion = inference.getConclusion();
            if(inference.getPremises().isEmpty()){
                changed.add(conclusion);
                remove.add(inference);
                if(ProofTools.isAsserted(inference))
                    sentence2justification.put(conclusion, Collections.singleton(conclusion));
                else
                    sentence2justification.put(conclusion, Collections.emptySet());
            }
        }
        inferences.remove(remove);

        System.out.println("changed: "+changed);
        System.out.println("inferences: "+inferences.size());

        while(!changed.isEmpty()){
            System.out.println("New round!");
            System.out.println("changed: "+changed.size());
            Set<SENTENCE> newChanged = new HashSet<>();
            for(IInference<SENTENCE> inference: inferences){

                if(inference.getPremises()
                        .stream()
                        .anyMatch(changed::contains) &&
                    inference.getPremises()
                            .stream()
                            .allMatch(sentence2justification::containsKey)){

                    //System.out.println("Dirty inference: ");
                    //System.out.println(inference);

                    Collection<Set<Set<SENTENCE>>> justs = inference.getPremises()
                            .stream()
                            .map(sentence2justification::get)
                            .collect(Collectors.toSet());
                    Set<Set<SENTENCE>> newJust = GeneralTools.unions(justs);

                    SENTENCE conclusion = inference.getConclusion();

                    if(!sentence2justification.get(conclusion).containsAll(newJust)) {
                        sentence2justification.putAll(inference.getConclusion(),
                                newJust);
                        newChanged.add(inference.getConclusion());
                    }
                }
            }
            changed=newChanged;
        }
        if(sentence2justification.containsKey(sentence))
            return sentence2justification.get(sentence);
        else
            return new HashSet<>();
    }

}
