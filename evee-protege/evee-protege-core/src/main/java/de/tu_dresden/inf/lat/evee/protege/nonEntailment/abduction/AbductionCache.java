package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.*;

public class AbductionCache<Result> {

    private final HashMap<KeyTuple, Result> resultMap;

    public AbductionCache(){
        this.resultMap = new HashMap<>();
    }

    public boolean containsResultFor(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenSignature){
        KeyTuple newKey = new KeyTuple(givenObservation, givenSignature);
        return this.resultMap.containsKey(newKey);
    }

    public Result getResult(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenSignature){
        KeyTuple newKey = new KeyTuple(givenObservation, givenSignature);
        return this.resultMap.get(newKey);
    }

    public void putResult(Set<OWLAxiom> observation, Collection<OWLEntity> signature, Result result){
        KeyTuple newKey = new KeyTuple(observation, signature);
        this.resultMap.put(newKey, result);
    }


    private static class KeyTuple {

        private final Set<OWLAxiom> observation;
        private final Collection<OWLEntity> signature;

        public KeyTuple(Set<OWLAxiom> observation, Collection<OWLEntity> signature){
            this.observation = observation;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj){
                return true;
            }
            if (!(obj instanceof KeyTuple)){
                return false;
            }
            KeyTuple keyTuple = (KeyTuple) obj;
            return keyTuple.observation.equals(this.observation) &&
                    keyTuple.signature.equals(this.signature);
        }

        @Override
        public int hashCode(){
            return Objects.hash(this.signature, this.observation);
        }

    }

}
