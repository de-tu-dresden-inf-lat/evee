package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AbductionCache<Result> {

    private final List<Set<OWLAxiom>> observationList;
    private final List<Collection<OWLEntity>> abducibleList;
    private final List<Result> results;

    public AbductionCache(){
        this.observationList = new ArrayList<>();
        this.abducibleList = new ArrayList<>();
        this.results = new ArrayList<>();
    }

    public boolean containsResultFor(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenAbducibles){
        for (int idx = 0; idx < this.observationList.size(); idx ++){
            if (this.observationList.get(idx).equals(givenObservation) &&
                    this.abducibleList.get(idx).equals(givenAbducibles)){
                return true;
            }
        }
        return false;
    }

    public Result getResult(Set<OWLAxiom> givenObservation, Collection<OWLEntity> givenAbducibles){
        Result result = null;
        for (int idx = 0; idx < this.observationList.size(); idx ++){
            if (this.observationList.get(idx).equals(givenObservation) &&
                    this.abducibleList.get(idx).equals(givenAbducibles)){
                result = this.results.get(idx);
            }
        }
        return result;
    }

    public void putResult(Set<OWLAxiom> observation, Collection<OWLEntity> abducibles, Result result){
        this.observationList.add(observation);
        this.abducibleList.add(abducibles);
        this.results.add(result);
    }

}
