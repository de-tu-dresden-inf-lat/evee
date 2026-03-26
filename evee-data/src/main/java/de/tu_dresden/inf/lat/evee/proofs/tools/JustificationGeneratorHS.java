package de.tu_dresden.inf.lat.evee.proofs.tools;

import de.tu_dresden.inf.lat.evee.general.tools.Pair;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import java.util.*;

/**
 * Uses the hitting set approach.
 */
public class JustificationGeneratorHS<SENTENCE,ONTOLOGY>  {

    public interface Converter<SENTENCE,ONTOLOGY> {
        ONTOLOGY convert(Set<SENTENCE> sentences);
    }

    private final IProofGenerator<SENTENCE,ONTOLOGY> proofGenerator;
    private final Converter<SENTENCE,ONTOLOGY> converter;

    public JustificationGeneratorHS(IProofGenerator proofGenerator, Converter<SENTENCE,ONTOLOGY> converter) {
        this.proofGenerator=proofGenerator;
        this.converter=converter;
    }

    private Set<SENTENCE> sentences;

    private Set<SENTENCE> fixed = new HashSet<>();

    public void setSentences(Set<SENTENCE> sentences) {
        this.sentences=new HashSet<>(sentences);
    }

    public void setFixed(Set<SENTENCE> fixed) {
        this.fixed=fixed;
        sentences.removeAll(fixed);
    }

    public Set<Set<SENTENCE>> allJustifications(SENTENCE sentence) {
      //  System.out.print("Finding justifications...");
      //  assert(fixed.stream().noneMatch(sentences::contains));
        Set<SENTENCE> _fixed = new HashSet<>(fixed);
        if(entails(Collections.emptySet(), _fixed, sentence))
            return Collections.singleton(Collections.emptySet());

        Set<Set<SENTENCE>> justificationContainer = new HashSet<>();
        findJustifications(sentence, Collections.emptySet(), new HashSet<>(), justificationContainer,_fixed);

        for(Set<SENTENCE> justification:justificationContainer){
            if(!entails(justification,_fixed,sentence)){
                System.out.println("Justification:");
                justification.forEach(System.out::println);
                System.out.println("Fixed:");
                _fixed.forEach(System.out::println);
                System.out.println("Sentence: ");
                System.out.println(sentence);
            }
            assert entails(justification, _fixed, sentence);
        }

     //   System.out.println("Done.");

        return justificationContainer;
    }

    public void findJustifications(SENTENCE sentence,
                                   Set<SENTENCE> currentPath,
                                   Set<Set<SENTENCE>> knownPaths,
                                   Set<Set<SENTENCE>> justificationsFound,
                                   Set<SENTENCE> fixed) {

      //  System.out.println("current path: "+currentPath.size());
      //  System.out.println("proving "+sentence);

        if(knownPaths.stream().anyMatch(knownPath -> currentPath.containsAll(knownPath)))
            return; // path subsumed

        Set<SENTENCE> currentSet = new HashSet<>(sentences);
        currentSet.removeAll(currentPath);

        Optional<Set<SENTENCE>> firstJust = singleJustification(sentence, currentSet, fixed);
        if(!firstJust.isPresent()){
            // end of path
            knownPaths.add(currentPath);
            return;
        }

        assert(entails(firstJust.get(),fixed,sentence));
        // new justification -- continue search
        justificationsFound.add(firstJust.get());

        for(SENTENCE next: firstJust.get()){
            Set<SENTENCE> newPath = new HashSet<>(currentPath);
            newPath.add(next);
            findJustifications(sentence, newPath, knownPaths, justificationsFound,fixed);
        }
    }

    public Optional<Set<SENTENCE>> singleJustification(SENTENCE sentence) {
        if(entails(Collections.emptySet(), fixed, sentence))
            return Optional.of(Collections.emptySet());
        return singleJustification(sentence, sentences, fixed);
    }

