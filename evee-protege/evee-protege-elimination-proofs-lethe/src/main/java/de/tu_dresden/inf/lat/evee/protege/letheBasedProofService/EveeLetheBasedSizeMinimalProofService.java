package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSizeMinimalProofGenerator()),
                EveeLetheBasedEliminationProofPreferencesManager.SIZE_MINIMAL,
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.SIZE_MINIMAL));
    }

}
