package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeFameBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL;

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new EveeFameBasedMinimalDynamicProofAdapter(
                new FameBasedWeightedSizeMinimalProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}