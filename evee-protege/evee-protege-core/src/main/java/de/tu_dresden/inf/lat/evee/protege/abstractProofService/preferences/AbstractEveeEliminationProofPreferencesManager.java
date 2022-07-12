package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;


public abstract class AbstractEveeEliminationProofPreferencesManager extends AbstractEveeSuboptimalProofPreferencesManager {

    protected final EveeBooleanProofPreference useProtegeReasonerDefaultPreference;
    protected final EveeBooleanProofPreference skipStepsDefaultPreference;
    protected final EveeBooleanProofPreference varyJustificationsDefaultPreference;
    protected final String PROTEGE_REASONER = "useProtegeReasoner";
    protected final String PROTEGE_REASONER_LABEL = "Use Protégé reasoner";
//    todo: test "Protégé" on other operating systems than windows
    protected final String PROTEGE_REASONER_TOOL_TIP = "Use the reasoner currently used by Protégé instead of own Hermit instance";
    protected final String SKIP_STEPS = "skipSteps";
    protected final String SKIP_STEPS_LABEL = "Allow merging of elimination steps";
    protected final String SKIP_STEPS_TOOL_TIP = "Allow elimination of multiple entities at once";
    protected final String VARY_JUSTIFICATIONS = "veryJustifications";
    protected final String VARY_JUSTIFICATIONS_LABEL = "Vary justifications";
    protected final String VARY_JUSTIFICATIONS_TOOL_TIP = "Vary justifications";

    public AbstractEveeEliminationProofPreferencesManager(String setId, String preferenceId, String identifier) {
        super(setId, preferenceId, identifier);
        this.useProtegeReasonerDefaultPreference = new EveeBooleanProofPreference(
                false, this.PROTEGE_REASONER_LABEL, this.PROTEGE_REASONER_TOOL_TIP);
        this.skipStepsDefaultPreference = new EveeBooleanProofPreference(
                true, this.SKIP_STEPS_LABEL, this.SKIP_STEPS_TOOL_TIP);
        this.varyJustificationsDefaultPreference = new EveeBooleanProofPreference(
                false, VARY_JUSTIFICATIONS_LABEL, VARY_JUSTIFICATIONS_TOOL_TIP);
    }

    public boolean loadUseProtegeReasoner() {
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(PROTEGE_REASONER,
                this.useProtegeReasonerDefaultPreference.getBooleanDefaultValue());
    }

    public void saveUseProtegeReasoner(boolean newValue) {
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(PROTEGE_REASONER, newValue);
    }

    public String getUseProtegeReasonerUILabel(){
        return this.useProtegeReasonerDefaultPreference.getUiLabel();
    }

    public String getUseProtegeReasonerUIToolTip(){
        return this.useProtegeReasonerDefaultPreference.getUiToolTip();
    }

    public boolean loadSkipSteps() {
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(SKIP_STEPS, this.skipStepsDefaultPreference.getBooleanDefaultValue());
    }

    public void saveSkipSteps(boolean newValue){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SKIP_STEPS, newValue);
    }

    public String getSkipStepsUILabel(){
        return this.skipStepsDefaultPreference.getUiLabel();
    }

    public String getSkipStepsUIToolTip(){
        return this.skipStepsDefaultPreference.getUiToolTip();
    }

    public boolean loadVaryJustifications() {
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(VARY_JUSTIFICATIONS, this.varyJustificationsDefaultPreference.getBooleanDefaultValue());
    }

    public void saveVaryJustifiactions(boolean newValue) {
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(VARY_JUSTIFICATIONS, newValue);
    }

    public String getVaryJustificationsUILabel(){
        return this.varyJustificationsDefaultPreference.getUiLabel();
    }

    public String getVaryJustificationsUIToolTip(){
        return this.varyJustificationsDefaultPreference.getUiToolTip();
    }

    @Override
    abstract protected void createIdentifierSet();

}
