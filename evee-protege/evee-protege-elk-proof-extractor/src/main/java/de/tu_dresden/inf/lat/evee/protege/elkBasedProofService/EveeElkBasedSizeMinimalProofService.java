package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeElkBasedSizeMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeElkBasedExtractorProofPreferencesManager.SIZE_MINIMAL;

    public EveeElkBasedSizeMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new ESPGMinimalSize(),
                new EveeElkBasedExtractorProofPreferencesManager(identifier),
                new EveeDynamicProofLoadingUI(identifier)));
    }

}
