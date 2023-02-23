package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

public class EveeElkBasedExtractorPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeElkBasedExtractorPreferencesUI(){
        super(new EveeElkBasedExtractorProofPreferencesManager());
    }

}
