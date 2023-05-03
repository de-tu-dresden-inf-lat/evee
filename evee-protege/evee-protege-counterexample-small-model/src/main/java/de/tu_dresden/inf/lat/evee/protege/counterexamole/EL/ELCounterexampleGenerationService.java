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

        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.man = OWLManager.createOWLOntologyManager();
//        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.counterexampleGenerator = new ELCounterexampleGenerator();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL";
    }
}
