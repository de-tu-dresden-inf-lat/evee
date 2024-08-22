package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new EveeFameBasedMinimalDynamicProofAdapter(
                new FameBasedWeightedSizeMinimalProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_ID4),
                new EveeDynamicSuboptimalProofLoadingUI(
                        EveeFameBasedEliminationProofPreferencesManager.PROOF_SERVICE_NAME4)));
    }

}