package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators.ESPGMinimalWeightedSize;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeElkBasedWeightedSizeMinimalProofService.class);

    public EveeElkBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalWeightedSize()),
                "ELK Proof, optimized for weighted size", "ELK Proof, optimized for weighted size", "de.tu_dresden.inf.lat.EveeElkBasedWeightedSizeMinimalProofService", "ELK Proof, optimized for weighted size_DoNotShowAgain");
    }
}
