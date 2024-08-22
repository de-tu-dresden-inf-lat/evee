package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedHeuristicProofService extends AbstractEveeProofService {

    public EveeFameBasedHeuristicProofService(){
        super(new EveeFameBasedForgettingDynamicProofAdapter(
                new FameBasedHeuristicProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID1),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME1)));
    }

}
