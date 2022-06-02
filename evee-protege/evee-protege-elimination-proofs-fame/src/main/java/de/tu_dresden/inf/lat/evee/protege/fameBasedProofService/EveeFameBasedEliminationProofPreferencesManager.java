package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;

public class EveeFameBasedEliminationProofPreferencesManager extends AbstractEveeEliminationProofPreferencesManager {

    protected static final String SET_ID = "EVEE_PROOF_SERVICE_FAME_ELIMINATION";
    protected static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_FAME_ELIMINATION";
    protected static final String HEURISTIC = "Elimination Proof (FAME)";
    protected static final String SIZE_MINIMAL = "Elimination Proof, optimized for size (FAME)";
    protected static final String SYMBOL_MINIMAL = "Elimination Proof, optimized for eliminated names (FAME)";
    protected static final String WEIGHTED_SIZE_MINIMAL = "Elimination Proof, optimized for weighted size (FAME)";

    public EveeFameBasedEliminationProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
        this.correctActivationDefaultPreferences();
    }

    public EveeFameBasedEliminationProofPreferencesManager() {
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

    protected void correctActivationDefaultPreferences() {
        this.activationDefaultPreferences.get(SIZE_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(SYMBOL_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(WEIGHTED_SIZE_MINIMAL).setBooleanDefaultValue(false);
    }

}
