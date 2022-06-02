package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

public class EveeElkBasedExtractorProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_ELK";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_ELK";
    protected static final String DEPTH_MINIMAL = "ELK Proof, optimized for depth";
    protected static final String SIZE_MINIMAL = "ELK Proof, optimized for size";
    protected static final String WEIGHTED_SIZE_MINIMAL = "ELK Proof, optimized for weighted size";

    public EveeElkBasedExtractorProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeElkBasedExtractorProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    protected void createIdentifierSet() {
        this.proofServiceNameSet.add(DEPTH_MINIMAL);
        this.proofServiceNameSet.add(SIZE_MINIMAL);
        this.proofServiceNameSet.add(WEIGHTED_SIZE_MINIMAL);
    }

}
