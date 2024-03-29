package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.VocabularyTab;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonEntailmentGeneralPreferencesManager {

    private static final String SET_ID = "EVEE_NON_ENTAILMENT_PREFERENCES";
    private static final String PREFERENCE_ID = "GENERAL";
    private static final String DEFAULT_TAB = "DEFAULT_TAB";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentGeneralPreferencesManager.class);

    public NonEntailmentGeneralPreferencesManager(){
    }

    private Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
    }

    public VocabularyTab loadDefaultVocabularyTab(){
        Preferences preferences = this.getProtegePreferences();
        String defaultTab = preferences.getString(DEFAULT_TAB, "Permitted");
        this.logger.debug("Loaded default vocabulary tab: {}", defaultTab);
        if (defaultTab.equals("Permitted")){
            return VocabularyTab.Permitted;
        } else{
            return VocabularyTab.Forbidden;
        }
    }

    public void saveDefaultVocabularyTab(VocabularyTab vocabularyTab){
        Preferences preferences = this.getProtegePreferences();
        preferences.putString(DEFAULT_TAB, vocabularyTab.toString());
        this.logger.debug("Saved to default vocabulary tab: {}", vocabularyTab);
    }

}
