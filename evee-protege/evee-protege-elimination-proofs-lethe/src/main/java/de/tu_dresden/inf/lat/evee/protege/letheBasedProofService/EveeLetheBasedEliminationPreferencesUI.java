package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeEliminationProofPreferencesUI;

public class EveeLetheBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    public EveeLetheBasedEliminationPreferencesUI(){
        super(new EveeLetheBasedEliminationProofPreferencesManager());
    }
}
