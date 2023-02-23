package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferencesUI;

public class EveeLetheBasedExtractorProofPreferencesUI extends EveeProofPreferencesUI {

    public EveeLetheBasedExtractorProofPreferencesUI(){
        super(new EveeLetheBasedExtractorProofPreferencesManager());
    }

}