    public Optional<Set<SENTENCE>> singleJustification(SENTENCE sentence, Set<SENTENCE> sentences, Set<SENTENCE> fixed) {
        boolean entailed = entails(sentences,fixed,sentence);
       /* System.out.println("sentence: "+sentence);
        System.out.println("variable set: ");
        sentences.forEach(System.out::println);
        System.out.println("fixed set: ");
        fixed.forEach(System.out::println);
        System.out.println("entailed: "+entailed);
        System.out.println("===================");*/
        if(!entailed){
            return Optional.empty();
        } else if(sentences.size()<2) {
            assert sentences.size()==1;
            return Optional.of(sentences);
        } else {
            Pair<Set<SENTENCE>> subsets = split(sentences);
            Set<SENTENCE> first = subsets.getFirst();
            Set<SENTENCE> second = subsets.getSecond();
            assert first.size()!=sentences.size() && second.size()!=sentences.size() : sentences.size()+" "+first.size()+" "+second.size();
            Optional<Set<SENTENCE>> partialResult = singleJustification(sentence,first,fixed);
            if(!partialResult.isPresent()){
                partialResult = singleJustification(sentence, second, fixed);
            }
            if(!partialResult.isPresent()){
                Set<SENTENCE> newFixed = new HashSet<>();
                newFixed.addAll(fixed);
                newFixed.addAll(second);
                partialResult = singleJustification(sentence, first, newFixed);
                if(!partialResult.isPresent()){
                    System.out.println("first: ");
                    first.forEach(System.out::println);
                    System.out.println("second: ");
                    second.forEach(System.out::println);
                    System.out.println("global fixed:");
                    this.fixed.forEach(System.out::println);
                    System.out.println("fixed:");
                    fixed.forEach(System.out::println);
                    System.out.println("New fixed: ");
                    newFixed.forEach(System.out::println);
                    System.out.println("Sentence: "+sentence);
                }
                assert(partialResult.isPresent());
                Set<SENTENCE> result1 = partialResult.get();
                newFixed = new HashSet<>();
                newFixed.addAll(fixed);
                newFixed.addAll(result1);
                partialResult = singleJustification(sentence, second, newFixed);
                if(!partialResult.isPresent()){
                    System.out.println("first: ");
                    first.forEach(System.out::println);
                    System.out.println("result1: ");
                    result1.forEach(System.out::println);
                    System.out.println("second: ");
                    second.forEach(System.out::println);
                    System.out.println("global fixed:");
                    this.fixed.forEach(System.out::println);
                    System.out.println("fixed:");
                    fixed.forEach(System.out::println);
                    System.out.println("New fixed: ");
                    newFixed.forEach(System.out::println);
                    System.out.println("Sentence: "+sentence);
                }
                assert(partialResult.isPresent());
                result1.addAll(partialResult.get());
                partialResult=Optional.of(result1);
            }
            assert(partialResult.isPresent());
            return partialResult;
        }
    }

    private boolean entails(Set<SENTENCE> set1, Set<SENTENCE> set2, SENTENCE sentence) {
        Set<SENTENCE> union = new HashSet<>();
        union.addAll(set1);
        union.addAll(set2);
        proofGenerator.setOntology(converter.convert(union));
        IProof<SENTENCE> proof = null;
        try {
            proof = proofGenerator.getProof(sentence);
        } catch (ProofGenerationException e) {
            return false;
        }

        if (proof.getInferences(sentence).isEmpty()) {
            return false;
        }
        return true;
    }

    private Pair<Set<SENTENCE>> split(Set<SENTENCE> sentences) {
        int half = sentences.size()/2;
        int count = 0;
        Set<SENTENCE> first = new HashSet<>();
        Set<SENTENCE> second = new HashSet<>();
        for(SENTENCE sentence: sentences){
            if(count<half){
                first.add(sentence);
            } else
                second.add(sentence);
            count++;
        }

        return new Pair(first,second);
    }

}
