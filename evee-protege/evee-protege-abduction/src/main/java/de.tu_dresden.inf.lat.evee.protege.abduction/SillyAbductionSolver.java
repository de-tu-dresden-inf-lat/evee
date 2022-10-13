package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SillyAbductionGenerator implements AbductionGenerator<Set<OWLObject>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>>{

    private final Set<OWLObject> observation;
    private final Set<OWLEntity> signature;
    private OWLOntology ontology;
    private final Set<OWLAxiom> hypothesis;

    public SillyAbductionGenerator(){
        this.observation = new HashSet<>();
        this.signature = new HashSet<>();
        this.hypothesis = new HashSet<>();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLSubClassOfAxiom sillyAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLNothing(), factory.getOWLThing());
        this.hypothesis.add(sillyAxiom);
        OWLSubClassOfAxiom verySillyAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLThing(), factory.getOWLNothing());
        this.hypothesis.add(verySillyAxiom);
    }

    @Override
    public void setObservations(Set<OWLObject> owlObjects) {
        this.observation.clear();
        this.observation.addAll(owlObjects);
    }

    @Override
    public void setSignature(Set<OWLEntity> owlEntities) {
        this.signature.clear();
        this.signature.addAll(owlEntities);
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    @Override
    public Set<Set<OWLAxiom>> generateAbductions() {
        HashSet<Set<OWLAxiom>> resultSet = new HashSet<>();
        this.hypothesis.forEach(hypo -> {
            HashSet<OWLAxiom> singleResult = new HashSet<>();
            singleResult.add(hypo);
            resultSet.add(singleResult);
        });
        return resultSet;
    }

}
