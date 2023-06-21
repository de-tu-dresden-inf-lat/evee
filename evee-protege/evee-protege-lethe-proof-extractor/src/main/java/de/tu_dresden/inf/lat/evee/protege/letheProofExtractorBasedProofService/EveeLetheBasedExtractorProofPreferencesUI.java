package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

public class EveeLetheBasedExtractorProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeLetheBasedExtractorProofPreferencesUI(){
        super();
        this.setLetheBasedExtractorProofPreferencesManager(new EveeLetheBasedExtractorProofPreferencesManager());
    }

    public void setLetheBasedExtractorProofPreferencesManager(EveeLetheBasedExtractorProofPreferencesManager proofPreferencesManager){
        super.setAbstractProofPreferencesManager(proofPreferencesManager);
    }

}
