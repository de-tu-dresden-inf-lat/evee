package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeFameBasedWeightedSizeMinimalProofService.class);
    private final static String identifier = "Elimination Proof, optimized for weighted size (FAME)";

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedWeightedSizeMinimalProofGenerator()),
                identifier, new EveeFameBasedEliminationProofPreferencesManager(identifier));
        this.proofAdapter.setDefaultIsActive(false);
    }

}