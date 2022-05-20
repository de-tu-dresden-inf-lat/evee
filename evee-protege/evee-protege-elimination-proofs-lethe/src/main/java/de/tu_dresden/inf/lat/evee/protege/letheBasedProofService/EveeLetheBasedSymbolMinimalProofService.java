package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSymbolMinimalProofService.class);
    private static final String identifier = "Elimination Proof, optimized for eliminated names (LETHE)";

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new LetheBasedSymbolMinimalProofGenerator()),
                identifier, new EveeLetheBasedEliminationProofPreferencesManager(identifier));
        this.proofAdapter.setDefaultIsActive(false);
    }

}
