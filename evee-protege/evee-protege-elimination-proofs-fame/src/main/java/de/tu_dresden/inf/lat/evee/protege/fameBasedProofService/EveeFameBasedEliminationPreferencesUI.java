package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeEliminationProofPreferencesUI;

public class EveeFameBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    public EveeFameBasedEliminationPreferencesUI(){
        super();
        this.setFameBasedEliminationProofPreferencesManager(new EveeFameBasedEliminationProofPreferencesManager());
    }

    public void setFameBasedEliminationProofPreferencesManager(EveeFameBasedEliminationProofPreferencesManager proofPreferencesManager){
        super.setAbstractEliminationProofPreferencesManager(proofPreferencesManager);
    }

}
