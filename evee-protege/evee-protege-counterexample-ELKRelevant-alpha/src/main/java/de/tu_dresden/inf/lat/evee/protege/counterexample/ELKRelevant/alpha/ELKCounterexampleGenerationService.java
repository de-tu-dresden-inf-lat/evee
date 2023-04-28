package de.tu_dresden.inf.lat.evee.protege.counterexample.ELKRelevant.alpha;


import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.ELKRelevantCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import org.semanticweb.owlapi.apibinding.OWLManager;

import java.util.HashSet;


public class ELKCounterexampleGenerationService extends AbstractCounterexampleGenerationService {

//    private final OWLDataFactory df;

    public ELKCounterexampleGenerationService() {

        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.man = OWLManager.createOWLOntologyManager();
        this.counterexampleGenerator = new ELKRelevantCounterexampleGenerator(ModelType.Alpha);
//        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//        this.counterexampleGenerator = new ELCounterexampleGenerator();

    }

}




