package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSizeMinimalProofGenerator()),
                EveeFameBasedEliminationProofPreferencesManager.SIZE_MINIMAL,
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.SIZE_MINIMAL));
    }

}