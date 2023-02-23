package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferencesUI;

public class EveeLetheBasedEliminationPreferencesUI extends EveeProofPreferencesUI {

    public EveeLetheBasedEliminationPreferencesUI(){
        super(new EveeLetheBasedEliminationProofPreferencesManager());
    }
}
