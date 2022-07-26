package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeElkBasedExtractorProofPreferencesManager.WEIGHTED_SIZE_MINIMAL;

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new OWLSignatureBasedMinimalTreeProofGenerator(new ESPGMinimalWeightedSize()),
                new EveeElkBasedExtractorProofPreferencesManager(identifier),
                new EveeDynamicProofLoadingUI(identifier)));
    }
}
