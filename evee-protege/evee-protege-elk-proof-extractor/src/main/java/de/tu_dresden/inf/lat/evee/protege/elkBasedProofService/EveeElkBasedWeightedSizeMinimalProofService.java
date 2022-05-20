package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeElkBasedWeightedSizeMinimalProofService.class);
    private static final String identifier = "ELK Proof, optimized for weighted size";

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalWeightedSize()),
                identifier, new EveeElkBasedExtractorProofPreferencesManager(identifier));
    }
}
