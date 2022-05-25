package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeEliminationProofPreferencesUI;

public class EveeFameBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    public EveeFameBasedEliminationPreferencesUI(){
        super(new EveeFameBasedEliminationProofPreferencesManager());
    }
}
