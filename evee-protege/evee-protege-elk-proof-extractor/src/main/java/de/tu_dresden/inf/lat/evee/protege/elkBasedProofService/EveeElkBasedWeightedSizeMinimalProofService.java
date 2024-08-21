package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new ESPGMinimalWeightedSize(),
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_ID3),
                new EveeDynamicProofLoadingUI(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_NAME3)));
    }
}
