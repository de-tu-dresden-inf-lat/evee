package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELCounterexampleGenerationServiceSmallModel extends AbstractCounterexampleGenerationService {
    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerationServiceSmallModel.class);

    public ELCounterexampleGenerationServiceSmallModel() {
        super();
        setCounterexampleGenerator(new ELCounterexampleGenerator(false));
        setSupportsExplanationMessage("Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL");
    }
}
