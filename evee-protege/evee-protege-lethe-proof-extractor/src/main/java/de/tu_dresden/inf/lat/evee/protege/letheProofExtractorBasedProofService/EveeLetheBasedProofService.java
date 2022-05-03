package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    public EveeLetheBasedProofService(){
        super(new CachingProofGenerator<>(new MinimalTreeProofGenerator(new LetheProofGenerator())), "Detailed Proof", "Detailed Proof", "de.tu_dresden.inf.lat.EveeLetheBasedProofPreferences", "Detailed Proof_DoNotShowAgain");
    }

}
