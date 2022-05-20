package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedWeightedSizeMinimalProofService.class);
    private final static String identifier = "Elimination Proof, optimized for weighted size (LETHE)";

    public EveeLetheBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedWeightedSizeMinimalProofGenerator()),
                identifier, new EveeLetheBasedEliminationProofPreferencesManager(identifier));
        this.proofAdapter.setDefaultIsActive(false);
    }

}
