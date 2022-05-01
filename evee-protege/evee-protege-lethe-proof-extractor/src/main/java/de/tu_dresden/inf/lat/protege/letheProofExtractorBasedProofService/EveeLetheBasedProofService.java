package de.tu_dresden.inf.lat.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    public EveeLetheBasedProofService(){
        super(new CachingProofGenerator<>(new MinimalTreeProofGenerator(new LetheProofGenerator())), "Detailed Proof", "Detailed Proof", "de.tu_dresden.inf.lat.EveeLetheBasedProofPreferences", "Detailed Proof_DoNotShowAgain");
    }

}
