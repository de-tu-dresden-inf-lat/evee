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

    private ONTOLOGY lastOntology = null;

    public void setOntology(ONTOLOGY ontology) {
        if (lastOntology == null || !lastOntology.equals(ontology)) {
            this.proofGenerator.setOntology(ontology);
            basisChanged = true;
        }
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

    public boolean basisChanged = false;
    public Map<SENTENCE, Set<SENTENCE>> reachableCache = new HashMap<>();
    public Map<SENTENCE, Set<Set<SENTENCE>>> justificationCache = new HashMap<>();

    public Set<Set<SENTENCE>> getJustifications2(SENTENCE sentence) {

        if(basisChanged) {
            long start = System.currentTimeMillis();
            IProof<SENTENCE> proof = null;
            try {
                proof = proofGenerator.getProof(sentence);
            } catch (ProofGenerationException e) {
                System.out.println("WARNING: dangerous use of exception");
                System.out.println("proof generation failed - assume not entailed");
                System.out.println("No proof, no justification");
                return Collections.emptySet();
            }

            if (proof.getInferences(sentence).isEmpty()) {
                System.out.println("No proof, no justification");
                return Collections.emptySet();
            }


            // first check whether anything relevant changed for this sentence since last time
            Set<SENTENCE> reachable = ProofTools.reachableAssertions(proof);
            System.out.println("Reachable:");
            reachable.forEach(System.out::println);
            if (reachableCache.containsKey(sentence)) {
                if (reachableCache.get(sentence).containsAll(reachable))
                    return justificationCache.get(sentence);
            }
            reachableCache.put(sentence, reachable);
            //System.out.println(proof);

            System.out.println("Inferences: ");
            proof.getInferences().forEach(System.out::println);

            Set<IInference<SENTENCE>> inferences = new HashSet<>(proof.getInferences());

            // remove self-loops, as they won't help us
            for(IInference<SENTENCE> inference:proof.getInferences()){
                for(SENTENCE premise:inference.getPremises())
                    if(premise.equals(inference.getConclusion()))
                        inferences.remove(inference);
            }
            System.out.println("Starting with " + inferences.size());

            SetMultimap<SENTENCE, Set<SENTENCE>> sentence2justification = HashMultimap.create();
            SetMultimap<SENTENCE, SENTENCE> sentence2reachable = HashMultimap.create();

            Set<IInference<SENTENCE>> toProcess = new HashSet<>(inferences);

            Set<SENTENCE> changed = new HashSet<>();
            Set<IInference<SENTENCE>> remove = new HashSet<>();

            for (IInference<SENTENCE> inference : inferences) {
                SENTENCE conclusion = inference.getConclusion();
                if (inference.getPremises().isEmpty()) {
                    changed.add(conclusion);
                    remove.add(inference);
                    if (ProofTools.isAsserted(inference)) {
                        sentence2justification.put(conclusion, Collections.singleton(conclusion));
                        sentence2reachable.put(conclusion, conclusion);
                    } else
                        sentence2justification.put(conclusion, Collections.emptySet());
                }
            }
            inferences.remove(remove);

            System.out.println("changed: " + changed);
            System.out.println("inferences: " + inferences.size());

            while (!changed.isEmpty()) {
                System.out.println("New round!");
                System.out.println("changed: " + changed.size());
                Set<SENTENCE> newChanged = new HashSet<>();
                for (IInference<SENTENCE> inference : inferences) {

                    if (affected(inference, changed, sentence2justification)) {
                    /*if (inference.getPremises()
                            .stream()
                            .anyMatch(changed::contains) &&
                            inference.getPremises()
                                    .stream()
                                    .allMatch(sentence2justification::containsKey)) {*/

                        //System.out.println("Dirty inference: ");
                        //System.out.println(inference);

                        //Set<SENTENCE> newReachable = new HashSet<>();
                        inference.getPremises()
                                .stream()
                                .map(sentence2reachable::get)
                                .forEach(x -> sentence2reachable.putAll(inference.getConclusion(), x));

                        Collection<Set<Set<SENTENCE>>> justs = inference.getPremises()
                                .stream()
                                .map(sentence2justification::get)
                                .collect(Collectors.toSet());
                        Set<Set<SENTENCE>> newJust = GeneralTools.unions(justs);
                        Set<Set<SENTENCE>> minimalNewJust = new HashSet<>();
                        for (Set<SENTENCE> just : newJust) {
                            if (!newJust.stream()
                                    .filter(x -> !x.equals(just))
                                    .anyMatch(x -> just.containsAll(x))
                            )
                                minimalNewJust.add(just);
                        }
                        newJust = minimalNewJust;

                        SENTENCE conclusion = inference.getConclusion();

                        if (!sentence2justification.get(conclusion).containsAll(newJust)) {
                            sentence2justification.putAll(inference.getConclusion(),
                                    newJust);
                            newChanged.add(inference.getConclusion());
                        }
                    }
                }
                changed = newChanged;
            }

            System.out.println("Justifying took " + (System.currentTimeMillis() - start));

            for (SENTENCE conclusion : sentence2reachable.keys()) {
                reachableCache.put(conclusion, sentence2reachable.get(conclusion));
                justificationCache.put(conclusion, sentence2justification.get(conclusion));
            }
        }
        Set<Set<SENTENCE>> result;
        if(justificationCache.containsKey(sentence))
            result = justificationCache.get(sentence);
        //if (sentence2justification.containsKey(sentence))
        //    result = sentence2justification.get(sentence);
        else
            result= new HashSet<>();

//        justificationCache.put(sentence,result);

        basisChanged = false;

        return result;
    }

    private boolean affected(
            IInference<? extends SENTENCE> inference,
            Set<SENTENCE> changed,
            Multimap<SENTENCE,Set<SENTENCE>> sentence2Justification) {
        boolean changeAffected=false;
        for(SENTENCE premise: inference.getPremises()){
            if(changed.contains(premise))
                changeAffected=true;
            if(!sentence2Justification.containsKey(premise))
                return false;
        }
        return changeAffected;
    }

}
