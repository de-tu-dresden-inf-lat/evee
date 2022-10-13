package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AbductionGenerationThread extends Thread {

    private final AbductionViewComponent viewComponent;
    private AbductionSolver<Set<OWLObject>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> abductionSolver;

    private final Logger logger = LoggerFactory.getLogger(AbductionGenerationThread.class);

    public AbductionGenerationThread(AbductionViewComponent viewComponent){
        super.setName("Evee Abduction Generation Thread");
        this.viewComponent = viewComponent;
    }

    public void setAbductionGenerator(AbductionSolver<Set<OWLObject>, Set<OWLEntity>, OWLOntology,  Set<Set<OWLAxiom>>> abductionSolver) {
        this.abductionSolver = abductionSolver;
    }

    public void run(){
        this.logger.debug("Abduction generation thread started");
        assert(abductionSolver != null);
        Set<Set<OWLAxiom>> abductions = this.abductionSolver.generateHypotheses();
        this.viewComponent.abductionGenerationCompleted();
        this.viewComponent.showResults(abductions);
        this.logger.debug("Abduction generation thread run method completed");
    }

}
