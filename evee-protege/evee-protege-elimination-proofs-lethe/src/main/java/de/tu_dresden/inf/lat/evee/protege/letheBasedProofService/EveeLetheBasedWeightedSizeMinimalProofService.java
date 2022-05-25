package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedWeightedSizeMinimalProofGenerator()),
                EveeLetheBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL,
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL
                ));
    }

}
