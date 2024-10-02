package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalDepth;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;


public class EveeElkBasedDepthMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedDepthMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new ESPGMinimalDepth(),
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_ID1),
                new EveeDynamicProofLoadingUI(
                        EveeElkBasedExtractorProofPreferencesManager.PROOF_SERVICE_NAME1)));
    }

}
