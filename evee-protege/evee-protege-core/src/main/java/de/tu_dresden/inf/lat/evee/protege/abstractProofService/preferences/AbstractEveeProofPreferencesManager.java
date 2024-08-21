package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractEveeProofPreferencesManager {

    protected static final String PREFERENCE_UI = "PREFERENCE_UI_PANEL";
    private static final String UI_TOOL_TIP = "Turns the proof service on and off";
    private final String setId;
    private final String preferenceId;
    private final String proofServiceID;
    protected final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesManager.class);

    public AbstractEveeProofPreferencesManager(String setId, String preferenceId, String proofServiceID){
        this.setId = setId;
        this.preferenceId = preferenceId;
        this.proofServiceID = proofServiceID;
    }

    abstract public SortedSet<String> getProofServiceIDs();

    abstract protected String parseProofServiceID2Name(String proofServiceID);

    public String getSetId(){
        return this.setId;
    }

    public String getPreferenceId(){
        return this.preferenceId;
    }

    public String getProofServiceName(){
        return this.parseProofServiceID2Name(this.proofServiceID);
    }

    protected Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(this.getSetId(), this.getPreferenceId());
    }

    public boolean loadIsActive(){
        Preferences preferences = this.getProtegePreferences();
        assert this.getProofServiceIDs().contains(this.proofServiceID);
        return preferences.getBoolean(this.proofServiceID, true);
    }

    public boolean loadIsActive(String key) {
        Preferences preferences = this.getProtegePreferences();
        assert this.getProofServiceIDs().contains(key);
        return preferences.getBoolean(key, true);
    }

    abstract public String getIsActiveUILabel(String key);

    public String getIsActiveToolTip(String key) {
        return UI_TOOL_TIP;
    }


    public void saveIsActive(String key, boolean value) {
        Preferences preferences = this.getProtegePreferences();
        assert this.getProofServiceIDs().contains(key);
        preferences.putBoolean(key, value);
        this.logger.debug("Preference isActive saved: " + value + " for: " + key);
    }

}
