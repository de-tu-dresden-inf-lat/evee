package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

public class EveeNemoBasedProofPreferencesManager extends AbstractEveeProofPreferencesManager{

    private static final String SET_ID = "EVEE_PROOF_NEMO";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_NEMO";

    protected static final String ELK =  "NEMO Proof, based on ELK";
    protected static final String TEXTBOOK =  "NEMO Proof, based on TEXTBOOK";
    protected static final String ENVELOPE =  "NEMO Proof, based on ENVELOPE";



    public EveeNemoBasedProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeNemoBasedProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    protected void createIdentifierSet() {
        this.proofServiceNameSet.add(ELK);
        this.proofServiceNameSet.add(TEXTBOOK);
        this.proofServiceNameSet.add(ENVELOPE);
    }

}

