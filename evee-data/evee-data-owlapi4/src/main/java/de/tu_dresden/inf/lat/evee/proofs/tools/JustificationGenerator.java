package de.tu_dresden.inf.lat.evee.proofs.tools;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import java.util.*;

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

    @Deprecated
    public Set<Set<SENTENCE>> getJustifications3(SENTENCE sentence) throws ProofGenerationException {
        IProof<SENTENCE> proof = proofGenerator.getProof(sentence);
        System.out.println("Proof: ");
        System.out.println(proof);
        Queue<SENTENCE> toProcess = new LinkedList<>();
        toProcess.add(sentence);
        return getJustifications(proof, toProcess, new HashSet<>());
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
                System.out.println(inference);
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

    @Deprecated
    public Collection<Set<SENTENCE>> getJustifications2(SENTENCE sentence) throws ProofGenerationException {
        IProof<SENTENCE> proof = proofGenerator.getProof(sentence);
        System.out.println("Proof: ");
        System.out.println(proof);

        Set<IInference<SENTENCE>> reachableInf = new HashSet<>();
        fillReachableInferences(proof, proof.getFinalConclusion(), reachableInf);

        Set<SENTENCE> reachableAss = new HashSet<>();
        for(IInference<SENTENCE> inf:reachableInf){
            if(ProofTools.isAsserted(inf))
                reachableAss.add(inf.getConclusion());
        }

        Multimap<SENTENCE, Set<SENTENCE>> justifications = HashMultimap.create();
        reachableAss.forEach(a -> justifications.put(a, Collections.singleton(a)));

        Set<IInference<SENTENCE>> unprocessed = new HashSet<>(reachableInf);
        while(!unprocessed.isEmpty()){
            IInference<SENTENCE> next = unprocessed.iterator().next();
            unprocessed.remove(next);

        }

        return justifications.get(sentence);
    }

    @Deprecated
    public Set<SENTENCE> reachableAssertions(IProof<SENTENCE> proof) {
        Set<IInference<SENTENCE>> reachableInf = new HashSet<>();
        fillReachableInferences(proof, proof.getFinalConclusion(), reachableInf);
        Set<SENTENCE> reachableAss = new HashSet<>();
        for(IInference<SENTENCE> inf:reachableInf){
            if(ProofTools.isAsserted(inf))
                reachableAss.add(inf.getConclusion());
        }
        return reachableAss;
    }

    @Deprecated
    private void fillReachableInferences(IProof<SENTENCE> proof, SENTENCE conclusion, Set<IInference<SENTENCE>> toFill) {
        if(toFill.contains(conclusion))
            return;
        else {
            proof.getInferences(conclusion).forEach( inf -> {
               toFill.add(inf);
               inf.getPremises().forEach(p -> fillReachableInferences(proof,p,toFill));
            });
            return;
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

    /**
     * Probably not optimal - just implemented first approach that came to mind.
     */
    @Deprecated
    private Set<Set<SENTENCE>> getJustifications(IProof<SENTENCE> proof, Queue<SENTENCE> toProcess,
                                                 Set<SENTENCE> currentBranch) {
        if(currentBranch.contains(toProcess.peek())) {
            return new HashSet<>();
        }
        else if(toProcess.isEmpty()) {
            return Collections.singleton(Collections.emptySet());
        }
        else {
            System.out.println("to process: "+toProcess);
            SENTENCE head = toProcess.poll();
            currentBranch.add(head); // <-- we know it cannot contain it
            Set<Set<SENTENCE>> result = new HashSet<>();
            for(IInference<SENTENCE> inference: proof.getInferences(head)) {
                if(ProofTools.isAsserted(inference)) {
                    for(Set<SENTENCE> justification: getJustifications(proof, toProcess,currentBranch)){
                        justification.add(head);
                        result.add(justification);
                    }
                } else {
                    toProcess.addAll(inference.getPremises());
                    result.addAll(getJustifications(proof,toProcess,currentBranch));

                    // we poll once for every premise, instead of just remove the premises
                    // otherwise, we might accidentally remove duplicate occurrences of a premise
                    for(SENTENCE prem:inference.getPremises()){
                        toProcess.poll();
                    }
                }
            }
            toProcess.add(head);
            currentBranch.remove(head); // <-- we know there cannot have been duplicate additions
            return result;
        }
    }



}
