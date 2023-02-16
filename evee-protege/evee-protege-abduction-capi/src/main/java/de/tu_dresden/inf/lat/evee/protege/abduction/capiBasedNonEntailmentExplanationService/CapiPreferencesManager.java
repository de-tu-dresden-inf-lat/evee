package de.tu_dresden.inf.lat.evee.protege.abduction.capiBasedNonEntailmentExplanationService;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class CapiPreferencesManager {

    private static final String SET_ID = "EVEE_NON_ENTAILMENT_EXPLANATION_SERVICE";
    private static final String PREFERENCE_ID = "EVEE_ABDUCTION_PREFERENCES_MANAGER";
    private static final String CAPI_PATH = "CAPI_PATH";
    private static final String TIME_LIMIT = "TIME_LIMIT";
    private static final String REMOVE_REDUNDANCIES = "REMOVE_REDUNDANCIES";
    private static final String SIMPLIFY_CONJUNCTIONS = "SIMPLIFY_CONJUNCTIONS";
    private static final String SEMANTICALLY_ORDERED = "SEMANTICALLY_ORDERED";

    private final Logger logger = LoggerFactory.getLogger(CapiPreferencesManager.class);

    public CapiPreferencesManager(){
    }

    private Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
    }

    public String loadSpassPath() {
        Preferences preferences = this.getProtegePreferences();
        String path = preferences.getString(CAPI_PATH, "");
        this.logger.debug("Loaded SPASS path: " + path);
        return path;
    }

    public void saveSpassPath(String newPath){
        Preferences preferences = this.getProtegePreferences();
        preferences.putString(CAPI_PATH, newPath);
        this.logger.debug("Saved SPASS path: " + newPath);
    }

    public int loadTimeLimit(){
        Preferences preferences = this.getProtegePreferences();
        int timeLimit = preferences.getInt(TIME_LIMIT, 10);
        this.logger.debug("Loaded time limit: " + timeLimit);
        return timeLimit;
    }

    public void saveTimeLimit(int timeLimit){
        Preferences preferences = this.getProtegePreferences();
        preferences.putInt(TIME_LIMIT, timeLimit);
        this.logger.debug("Saved time limit: " + timeLimit);
    }

    public boolean loadRemoveRedundancies(){
        Preferences preferences = this.getProtegePreferences();
        boolean value = preferences.getBoolean(REMOVE_REDUNDANCIES, false);
        this.logger.debug("Loaded remove redundancies: " + value);
        return value;
    }

    public void saveRemoveRedundancies(boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(REMOVE_REDUNDANCIES, value);
        this.logger.debug("Saved remove redundancies: " + value);
    }

    public boolean loadSimplifyConjunctions(){
        Preferences preferences = this.getProtegePreferences();
        boolean value = preferences.getBoolean(SIMPLIFY_CONJUNCTIONS, false);
        this.logger.debug("Loaded simplify conjunctions: " + value);
        return value;
    }

    public void saveSimplifyConjunctions(boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SIMPLIFY_CONJUNCTIONS, value);
        this.logger.debug("Saved simplify conjunctions: " + value);
    }

    public boolean loadSemanticallyOrdered(){
        Preferences preferences = this.getProtegePreferences();
        boolean value = preferences.getBoolean(SEMANTICALLY_ORDERED, false);
        this.logger.debug("Loaded semantically ordered: " + value);
        return value;
    }

    public void saveSemanticallyOrdered(boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SEMANTICALLY_ORDERED, value);
        this.logger.debug("Saved semantically ordered: " + value);
    }


}
