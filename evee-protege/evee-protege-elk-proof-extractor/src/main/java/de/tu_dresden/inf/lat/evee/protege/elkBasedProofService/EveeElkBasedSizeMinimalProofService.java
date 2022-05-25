package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeElkBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalSize()),
                EveeElkBasedExtractorProofPreferencesManager.SIZE_MINIMAL,
                new EveeElkBasedExtractorProofPreferencesManager(
                        EveeElkBasedExtractorProofPreferencesManager.SIZE_MINIMAL));
    }

}
