package de.tu_dresden.inf.lat.evee.protege.counterexample.ELKRelevant;


import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.ELKRelevantCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collections;
import java.util.Set;


public class ELKCounterexampleGenerationServiceBeta extends AbstractCounterexampleGenerationService {

//    private final OWLDataFactory df;

    public ELKCounterexampleGenerationServiceBeta() {
        super();
        setCounterexampleGenerator(new ELKRelevantCounterexampleGenerator(ModelType.Beta));
        setSupportsExplanationMessage("Please enter an observation containing a single subclass axiom without complex class expressions");
    }

    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {

    }

    @Override
    public Set<OWLEntity> getAdditionalSignatureNames() {
        return Collections.emptySet();
    }
}




