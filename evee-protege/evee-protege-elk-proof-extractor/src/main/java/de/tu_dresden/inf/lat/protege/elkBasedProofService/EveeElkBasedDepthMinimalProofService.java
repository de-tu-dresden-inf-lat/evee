package de.tu_dresden.inf.lat.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.proofGenerators.specializedGenerators.ESPGMinimalDepth;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedDepthMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.elkBasedProofService.EveeElkBasedDepthMinimalProofService.class);

    public EveeElkBasedDepthMinimalProofService(){
        super(new CachingProofGenerator<>(new ESPGMinimalDepth()),
                "ELK Proof, optimized for depth", "ELK Proof, optimized for depth", "de.tu_dresden.inf.lat.EveeElkBasedDepthMinimalProofService", "ELK Proof, optimized for depth_DoNotShowAgain");
    }

}
