package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedSizeMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedEliminationProofPreferencesManager.SIZE_MINIMAL;

    public EveeLetheBasedSizeMinimalProofService(){
        super(new EveeLetheBasedSizeMinimalDynamicProofAdapter(
                new EveeLetheBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}
