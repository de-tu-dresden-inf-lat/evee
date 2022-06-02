package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalDepth;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;


public class EveeElkBasedDepthMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedDepthMinimalProofService(){
        super(new EveeElkBasedDynamicProofAdapter(
                new CachingProofGenerator<>(new ESPGMinimalDepth()),
                EveeElkBasedExtractorProofPreferencesManager.DEPTH_MINIMAL,
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.DEPTH_MINIMAL)));
    }

}
