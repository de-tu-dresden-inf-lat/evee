package de.tu_dresden.inf.lat.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.fameBasedProofService.EveeFameBasedWeightedSizeMinimalProofService.class);

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedWeightedSizeMinimalProofGenerator()), "Elimination Proof, optimized for weighted size (FAME)", "Elimination Proof, optimized for weighted size (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedWeightedSizeMinimalProofService", "Elimination Proof, optimized for weighted size (FAME)_DoNotShowAgain");
    }

}