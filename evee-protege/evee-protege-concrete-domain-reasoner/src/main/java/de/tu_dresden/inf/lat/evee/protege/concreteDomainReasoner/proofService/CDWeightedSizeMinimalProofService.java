package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.ConstraintSizeWeightedTreeSizeMeasure;
import de.tu_dresden.inf.lat.evee.concreteDomains.owlApi.OWLCDProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;

/** Constraints count with their sizes. */
public class CDWeightedSizeMinimalProofService extends CDProofService {

    public CDWeightedSizeMinimalProofService() {
        this(new OWLCDProofGenerator(null));
    }

    private CDWeightedSizeMinimalProofService(OWLCDProofGenerator generator) {
        super(generator, new MinimalTreeProofGenerator<>(generator,
                new ConstraintSizeWeightedTreeSizeMeasure(generator.constraints())),
                CDProofPreferencesManager.WEIGHTED_SIZE_ID);
    }
}
