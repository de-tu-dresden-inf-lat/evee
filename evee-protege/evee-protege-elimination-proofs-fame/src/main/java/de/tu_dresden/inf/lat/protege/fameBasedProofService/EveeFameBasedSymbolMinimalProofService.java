package de.tu_dresden.inf.lat.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.FameBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSymbolMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.fameBasedProofService.EveeFameBasedSymbolMinimalProofService.class);

    public EveeFameBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSymbolMinimalProofGenerator()), "Elimination Proof, optimized for eliminated names (FAME)", "Elimination Proof, optimized for eliminated names (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedSymbolMinimalProofService", "Elimination Proof, optimized for eliminated names (FAME)_DoNotShowAgain");
    }


}