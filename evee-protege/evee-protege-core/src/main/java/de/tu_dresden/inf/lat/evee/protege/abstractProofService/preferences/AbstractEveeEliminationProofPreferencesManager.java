package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractEveeEliminationProofPreferencesManager extends AbstractEveeSuboptimalProofPreferencesManager {

    private final EveeBooleanProofPreference useProtegeReasonerDefaultPreference;
    private boolean useProtegeReasonerLastUsedValue;
    private static long useProtegeReasonerTimeStamp;
    private final EveeBooleanProofPreference skipStepsDefaultPreference;
    private boolean skipStepsLastUsedValue;
    private static long skipStepsTimeStamp;
    private final EveeBooleanProofPreference varyJustificationsDefaultPreference;
    private boolean varyJustificationsLastUsedValue;
    private static long varyJustificationsTimeStamp;
    private static boolean initialised = false;
    private final String PROTEGE_REASONER = "useProtegeReasoner";
    //    todo: test "Protégé" on other operating systems than Windows
    private final String PROTEGE_REASONER_LABEL = "Use Protégé reasoner";
    private final String PROTEGE_REASONER_TOOL_TIP = "Use the reasoner currently used by Protégé instead of own Hermit instance";
    private final String SKIP_STEPS = "skipSteps";
    private final String SKIP_STEPS_LABEL = "Allow merging of elimination steps";
    private final String SKIP_STEPS_TOOL_TIP = "Allow elimination of multiple entities at once";
    private final String VARY_JUSTIFICATIONS = "varyJustifications";
    private final String VARY_JUSTIFICATIONS_LABEL = "Vary justifications";
    private final String VARY_JUSTIFICATIONS_TOOL_TIP = "Vary justifications";

    private final Logger logger = LoggerFactory.getLogger(AbstractEveeEliminationProofPreferencesManager.class);

    public AbstractEveeEliminationProofPreferencesManager(String setId, String preferenceId, String identifier) {
        super(setId, preferenceId, identifier);
        this.useProtegeReasonerDefaultPreference = new EveeBooleanProofPreference(
                false, this.PROTEGE_REASONER_LABEL, this.PROTEGE_REASONER_TOOL_TIP);
        this.useProtegeReasonerLastUsedValue = false;
        this.skipStepsDefaultPreference = new EveeBooleanProofPreference(
                true, this.SKIP_STEPS_LABEL, this.SKIP_STEPS_TOOL_TIP);
        this.skipStepsLastUsedValue = true;
        this.varyJustificationsDefaultPreference = new EveeBooleanProofPreference(
                false, VARY_JUSTIFICATIONS_LABEL, VARY_JUSTIFICATIONS_TOOL_TIP);
        this.varyJustificationsLastUsedValue = false;
        if (! initialised){
            useProtegeReasonerTimeStamp = System.currentTimeMillis();
            skipStepsTimeStamp = System.currentTimeMillis();
            varyJustificationsTimeStamp = System.currentTimeMillis();
            initialised = true;
        }
    }

    public boolean loadUseProtegeReasoner() {
        this.useProtegeReasonerLastUsedValue = this.internalLoadUseProtegeReasoner();
        return this.useProtegeReasonerLastUsedValue;
    }

    public boolean useProtegeReasonerChanged(long otherTimeStamp){
        if (useProtegeReasonerTimeStamp != otherTimeStamp){
            return ! this.useProtegeReasonerLastUsedValue == this.internalLoadUseProtegeReasoner();
        }
        return false;
    }

    private boolean internalLoadUseProtegeReasoner(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(PROTEGE_REASONER,
                this.useProtegeReasonerDefaultPreference.getBooleanDefaultValue());
    }

    public long getUseProtegeReasonerTimeStamp(){
        return useProtegeReasonerTimeStamp;
    }

    public void saveUseProtegeReasoner(boolean newValue) {
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(PROTEGE_REASONER, newValue);
        this.logger.debug("Preference useProtegeReasoner saved: " + newValue);
        useProtegeReasonerTimeStamp = System.currentTimeMillis();
    }

    public String getUseProtegeReasonerUILabel(){
        return this.useProtegeReasonerDefaultPreference.getUiLabel();
    }

    public String getUseProtegeReasonerUIToolTip(){
        return this.useProtegeReasonerDefaultPreference.getUiToolTip();
    }

    public boolean loadSkipSteps() {
        this.skipStepsLastUsedValue = this.internalLoadSkipSteps();
        return this.skipStepsLastUsedValue;
    }

    public boolean skipStepsChanged(long otherTimeStamp){
        if (skipStepsTimeStamp != otherTimeStamp){
            return ! this.skipStepsLastUsedValue == this.internalLoadSkipSteps();
        }
        return false;
    }

    private boolean internalLoadSkipSteps(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(SKIP_STEPS,
                this.skipStepsDefaultPreference.getBooleanDefaultValue());
    }

    public long getSkipStepsTimeStamp(){
        return skipStepsTimeStamp;
    }

    public void saveSkipSteps(boolean newValue){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SKIP_STEPS, newValue);
        this.logger.debug("Preference skipSteps saved: " + newValue);
        skipStepsTimeStamp = System.currentTimeMillis();
    }

    public String getSkipStepsUILabel(){
        return this.skipStepsDefaultPreference.getUiLabel();
    }

    public String getSkipStepsUIToolTip(){
        return this.skipStepsDefaultPreference.getUiToolTip();
    }

    public boolean loadVaryJustifications() {
        this.varyJustificationsLastUsedValue = this.internalLoadVaryJustifications();
        return varyJustificationsLastUsedValue;
    }

    public boolean varyJustificationsChanged(long otherTimeStamp){
        if (varyJustificationsTimeStamp != otherTimeStamp){
            return ! this.varyJustificationsLastUsedValue == this.internalLoadVaryJustifications();
        }
        return false;
    }

    private boolean internalLoadVaryJustifications(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(VARY_JUSTIFICATIONS,
                this.varyJustificationsDefaultPreference.getBooleanDefaultValue());
    }

    public long getVaryJustificationsTimeStamp(){
        return varyJustificationsTimeStamp;
    }

    public void saveVaryJustifications(boolean newValue) {
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(VARY_JUSTIFICATIONS, newValue);
        this.logger.debug("Preference varyJustifications saved: " + newValue);
        varyJustificationsTimeStamp = System.currentTimeMillis();
    }

    public String getVaryJustificationsUILabel(){
        return this.varyJustificationsDefaultPreference.getUiLabel();
    }

    public String getVaryJustificationsUIToolTip(){
        return this.varyJustificationsDefaultPreference.getUiToolTip();
    }

}
