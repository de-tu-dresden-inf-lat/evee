package de.tu_dresden.inf.lat.evee.protege.counterexamole.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class ELCounterexampleGenerationService extends AbstractCounterexampleGenerationService {
    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerationService.class);
//    private final OWLDataFactory df;

    public ELCounterexampleGenerationService() {
        this.logger.debug("Creating de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELCounterExampleGenerator");
        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.man = OWLManager.createOWLOntologyManager();
//        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.counterexampleGenerator = new ELCounterexampleGenerator();
        this.logger.debug("de.tu_dresden.inf.lat.evee.protege.modelgeneration.ELCounterExampleGenerator created successfully.");
    }
}
