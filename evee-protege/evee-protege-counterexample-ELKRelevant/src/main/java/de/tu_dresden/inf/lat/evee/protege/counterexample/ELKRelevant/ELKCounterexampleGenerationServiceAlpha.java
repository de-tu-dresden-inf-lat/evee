package de.tu_dresden.inf.lat.evee.protege.counterexample.ELKRelevant;


import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.ELKRelevantCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.semanticweb.owlapi.apibinding.OWLManager;

import java.util.HashSet;


public class ELKCounterexampleGenerationServiceAlpha extends AbstractCounterexampleGenerationService {

//    private final OWLDataFactory df;

    public ELKCounterexampleGenerationServiceAlpha() {
        super();
        setCounterexampleGenerator(new ELKRelevantCounterexampleGenerator(ModelType.Alpha));
    }

}




