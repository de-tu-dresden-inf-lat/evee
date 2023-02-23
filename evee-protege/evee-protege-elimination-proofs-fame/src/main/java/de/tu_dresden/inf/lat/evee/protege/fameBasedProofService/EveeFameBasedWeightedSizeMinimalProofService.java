package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new EveeFameBasedEliminationDynamicProofAdapter(
                new CachingProofGenerator<>(new FameBasedWeightedSizeMinimalProofGenerator()),
                EveeFameBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL,
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL)));
    }

}