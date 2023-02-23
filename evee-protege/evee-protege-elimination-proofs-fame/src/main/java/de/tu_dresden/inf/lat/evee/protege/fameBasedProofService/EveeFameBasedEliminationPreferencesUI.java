package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui.AbstractEveeEliminationProofPreferencesUI;

public class EveeFameBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    public EveeFameBasedEliminationPreferencesUI(){
        super(new EveeFameBasedEliminationProofPreferencesManager());
    }

}
