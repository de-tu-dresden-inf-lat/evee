package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class ELCounterexampleGenerationService extends AbstractCounterexampleGenerationService {
    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerationService.class);

    public ELCounterexampleGenerationService() {
        super();
        setCounterexampleGenerator(new ELCounterexampleGenerator());
        setSupportsExplanationMessage("Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL");
    }
}
