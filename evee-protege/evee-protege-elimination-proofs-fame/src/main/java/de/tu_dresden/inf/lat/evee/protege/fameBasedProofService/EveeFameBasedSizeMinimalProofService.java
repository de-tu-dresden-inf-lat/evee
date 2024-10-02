package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedSizeMinimalProofService(){
        super(new EveeFameBasedMinimalDynamicProofAdapter(
                new FameBasedSizeMinimalProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID2),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME2)));
    }

}