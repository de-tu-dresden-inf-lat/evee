package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

public class EveeLetheBasedExtractorProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_LETHE_DETAILED";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_DETAILED";
    protected static final String DETAILED = "Detailed Proof (Lethe)";

    public EveeLetheBasedExtractorProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeLetheBasedExtractorProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    protected void createIdentifierSet() {
        this.proofServiceNameSet.add(DETAILED);
    }

}
