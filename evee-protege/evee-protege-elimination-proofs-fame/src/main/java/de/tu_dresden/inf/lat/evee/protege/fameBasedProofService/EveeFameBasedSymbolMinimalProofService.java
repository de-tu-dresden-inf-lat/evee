package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedSymbolMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeFameBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL;

    public EveeFameBasedSymbolMinimalProofService(){
        super(new EveeFameBasedEliminationDynamicProofAdapter(
                new CachingProofGenerator<>(new FameBasedSymbolMinimalProofGenerator()),
                new EveeFameBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}