package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.forgettingBasedProofs.LetheBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSymbolMinimalProofGenerator()), "Elimination Proof, optimized for eliminated names (LETHE)", "Elimination Proof, optimized for eliminated names (LETHE)", "de.tu_dresden.inf.lat.EveeLetheBasedSymbolMinimalProofService", "Elimination Proof, optimized for eliminated names (LETHE)_DoNotShowAgain");
    }

}
