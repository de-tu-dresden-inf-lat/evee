package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class AbstractEveeProofPreferencesManager {

    protected static final String PREFERENCE_UI = "PREFERENCE_UI_PANEL";
    private final String setId;
    private final String preferenceId;
    private final String proofServiceName;
    protected final SortedSet<String> proofServiceNameSet = new TreeSet<>();
    protected final SortedMap<String, EveeActivationProofPreference> activationDefaultPreferences = new TreeMap<>();
    protected final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesManager.class);

    public AbstractEveeProofPreferencesManager(String setId, String preferenceId, String proofServiceName){
        this.setId = setId;
        this.preferenceId = preferenceId;
        this.proofServiceName = proofServiceName;
        this.createIdentifierSet();
        for (String name : this.proofServiceNameSet){
            EveeActivationProofPreference eveePreference = new EveeActivationProofPreference(
                    true, name);
            this.activationDefaultPreferences.put(name, eveePreference);
        }
    }

    abstract protected void createIdentifierSet();

    public String getSetId(){
        return this.setId;
    }

    public String getPreferenceId(){
        return this.preferenceId;
    }

    public SortedSet<String> getProofServiceNameSet(){
        return this.proofServiceNameSet;
    }

    protected Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(this.getSetId(), this.getPreferenceId());
    }

    public boolean loadIsActive(){
        Preferences preferences = this.getProtegePreferences();
        assert this.proofServiceNameSet.contains(this.proofServiceName);
        boolean defaultValue = this.activationDefaultPreferences.get(this.proofServiceName).getBooleanDefaultValue();
        return preferences.getBoolean(this.proofServiceName, defaultValue);
    }

    public boolean loadIsActive(String key) {
        Preferences preferences = this.getProtegePreferences();
        assert this.proofServiceNameSet.contains(key);
        boolean defaultValue = this.activationDefaultPreferences.get(key).getBooleanDefaultValue();
        return preferences.getBoolean(key, defaultValue);
    }

    public String getIsActiveUILabel(String key) {
        assert this.proofServiceNameSet.contains(key);
        return this.activationDefaultPreferences.get(key).getUiLabel();
    }

    public String getIsActiveToolTip(String key){
        assert this.proofServiceNameSet.contains(key);
        return this.activationDefaultPreferences.get(key).getUiToolTip();
    }

    public void saveIsActive(String key, boolean value) {
        Preferences preferences = this.getProtegePreferences();
        assert this.proofServiceNameSet.contains(key);
        preferences.putBoolean(key, value);
    }

}
