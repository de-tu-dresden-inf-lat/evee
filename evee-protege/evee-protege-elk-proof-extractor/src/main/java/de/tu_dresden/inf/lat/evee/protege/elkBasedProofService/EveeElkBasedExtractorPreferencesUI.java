package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferencesUI;

public class EveeElkBasedExtractorPreferencesUI extends EveeProofPreferencesUI {

    public EveeElkBasedExtractorPreferencesUI(){
        super(new EveeElkBasedExtractorProofPreferencesManager());
    }
}
