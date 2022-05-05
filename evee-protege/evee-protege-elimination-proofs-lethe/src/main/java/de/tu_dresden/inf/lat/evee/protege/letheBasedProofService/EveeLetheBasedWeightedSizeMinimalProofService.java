package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedWeightedSizeMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedWeightedSizeMinimalProofGenerator()), "Elimination Proof, optimized for weighted size (LETHE)", "Elimination Proof, optimized for weighted size (LETHE)", "de.tu_dresden.inf.lat.EveeLetheBasedWeightedSizeMinimalProofService", "Elimination Proof, optimized for weighted size (LETHE)_DoNotShowAgain");
    }

}
