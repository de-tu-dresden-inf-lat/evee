package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedSizeMinimalProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeElkBasedSizeMinimalProofService.class);
    private static final String identifier = "ELK Proof, optimized for size";

    public EveeElkBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalSize()),
                identifier, new EveeElkBasedExtractorProofPreferencesManager(identifier));
    }

}
