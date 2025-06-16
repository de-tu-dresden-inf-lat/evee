package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

public class EveeNemoBasedProofPreferencesUI extends AbstractEveeProofPreferencesUI{
    
    public EveeNemoBasedProofPreferencesUI(){
        super();
        this.setNemoBasedProofPreferencesManager(new EveeNemoBasedProofPreferencesManager());
    }

    public void setNemoBasedProofPreferencesManager(EveeNemoBasedProofPreferencesManager proofPreferencesManager){
        super.setAbstractProofPreferencesManager(proofPreferencesManager);
    }
}
