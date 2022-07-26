package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEveeSuboptimalProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    protected final EveeBooleanProofPreference suboptimalWarningDefaultPreference;
    public final String SUBOPTIMAL_MSG = "showSuboptimalProofWarning";
    private final String SUBOPTIMAL_MSG_LABEL = "Show warning for suboptimal proof";
    private final String SUBOPTIMAL_MSG_TOOLTIP = "Show a warning if a suboptimal proof was found after cancellation of the proof service";
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeSuboptimalProofPreferencesManager.class);

    public AbstractEveeSuboptimalProofPreferencesManager(String setId, String preferenceId, String identifier){
        super(setId, preferenceId, identifier);
        this.suboptimalWarningDefaultPreference = new EveeBooleanProofPreference(
                true, SUBOPTIMAL_MSG_LABEL, SUBOPTIMAL_MSG_TOOLTIP);
    }

    public boolean loadShowSuboptimalProofWarning(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getBoolean(SUBOPTIMAL_MSG,
                this.suboptimalWarningDefaultPreference.getBooleanDefaultValue());
    }

    public void saveShowSuboptimalProofWarning(boolean newValue){
        Preferences preferences = this.getProtegePreferences();
        preferences.putBoolean(SUBOPTIMAL_MSG, newValue);
        this.logger.debug("Preference showSuboptimalProofWarning saved: " + newValue);
    }

    public String getSuboptimalProofWarningUILabel(){
        return this.suboptimalWarningDefaultPreference.getUiLabel();
    }

    public String getSuboptimalProofWarningUIToolTip(){
        return this.suboptimalWarningDefaultPreference.getUiToolTip();
    }

}
