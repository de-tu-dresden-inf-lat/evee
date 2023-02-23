package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

public abstract class AbstractEveeProofPreferencesManager {

    public static final String GENERAL_NAME = "GENERAL_PREFERENCES";
    public static final String ACTIVE = "isActive";
    private final String ACTIVE_LABEL = "Activate ";
    private final String ACTIVE_TOOLTIP = "Turns the proof service on and off";
    public static final String SUBOPTIMAL_MSG = "showSuboptimalProofWarning";
    private final String SUBOPTIMAL_MSG_LABEL = "Don't show warning for suboptimal proof";
    private final String SUBOTPIMAL_MSG_TOOLTIP = "Show a warning if a suboptimal proof was found after cancellation of the proof service.";
    private final String baseKey;
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesManager.class);

    public AbstractEveeProofPreferencesManager(String baseKey){
        this.baseKey = baseKey;
        if (baseKey.equals(GENERAL_NAME)){
            if (! this.getDefaultBooleanPreferences().containsKey(SUBOPTIMAL_MSG)){
                EveeProofPreferenceBoolean suboptimalMessagePreference = new EveeProofPreferenceBoolean(
                        GENERAL_NAME, SUBOPTIMAL_MSG, true, SUBOPTIMAL_MSG_LABEL, SUBOTPIMAL_MSG_TOOLTIP);
                this.getDefaultBooleanPreferences().put(baseKey + SUBOPTIMAL_MSG, suboptimalMessagePreference);
                this.logger.debug("suboptimal proof message preference set with key " + this.baseKey + SUBOPTIMAL_MSG);
            }
        }
        else {
            EveeProofPreferenceBoolean activationPreference = new EveeProofPreferenceBoolean(
                    this.baseKey, ACTIVE, true, this.ACTIVE_LABEL + this.baseKey, this.ACTIVE_TOOLTIP);
            this.getDefaultBooleanPreferences().put(this.baseKey + ACTIVE, activationPreference);
            this.logger.debug("activation preference set for key " + this.baseKey + ACTIVE);
        }
    }

    abstract public String getSetId();

    abstract public String getPreferenceId();

    abstract public HashMap<String, EveeProofPreferenceBoolean> getDefaultBooleanPreferences();

    abstract public HashMap<String, EveeProofPreferenceInteger> getDefaultIntegerPreferences();

    private Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(this.getSetId(), this.getPreferenceId());
    }

    public EveeProofPreferenceBoolean getDefaultPreferenceBoolean(String key) {
        return this.getDefaultBooleanPreferences().get(key);
    }

    public Set<String> getDefaultPreferenceKeysBoolean(){
        return this.getDefaultBooleanPreferences().keySet();
    }

    public void setDefaultPreferenceBoolean(String key, boolean value){
        EveeProofPreferenceBoolean booleanPreference = this.getDefaultBooleanPreferences().get(key);
        booleanPreference.setBooleanDefaultValue(value);
    }

    public void setProtegePreferenceBoolean(String key, boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(key, value);
    }

    public EveeProofPreferenceInteger getDefaultPreferenceInteger(String key) {
        return this.getDefaultIntegerPreferences().get(key);
    }

    public Set<String> getDefaultPreferenceKeysInteger() {
        return this.getDefaultIntegerPreferences().keySet();
    }

    public void setDefaultPreferenceInteger(String key, Integer value){
        EveeProofPreferenceInteger integerPreference = this.getDefaultIntegerPreferences().get(key);
        integerPreference.setIntegerDefaultValue(value);
    }

    public void setProtegePreferenceInteger(String key, int value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putInt(key, value);
    }

    public String getBaseKey(){
        return this.baseKey;
    }

    public void setDefaultIsActive(boolean newValue){
        String key = this.baseKey + ACTIVE;
        this.getDefaultBooleanPreferences().get(key).setBooleanDefaultValue(newValue);
    }

    public boolean getProtegeIsActive(){
        String key = this.baseKey + ACTIVE;
        boolean defaultValue = this.getDefaultBooleanPreferences().get(key).getBooleanDefaultValue();
        return this.getProtegePreferences().getBoolean(key, defaultValue);
    }

    public void setProtegeIsActive(boolean newValue){
        String key = this.baseKey + ACTIVE;
        this.getProtegePreferences().putBoolean(key, newValue);
    }

    public boolean getProtegeShowSuboptimalProofMessage(){
        String key = this.baseKey + SUBOPTIMAL_MSG;
        boolean defaultValue = this.getDefaultBooleanPreferences().get(key).getBooleanDefaultValue();
        return this.getProtegePreferences().getBoolean(key, defaultValue);
    }

    public void setProtegeShowSuboptimalProofMessage(boolean newValue){
        String key = this.baseKey + SUBOPTIMAL_MSG;
        this.getProtegePreferences().putBoolean(key, newValue);
    }

    public boolean getProtegePreferenceBoolean(String key) throws EveeProofPreferecenRetrievalException {
        if (! this.getDefaultBooleanPreferences().containsKey(key)){
            throw new EveeProofPreferecenRetrievalException("No default boolean preference set for key " + key);
        }
        Preferences protegePreferences = this.getProtegePreferences();
        return protegePreferences.getBoolean(key, this.getDefaultBooleanPreferences().get(key).getBooleanDefaultValue());
    }

    public Integer getProtegePreferenceInteger(String key) throws EveeProofPreferecenRetrievalException {
        if (! this.getDefaultIntegerPreferences().containsKey(key)){
            throw new EveeProofPreferecenRetrievalException("No default integer preference set for key " + key);
        }
        Preferences protegePreferences = this.getProtegePreferences();
        return protegePreferences.getInt(key, this.getDefaultIntegerPreferences().get(key).getDefaultIntegerValue());
    }

}
