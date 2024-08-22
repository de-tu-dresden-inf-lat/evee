package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedSymbolMinimalProofService(){
        super(new EveeFameBasedSymbolMinimalDynamicProofAdapter(
                new FameBasedSymbolMinimalProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID3),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME3)));
    }

}