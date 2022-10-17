package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SlowAbductionSolver implements AbductionGenerator<Set<OWLObject>, OWLEntity, OWLOntology, Set<Set<OWLAxiom>>> {

    private final Set<OWLObject> observation;
    private final Set<OWLEntity> signature;
    private OWLOntology ontology;
    private final Set<OWLAxiom> hypothesis;

    private final Logger logger = LoggerFactory.getLogger(SlowAbductionSolver.class);

    public SlowAbductionSolver(){
        this.observation = new HashSet<>();
        this.signature = new HashSet<>();
        this.hypothesis = new HashSet<>();
    }

    @Override
    public void setObservation(Set<OWLObject> owlObjects) {
        this.observation.clear();
        this.observation.addAll(owlObjects);
    }

    @Override
    public void setAbducibles(Collection<OWLEntity> owlEntities) {
        this.signature.clear();
        this.signature.addAll(owlEntities);
    }

    @Override
    public void setOntology(OWLOntology ontology) {

    }

    @Override
    public Set<Set<OWLAxiom>> generateHypotheses() {
        HashSet<Set<OWLAxiom>> result = new HashSet<>();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        int idx = 0;
        while (idx <= 4){
            OWLClass aClass = factory.getOWLClass(IRI.create("A" + idx));
            OWLClass bCLass = factory.getOWLClass(IRI.create("B" + idx));
            OWLSubClassOfAxiom subCLassAxiom = factory.getOWLSubClassOfAxiom(aClass, bCLass);
            HashSet<OWLAxiom> singleResult = new HashSet<>();
            singleResult.add(subCLassAxiom);
            result.add(singleResult);
            idx += 1;
            try{
                Thread.sleep(2000);
            }
            catch (InterruptedException e){
                this.logger.debug(e.toString());
            }
        }
        return result;
    }
}
