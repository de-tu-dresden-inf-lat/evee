package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.DepthMeasure;

public class CDDepthMinimalProofService extends CDProofService {

    public CDDepthMinimalProofService() {
        this(new OWLCDProofGenerator(null));
    }

    private CDDepthMinimalProofService(OWLCDProofGenerator generator) {
        super(generator, new MinimalTreeProofGenerator<>(generator, new DepthMeasure<>()),
                CDProofPreferencesManager.DEPTH_ID);
    }
}
