package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    private final static String identifier = EveeFameBasedEliminationProofPreferencesManager.SIZE_MINIMAL;

    public EveeFameBasedSizeMinimalProofService(){
        super(new EveeFameBasedMinimalDynamicProofAdapter(
                new FameBasedSizeMinimalProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}