package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferenceBoolean;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferenceInteger;

import java.util.HashMap;

public class EveeLetheBasedEliminationProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_LETHE_ELIMINATION";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_ELIMINATION";
    protected static final HashMap<String, EveeProofPreferenceBoolean> defaultBooleanPreferences = new HashMap<String, EveeProofPreferenceBoolean>();
    protected static final HashMap<String, EveeProofPreferenceInteger> defaultIntegerPreferences = new HashMap<String, EveeProofPreferenceInteger>();

    public EveeLetheBasedEliminationProofPreferencesManager(String baseKey) {
        super(baseKey);
    }

    @Override
    public String getSetId() {
        return SET_ID;
    }

    @Override
    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    @Override
    public HashMap<String, EveeProofPreferenceBoolean> getDefaultBooleanPreferences() {
        return defaultBooleanPreferences;
    }

    @Override
    public HashMap<String, EveeProofPreferenceInteger> getDefaultIntegerPreferences() {
        return defaultIntegerPreferences;
    }

    public EveeLetheBasedEliminationProofPreferencesManager(){
        super(AbstractEveeProofPreferencesManager.GENERAL_NAME);
    }

}
