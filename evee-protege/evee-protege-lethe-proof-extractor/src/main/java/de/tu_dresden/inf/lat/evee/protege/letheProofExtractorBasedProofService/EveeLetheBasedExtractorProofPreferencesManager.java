package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferenceBoolean;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.EveeProofPreferenceInteger;

import java.util.HashMap;

public class EveeLetheBasedExtractorProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_LETHE_DETAILED";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_DETAILED";
    protected static final HashMap<String, EveeProofPreferenceBoolean> defaultBooleanPreferences = new HashMap<String, EveeProofPreferenceBoolean>();
    protected static final HashMap<String, EveeProofPreferenceInteger> defaultIntegerPreferences = new HashMap<String, EveeProofPreferenceInteger>();

    public EveeLetheBasedExtractorProofPreferencesManager(String baseKey) {
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

    public EveeLetheBasedExtractorProofPreferencesManager(){
        super(AbstractEveeProofPreferencesManager.GENERAL_NAME);
    }

}
