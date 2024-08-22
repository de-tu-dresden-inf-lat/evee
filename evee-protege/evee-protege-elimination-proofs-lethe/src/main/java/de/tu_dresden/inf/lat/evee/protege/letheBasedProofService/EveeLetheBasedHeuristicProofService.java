package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedHeuristicProofService extends AbstractEveeProofService {

    public EveeLetheBasedHeuristicProofService(){
        super(new EveeLetheBasedForgettingDynamicProofAdapter(
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID1),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME1)));
    }

}
