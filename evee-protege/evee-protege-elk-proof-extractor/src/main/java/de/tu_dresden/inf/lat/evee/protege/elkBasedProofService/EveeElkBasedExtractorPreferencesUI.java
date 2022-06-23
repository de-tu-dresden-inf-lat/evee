package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

public class EveeElkBasedExtractorPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeElkBasedExtractorPreferencesUI(){
        super();
        this.setElkBasedExtractorProofPreferencesManager(new EveeElkBasedExtractorProofPreferencesManager());
    }

    public void setElkBasedExtractorProofPreferencesManager(EveeElkBasedExtractorProofPreferencesManager proofPreferencesManager){
        super.setAbstractProofPreferencesManager(proofPreferencesManager);
    }

}
