package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class ELCounterexampleGenerationServiceSmallModel extends AbstractCounterexampleGenerationService {
    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerationServiceSmallModel.class);

    public ELCounterexampleGenerationServiceSmallModel() {
        super();
        setCounterexampleGenerator(new ELCounterexampleGenerator(false));
        setSupportsExplanationMessage("Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL");
    }

    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {

    }

    @Override
    public Set<OWLEntity> getAdditionalSignatureNames() {
        return Collections.emptySet();
    }
}
