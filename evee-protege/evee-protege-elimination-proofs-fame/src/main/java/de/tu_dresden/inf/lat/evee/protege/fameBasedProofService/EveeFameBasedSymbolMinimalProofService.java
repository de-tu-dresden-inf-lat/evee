package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSymbolMinimalProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeFameBasedSymbolMinimalProofService.class);

    public EveeFameBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSymbolMinimalProofGenerator()), "Elimination Proof, optimized for eliminated names (FAME)", "Elimination Proof, optimized for eliminated names (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedSymbolMinimalProofService", "Elimination Proof, optimized for eliminated names (FAME)_DoNotShowAgain");
    }


}