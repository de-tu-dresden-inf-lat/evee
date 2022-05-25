package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalWeightedSize()),
                EveeElkBasedExtractorProofPreferencesManager.WEIGHTED_SIZE_MINIMAL,
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.WEIGHTED_SIZE_MINIMAL));
    }
}
