package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELCounterexampleGenerationServiceTreeModel extends AbstractCounterexampleGenerationService {
    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerationServiceTreeModel.class);

    public ELCounterexampleGenerationServiceTreeModel() {
        super();
        setCounterexampleGenerator(new ELCounterexampleGenerator(true));
        setSupportsExplanationMessage("Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL");
    }

}
