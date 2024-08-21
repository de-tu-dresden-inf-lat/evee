package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalSize;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeElkBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedSizeMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new ESPGMinimalSize(),
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_ID2),
                new EveeDynamicProofLoadingUI(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_NAME2)));
    }

}
