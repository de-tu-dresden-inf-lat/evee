package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.AbstractCounterexampleGenerationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;

public class ELCounterexampleGenerationServiceTreeModel extends AbstractCounterexampleGenerationService {

    public ELCounterexampleGenerationServiceTreeModel() {
        super();
        setCounterexampleGenerator(new ELCounterexampleGenerator(true));
        setSupportsExplanationMessage("Please enter some observation containing a single OWLSubClassOfAxiom expressed in EL");
    }

    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {

    }

}
