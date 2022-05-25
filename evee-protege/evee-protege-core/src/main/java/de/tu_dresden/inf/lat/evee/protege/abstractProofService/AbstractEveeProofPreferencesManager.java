package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class AbstractEveeProofPreferencesManager {

    protected static final String PREFERENCE_UI = "PREFERENCE_UI_PANEL";
    private final String setId;
    private final String preferenceId;
    private final String proofServiceName;
    protected final TreeSet<String> proofServiceNameSet = new TreeSet<>();
    protected final TreeMap<String, EveeActivationProofPreference> activationDefaultPreferences = new TreeMap<>();
    protected final TreeMap<String, EveeBooleanProofPreference> booleanDefaultPreferences = new TreeMap<>();
    public final String SUBOPTIMAL_MSG = "showSuboptimalProofWarning";
    private final String SUBOPTIMAL_MSG_LABEL = "Show warning for suboptimal proof";
    private final String SUBOPTIMAL_MSG_TOOLTIP = "Show a warning if a suboptimal proof was found after cancellation of the proof service";
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
        EveeBooleanProofPreference suboptimalWarning = new EveeBooleanProofPreference(
                true, SUBOPTIMAL_MSG_LABEL, SUBOPTIMAL_MSG_TOOLTIP);
        this.booleanDefaultPreferences.put(SUBOPTIMAL_MSG, suboptimalWarning);
    }

    abstract protected void createIdentifierSet();

    public String getSetId(){
        return this.setId;
    }

    public String getPreferenceId(){
        return this.preferenceId;
    }

    public String getProofServiceName(){
        return this.proofServiceName;
    }

    public TreeSet<String> getProofServiceNameSet(){
        return this.proofServiceNameSet;
    }

    protected Preferences getProtegePreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(this.getSetId(), this.getPreferenceId());
    }

    public boolean loadIsActive(){
        Preferences preferences = this.getProtegePreferences();
        boolean defaultValue = this.activationDefaultPreferences.get(this.proofServiceName).getBooleanDefaultValue();
        return preferences.getBoolean(this.proofServiceName, defaultValue);
    }

    //    todo: should we ensure that key is present in activationDefaultPreferences??
    public boolean loadIsActive(String key) {
        Preferences preferences = this.getProtegePreferences();
        boolean defaultValue = this.activationDefaultPreferences.get(key).getBooleanDefaultValue();
        return preferences.getBoolean(key, defaultValue);
    }

    public String getIsActiveUILabel(String key) {
        return this.activationDefaultPreferences.get(key).getUiLabel();
    }

    public String getIsActiveToolTip(String key){
        return this.activationDefaultPreferences.get(key).getUiToolTip();
    }

    //    todo: should we ensure that key is present in activationDefaultPreferences??
    protected void saveIsActive(String key, boolean value) {
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(key, value);
    }

    public boolean loadShowSuboptimalProofWarning(){
        Preferences preferences = this.getProtegePreferences();
        boolean defaultValue = this.booleanDefaultPreferences.get(SUBOPTIMAL_MSG).getBooleanDefaultValue();
        return preferences.getBoolean(SUBOPTIMAL_MSG, defaultValue);
    }

    public void saveShowSuboptimalProofWarning(boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SUBOPTIMAL_MSG, value);
    }

    public String getSuboptimalProofWarningUILabel(){
        return this.booleanDefaultPreferences.get(SUBOPTIMAL_MSG).getUiLabel();
    }

    public String getSuboptimalProofWarningUIToolTip(){
        return this.booleanDefaultPreferences.get(SUBOPTIMAL_MSG).getUiToolTip();
    }

    public void saveBooleanPreferenceValue(String key, boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(key, value);
    }

    //    public AbstractEveeProofPreferencesManager(String setId, String preferenceId){
//        this.setId = setId;
//        this.preferenceId = preferenceId;
//        this.proofServiceName = PREFERENCE_UI;
//        this.initialize();
//    }
//
//    protected void initialize(){
//        this.createIdentifierSet();
//        for (String proofServiceName : this.proofServiceNameSet){
//            EveeActivationProofPreference eveePreference = new EveeActivationProofPreference(
//                    true, proofServiceName);
//            this.activationDefaultPreferences.put(proofServiceName, eveePreference);
//        }
//        EveeBooleanProofPreference suboptimalWarning = new EveeBooleanProofPreference(
//                true, SUBOPTIMAL_MSG_LABEL, SUBOPTIMAL_MSG_TOOLTIP);
//        this.booleanDefaultPreferences.put(SUBOPTIMAL_MSG, suboptimalWarning);
//    }
//
//    protected void createActivationDefaultPreferences(){
//        for (String proofServiceName : this.proofServiceNameSet){
//            EveeActivationProofPreference eveePreference = new EveeActivationProofPreference(
//                    true, proofServiceName);
//            this.activationDefaultPreferences.put(proofServiceName, eveePreference);
//        }
//    }
//
//    protected void ensureActivtionDefaultPrefrences() throws EveeProofPreferecenRetrievalException{
//        for (String identifier : this.identifierSet){
//            if (! this.activationDefaultPreferences.containsKey(identifier)){
//                throw new EveeProofPreferecenRetrievalException(
//                        "Error during initialization: No default activation preference set for identifier <" + identifier + ">");
//            }
//        }
//    }

}
