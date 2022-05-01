package de.tu_dresden.inf.lat.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.LetheBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedHeuristicProofService extends AbstractEveeProofService {

    public EveeLetheBasedHeuristicProofService(){
        super(new CachingProofGenerator<>(new LetheBasedHeuristicProofGenerator()), "Elimination Proof (LETHE)", "Elimination Proof (LETHE)", "de.tu_dresden.inf.lat.EveeLetheBasedHeuristicProofService", "Elimination Proof (LETHE)_DoNotShowAgain");
    }

}
