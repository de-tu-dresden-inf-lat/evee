package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.prefs.Preferences;

import java.util.TreeMap;

//todo: hier gehts weiter

public abstract class AbstractEveeEliminationProofPreferencesManager extends AbstractEveeProofPreferencesManager{

    private final TreeMap<String, EveeIntegerProofPreference> integerDefaultPreferences = new TreeMap<>();
    protected final String TIME_OUT = "timeOut";
    //    todo: place-holder labels and tool tips for now
    protected final String TIME_OUT_LABEL = "Reasoner timeout:";
    protected final String TIME_OUT_TOOL_TIP = "Set the time out for the reasoner used by the proof service";
    protected final String PROTEGE_REASONER = "useProtegeReasoner";
    protected final String PROTEGE_REASONER_LABEL = "Use Protégé reasoner";
    protected final String PROTEGE_REASONER_TOOL_TIP = "Use the reasoner currently used by Protégé";
    protected final String SKIP_STEPS = "skipSteps";
    protected final String SKIP_STEPS_LABEL = "Merge reasoning steps";
    protected final String SKIP_STEPS_TOOL_TIP = "Merge several smaller reasoning steps into one";

    public AbstractEveeEliminationProofPreferencesManager(String setId, String preferenceId, String identifier) {
        super(setId, preferenceId, identifier);
        this.initializeEliminationProofPreferences();
    }

//    public AbstractEveeEliminationProofPreferencesManager(String setId, String preferenceId) {
//        super(setId, preferenceId);
//        this.initializeEliminationProofPreferences();
//    }

    protected void initializeEliminationProofPreferences(){
        EveeBooleanProofPreference protegeReasoner = new EveeBooleanProofPreference(
                false, this.PROTEGE_REASONER_LABEL, this.PROTEGE_REASONER_TOOL_TIP);
        this.booleanDefaultPreferences.put(PROTEGE_REASONER, protegeReasoner);
        EveeBooleanProofPreference skipSteps = new EveeBooleanProofPreference(
                false, this.SKIP_STEPS_LABEL, this.SKIP_STEPS_TOOL_TIP);
        this.booleanDefaultPreferences.put(SKIP_STEPS, skipSteps);
        EveeIntegerProofPreference timeOut = new EveeIntegerProofPreference(
                1000, this.TIME_OUT_LABEL, this.TIME_OUT_TOOL_TIP);
        this.integerDefaultPreferences.put(TIME_OUT, timeOut);
    }

    protected boolean loadUseProtegeReasoner() {
        Preferences preferences = this.getProtegePreferences();
        boolean defaultValue = this.booleanDefaultPreferences.get(PROTEGE_REASONER).getBooleanDefaultValue();
        return preferences.getBoolean(PROTEGE_REASONER, defaultValue);
    }

    protected String getUseProtegeReasonerUILabel(){
        return this.booleanDefaultPreferences.get(PROTEGE_REASONER).getUiLabel();
    }

    protected String getUseProtegeReasonerUIToolTip(){
        return this.booleanDefaultPreferences.get(PROTEGE_REASONER).getUiToolTip();
    }

    protected boolean loadSkipSteps() {
        Preferences preferences = this.getProtegePreferences();
        boolean defaultValue = this.booleanDefaultPreferences.get(SKIP_STEPS).getBooleanDefaultValue();
        return preferences.getBoolean(SKIP_STEPS, defaultValue);
    }

    protected String getSkipStepsUILabel(){
        return this.booleanDefaultPreferences.get(SKIP_STEPS).getUiLabel();
    }

    protected String getSkipStepsUIToolTip(){
        return this.booleanDefaultPreferences.get(SKIP_STEPS).getUiToolTip();
    }

    protected int loadTimeOut(){
        Preferences preferences = this.getProtegePreferences();
        int defaultValue = this.integerDefaultPreferences.get(TIME_OUT).getDefaultIntegerValue();
        return preferences.getInt(TIME_OUT, defaultValue);
    }

    protected String getTimeOutUILabel(){
        return this.integerDefaultPreferences.get(TIME_OUT).getUiLabel();
    }

    protected String getTimeOutUIToolTip(){
        return this.integerDefaultPreferences.get(TIME_OUT).getUiToolTip();
    }

    protected void saveIntegerPreferenceValue(String key, int value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putInt(key, value);
    }

    @Override
    abstract protected void createIdentifierSet();

}
