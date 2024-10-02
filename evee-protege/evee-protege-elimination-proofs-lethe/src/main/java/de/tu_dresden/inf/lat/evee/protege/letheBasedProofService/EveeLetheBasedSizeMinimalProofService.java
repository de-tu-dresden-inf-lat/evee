package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSizeMinimalProofService(){
        super(new EveeLetheBasedSizeMinimalDynamicProofAdapter(
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID2),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME2)));
    }

}
