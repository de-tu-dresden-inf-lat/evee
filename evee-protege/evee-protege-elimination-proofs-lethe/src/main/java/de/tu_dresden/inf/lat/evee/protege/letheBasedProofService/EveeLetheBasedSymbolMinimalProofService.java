package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new EveeLetheBasedSymbolMinimalDynamicProofAdapter(
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID3),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeLetheBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME3)));
    }

}
