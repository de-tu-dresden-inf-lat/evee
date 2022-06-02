package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedSizeMinimalProofService(){
        super(new EveeFameBasedEliminationDynamicProofAdapter(
                new CachingProofGenerator<>(new FameBasedSizeMinimalProofGenerator()),
                EveeFameBasedEliminationProofPreferencesManager.SIZE_MINIMAL,
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.SIZE_MINIMAL)));
    }

}