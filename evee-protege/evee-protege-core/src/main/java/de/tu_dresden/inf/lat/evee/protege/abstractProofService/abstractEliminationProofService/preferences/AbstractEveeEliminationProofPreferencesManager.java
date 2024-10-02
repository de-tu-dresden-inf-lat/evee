package de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeBooleanProofPreference;
import org.protege.editor.core.prefs.Preferences;


public abstract class AbstractEveeEliminationProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    protected final EveeBooleanProofPreference useProtegeReasonerDefaultPreference;
    protected final EveeBooleanProofPreference skipStepsDefaultPreference;
    protected final EveeBooleanProofPreference suboptimalWarningDefaultPreference;
    public final String SUBOPTIMAL_MSG = "showSuboptimalProofWarning";
    private final String SUBOPTIMAL_MSG_LABEL = "Show warning for suboptimal proof";
    private final String SUBOPTIMAL_MSG_TOOLTIP = "Show a warning if a suboptimal proof was found after cancellation of the proof service";
    protected final String PROTEGE_REASONER = "useProtegeReasoner";
    protected final String PROTEGE_REASONER_LABEL = "Use Protégé reasoner";
//    todo: test "Protégé" on other operating systems than windows
    protected final String PROTEGE_REASONER_TOOL_TIP = "Use the reasoner currently used by Protégé instead of own Hermit instance";
    protected final String SKIP_STEPS = "skipSteps";
    protected final String SKIP_STEPS_LABEL = "Merge elimination steps";
    protected final String SKIP_STEPS_TOOL_TIP = "Allow elimination of multiple entities at once";

    public AbstractEveeEliminationProofPreferencesManager(String setId, String preferenceId, String identifier) {
        super(setId, preferenceId, identifier);
        this.useProtegeReasonerDefaultPreference = new EveeBooleanProofPreference(
                false, this.PROTEGE_REASONER_LABEL, this.PROTEGE_REASONER_TOOL_TIP);
        this.skipStepsDefaultPreference = new EveeBooleanProofPreference(
                false, this.SKIP_STEPS_LABEL, this.SKIP_STEPS_TOOL_TIP);
        this.suboptimalWarningDefaultPreference = new EveeBooleanProofPreference(
                true, SUBOPTIMAL_MSG_LABEL, SUBOPTIMAL_MSG_TOOLTIP);
    }

    public boolean loadShowSuboptimalProofWarning(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(SUBOPTIMAL_MSG,
                this.suboptimalWarningDefaultPreference.getBooleanDefaultValue());
    }

    public void saveShowSuboptimalProofWarning(boolean value){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SUBOPTIMAL_MSG, value);
    }

    public String getSuboptimalProofWarningUILabel(){
        return this.suboptimalWarningDefaultPreference.getUiLabel();
    }

    public String getSuboptimalProofWarningUIToolTip(){
        return this.suboptimalWarningDefaultPreference.getUiToolTip();
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

}
