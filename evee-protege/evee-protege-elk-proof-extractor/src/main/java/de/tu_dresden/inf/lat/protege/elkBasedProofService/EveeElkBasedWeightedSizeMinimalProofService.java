package de.tu_dresden.inf.lat.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.elkBasedProofService.EveeElkBasedWeightedSizeMinimalProofService.class);

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalWeightedSize()),
                "ELK Proof, optimized for weighted size", "ELK Proof, optimized for weighted size", "de.tu_dresden.inf.lat.EveeElkBasedWeightedSizeMinimalProofService", "ELK Proof, optimized for weighted size_DoNotShowAgain");
    }
}
