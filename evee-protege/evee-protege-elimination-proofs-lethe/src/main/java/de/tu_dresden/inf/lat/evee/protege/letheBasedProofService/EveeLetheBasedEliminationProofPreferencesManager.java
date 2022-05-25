package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeEliminationProofPreferencesManager;

public class EveeLetheBasedEliminationProofPreferencesManager extends AbstractEveeEliminationProofPreferencesManager {

    protected static final String SET_ID = "EVEE_PROOF_SERVICE_LETHE_ELIMINATION";
    protected static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_ELIMINATION";
    protected static final String HEURISTIC = "Elimination Proof (LETHE)";
    protected static final String SIZE_MINIMAL = "Elimination Proof, optimized for size (LETHE)";
    protected static final String SYMBOL_MINIMAL = "Elimination Proof, optimized for eliminated names (LETHE)";
    protected static final String WEIGHTED_SIZE_MINIMAL = "Elimination Proof, optimized for weighted size (LETHE)";

    public EveeLetheBasedEliminationProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
        this.correctActivationDefaultPreferences();
    }

    public EveeLetheBasedEliminationProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeEliminationProofPreferencesManager.PREFERENCE_UI);
        this.correctActivationDefaultPreferences();
    }

    @Override
    protected void createIdentifierSet() {
        this.proofServiceNameSet.add(HEURISTIC);
        this.proofServiceNameSet.add(SIZE_MINIMAL);
        this.proofServiceNameSet.add(SYMBOL_MINIMAL);
        this.proofServiceNameSet.add(WEIGHTED_SIZE_MINIMAL);
    }

    protected void correctActivationDefaultPreferences(){
        this.activationDefaultPreferences.get(SIZE_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(SYMBOL_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(WEIGHTED_SIZE_MINIMAL).setBooleanDefaultValue(false);
    }

}
