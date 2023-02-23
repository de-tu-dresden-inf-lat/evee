package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new EveeLetheBasedEliminationDynamicProofAdapter(
                new CachingProofGenerator<>(new LetheBasedSymbolMinimalProofGenerator()),
                EveeLetheBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL,
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL)));
    }

}
