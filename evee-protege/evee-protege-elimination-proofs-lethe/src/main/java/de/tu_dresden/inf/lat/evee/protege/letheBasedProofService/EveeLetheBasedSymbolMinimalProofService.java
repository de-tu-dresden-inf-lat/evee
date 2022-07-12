package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedSymbolMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL;

    public EveeLetheBasedSymbolMinimalProofService(){
        super(new EveeLetheBasedSymbolMinimalDynamicProofAdapter(
                new LetheBasedSymbolMinimalProofGenerator(),
                new EveeLetheBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}
