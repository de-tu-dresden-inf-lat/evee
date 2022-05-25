package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedSymbolMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSymbolMinimalProofService extends AbstractEveeProofService {

    public EveeFameBasedSymbolMinimalProofService(){
        super(new CachingProofGenerator<>(new FameBasedSymbolMinimalProofGenerator()),
                EveeFameBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL,
                new EveeFameBasedEliminationProofPreferencesManager(
                        EveeFameBasedEliminationProofPreferencesManager.SYMBOL_MINIMAL));
    }

}