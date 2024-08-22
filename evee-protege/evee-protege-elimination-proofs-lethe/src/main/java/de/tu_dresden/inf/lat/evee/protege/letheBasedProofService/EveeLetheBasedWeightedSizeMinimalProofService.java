package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedWeightedSizeMinimalProofService(){
        super(new EveeLetheBasedWeightedSizeMinimalDynamicProofAdapter(
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID4),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME4)));
    }

}
