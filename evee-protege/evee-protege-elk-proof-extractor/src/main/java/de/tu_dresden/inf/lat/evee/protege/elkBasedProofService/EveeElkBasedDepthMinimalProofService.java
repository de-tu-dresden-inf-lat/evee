package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalDepth;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;


public class EveeElkBasedDepthMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeElkBasedExtractorProofPreferencesManager.DEPTH_MINIMAL;

    public EveeElkBasedDepthMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new ESPGMinimalDepth(),
                new EveeElkBasedExtractorProofPreferencesManager(identifier),
                new EveeDynamicProofLoadingUI(identifier)));
    }

}
