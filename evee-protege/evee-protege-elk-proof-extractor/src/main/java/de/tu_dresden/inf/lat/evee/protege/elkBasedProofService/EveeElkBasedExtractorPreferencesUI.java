package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofPreferencesUI;

public class EveeElkBasedExtractorPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeElkBasedExtractorPreferencesUI(){
        super(new EveeElkBasedExtractorProofPreferencesManager());
    }

    @Override
    protected void addAdditionalPreferences() {
//        no operation needed
    }

    @Override
    protected void saveAdditionalPreferences() {
//        no operation needed
    }
}
