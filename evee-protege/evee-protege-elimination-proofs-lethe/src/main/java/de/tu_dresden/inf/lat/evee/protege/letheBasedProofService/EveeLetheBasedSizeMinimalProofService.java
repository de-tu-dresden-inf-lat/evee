package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSizeMinimalProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSizeMinimalProofService.class);
    private static final String identifier = "Elimination Proof, optimized for size (LETHE)";

    public EveeLetheBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSizeMinimalProofGenerator()),
                identifier, new EveeLetheBasedEliminationProofPreferencesManager(identifier));
        this.proofAdapter.setDefaultIsActive(false);
    }

}
