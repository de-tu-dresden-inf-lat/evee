package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSizeMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeFameBasedSizeMinimalProofService.class);

    public EveeFameBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSizeMinimalProofGenerator()), "Elimination Proof, optimized for size (FAME)", "Elimination Proof, optimized for size (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedSizeMinimalProofService", "Elimination Proof, optimized for size (FAME)_DoNotShowAgain");
    }


}