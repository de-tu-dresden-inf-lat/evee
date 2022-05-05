package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSizeMinimalProofGenerator()), "Elimination Proof, optimized for size (LETHE)", "Elimination Proof, optimized for size (LETHE)", "de.tu_dresden.inf.lat.EveeLetheBasedSizeMinimalProofService", "Elimination Proof, optimized for size (LETHE)_DoNotShowAgain");
    }

}
