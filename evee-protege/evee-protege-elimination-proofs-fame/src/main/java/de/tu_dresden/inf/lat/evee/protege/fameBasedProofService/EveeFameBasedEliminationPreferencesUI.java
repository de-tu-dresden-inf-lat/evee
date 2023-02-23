package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferencesUI;

public class EveeFameBasedEliminationPreferencesUI extends EveeProofPreferencesUI {

    public EveeFameBasedEliminationPreferencesUI(){
        super(new EveeFameBasedEliminationProofPreferencesManager());
    }
}
