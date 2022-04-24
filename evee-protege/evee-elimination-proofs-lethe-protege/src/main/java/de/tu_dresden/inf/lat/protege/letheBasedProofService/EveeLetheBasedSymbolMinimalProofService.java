package de.tu_dresden.inf.lat.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.LetheBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSymbolMinimalProofGenerator()), "Elimination Proof, optimized for eliminated names (LETHE)", "Elimination Proof, optimized for eliminated names (LETHE)", "de.tu_dresden.inf.lat.EveeLetheBasedSymbolMinimalProofService", "Elimination Proof, optimized for eliminated names (LETHE)_DoNotShowAgain");
    }

}
