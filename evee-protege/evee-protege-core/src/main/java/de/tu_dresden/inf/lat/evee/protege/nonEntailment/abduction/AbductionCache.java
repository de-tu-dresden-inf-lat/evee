package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.*;

public class AbductionCache<Result> {

    private final HashMap<KeyTuple, Result> resultMap;

    public AbductionCache(){
        this.resultMap = new HashMap<>();
    }

    public boolean containsResultFor(Set<OWLAxiom> givenMissingEntailment, Collection<OWLEntity> givenSignature){
        KeyTuple newKey = new KeyTuple(givenMissingEntailment, givenSignature);
        return this.resultMap.containsKey(newKey);
    }

    public Result getResult(Set<OWLAxiom> givenMissingEntailment, Collection<OWLEntity> givenSignature){
        KeyTuple newKey = new KeyTuple(givenMissingEntailment, givenSignature);
        return this.resultMap.get(newKey);
    }

    public void putResult(Set<OWLAxiom> missingEntailment, Collection<OWLEntity> signature, Result result){
        KeyTuple newKey = new KeyTuple(missingEntailment, signature);
        this.resultMap.put(newKey, result);
    }


    private static class KeyTuple {

        private final Set<OWLAxiom> missingEntailment;
        private final Collection<OWLEntity> signature;

        public KeyTuple(Set<OWLAxiom> missingEntailment, Collection<OWLEntity> signature){
            this.missingEntailment = missingEntailment;
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
            return keyTuple.missingEntailment.equals(this.missingEntailment) &&
                    keyTuple.signature.equals(this.signature);
        }

        @Override
        public int hashCode(){
            return Objects.hash(this.signature, this.missingEntailment);
        }

    }

}
