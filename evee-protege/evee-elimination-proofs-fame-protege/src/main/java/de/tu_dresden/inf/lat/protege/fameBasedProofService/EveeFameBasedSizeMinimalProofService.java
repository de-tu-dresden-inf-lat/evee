package de.tu_dresden.inf.lat.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.fameBasedProofService.EveeFameBasedSizeMinimalProofService.class);

    public EveeFameBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSizeMinimalProofGenerator()), "Elimination Proof, optimized for size (FAME)", "Elimination Proof, optimized for size (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedSizeMinimalProofService", "Elimination Proof, optimized for size (FAME)_DoNotShowAgain");
    }


}