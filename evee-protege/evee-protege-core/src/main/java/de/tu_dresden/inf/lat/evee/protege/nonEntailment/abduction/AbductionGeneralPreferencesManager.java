package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbductionGeneralPreferencesManager {

    private static final String SET_ID = "EVEE_ABDUCTION_PREFERENCES";
    private static final String PREFERENCE_ID = "GENERAL";

    private static final String MAX_HYPOTHESIS = "MAX_HYPOTHESIS";

    private Logger logger = LoggerFactory.getLogger(AbductionGeneralPreferencesManager.class);

    public AbductionGeneralPreferencesManager(){

    }

    private Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
    }

    public int loadMaximumHypothesisNumber(){
        Preferences preferences = this.getProtegePreferences();
        int maxHypothesis = preferences.getInt(MAX_HYPOTHESIS, 10);
        this.logger.debug("Loaded maximum number of hypothesis from preferences: {}", maxHypothesis);
        return maxHypothesis;
    }

    public void saveMaximumHypothesisNumber(int maxHypothesis){
        Preferences preferences = this.getProtegePreferences();
        preferences.putInt(MAX_HYPOTHESIS, maxHypothesis);
        this.logger.debug("Saved maximum number of hypothesis to preferences: {}", maxHypothesis);
    }

}
