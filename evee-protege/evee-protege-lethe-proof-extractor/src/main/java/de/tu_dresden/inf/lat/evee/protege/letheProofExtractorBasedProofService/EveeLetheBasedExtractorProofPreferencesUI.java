package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofPreferencesUI;

public class EveeLetheBasedExtractorProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeLetheBasedExtractorProofPreferencesUI(){
        super(new EveeLetheBasedExtractorProofPreferencesManager());
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
