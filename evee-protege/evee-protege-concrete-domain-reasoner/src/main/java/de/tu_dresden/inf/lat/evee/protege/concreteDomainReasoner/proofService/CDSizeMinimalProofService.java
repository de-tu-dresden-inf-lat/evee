package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

public class CDSizeMinimalProofService extends CDProofService {

    public CDSizeMinimalProofService() {
        this(new OWLCDProofGenerator(null));
    }

    private CDSizeMinimalProofService(OWLCDProofGenerator generator) {
        super(generator, new MinimalTreeProofGenerator<>(generator, new TreeSizeMeasure<>()),
                CDProofPreferencesManager.SIZE_ID);
    }
}
