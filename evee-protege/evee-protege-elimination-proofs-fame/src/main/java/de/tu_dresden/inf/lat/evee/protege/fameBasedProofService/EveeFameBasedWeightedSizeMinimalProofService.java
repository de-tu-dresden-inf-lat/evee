package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeFameBasedWeightedSizeMinimalProofService.class);

    public EveeFameBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedWeightedSizeMinimalProofGenerator()), "Elimination Proof, optimized for weighted size (FAME)", "Elimination Proof, optimized for weighted size (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedWeightedSizeMinimalProofService", "Elimination Proof, optimized for weighted size (FAME)_DoNotShowAgain");
    }

}