package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

public class EveeLetheBasedExtractorProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    public EveeLetheBasedExtractorProofPreferencesUI(){
        super(new EveeLetheBasedExtractorProofPreferencesManager());
    }

}
