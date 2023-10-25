package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.VocabularyTab;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.PreferencesChangeListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEventType;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NonEntailmentGeneralPreferencesManager {

    private static final String SET_ID = "EVEE_NON_ENTAILMENT_PREFERENCES";
    private static final String PREFERENCE_ID = "GENERAL";
    private static final String DEFAULT_TAB = "DEFAULT_TAB";
    private static final String SHOW_FILTER_WARNING = "SHOW_FILTER_WARNING";
    private static final String SIGNATURE_COMPONENT_LAYOUT = "SIGNATURE_COMPONENT_LAYOUT";
    public static final String STANDARD_LAYOUT = "Standard";
    public static final String ALTERNATIVE_LAYOUT_LISTS = "Alternative";
    private static final List<PreferencesChangeListener> changeListeners = new ArrayList<>();
    private static NonEntailmentGeneralPreferencesManager preferencesManager = null;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentGeneralPreferencesManager.class);

    public NonEntailmentGeneralPreferencesManager(){
    }

    public void registerPreferencesChangeListener(PreferencesChangeListener changeListener){
        changeListeners.add(changeListener);
    }

    public void removePreferenceChangeListener(PreferencesChangeListener changeListener){
        changeListeners.remove(changeListener);
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

    public boolean loadShowFilterWarningMessage(){
        Preferences preferences = this.getProtegePreferences();
        boolean showWarning = preferences.getBoolean(SHOW_FILTER_WARNING, true);
        this.logger.debug("Loaded boolean show filter warning message: {}", showWarning);
        return showWarning;
    }

    public void saveShowFilterWarningMessage(boolean showFilterWarning){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SHOW_FILTER_WARNING, showFilterWarning);
        this.logger.debug("Saved boolean show filter warning message: {}", showFilterWarning);
    }

    public String loadSignatureComponentLayout(){
        Preferences preferences = this.getProtegePreferences();
        String layout = preferences.getString(SIGNATURE_COMPONENT_LAYOUT, STANDARD_LAYOUT);
        this.logger.debug("Loaded layout information: {}", layout);
        return layout;
    }

    public void saveSignatureComponentLayout(String newLayout){
        Preferences preferences = this.getProtegePreferences();
        preferences.putString(SIGNATURE_COMPONENT_LAYOUT, newLayout);
        this.logger.debug("Saved layout information: {}", newLayout);
        this.updateListeners();
    }

    public Vector<String> getLayoutStrings(){
        Vector<String> layouts = new Vector<>();
        layouts.add(STANDARD_LAYOUT);
        layouts.add(ALTERNATIVE_LAYOUT_LISTS);
        return  layouts;
    }

    private void updateListeners(){
        for (PreferencesChangeListener changeListener : changeListeners){
            changeListener.handlePreferenceChange(GeneralPreferencesChangeEventType.LAYOUT_CHANGE);
        }
    }

    public static NonEntailmentGeneralPreferencesManager getInstance(){
        if (preferencesManager == null) {
            preferencesManager = new NonEntailmentGeneralPreferencesManager();
        }
        return preferencesManager;
    }

}
