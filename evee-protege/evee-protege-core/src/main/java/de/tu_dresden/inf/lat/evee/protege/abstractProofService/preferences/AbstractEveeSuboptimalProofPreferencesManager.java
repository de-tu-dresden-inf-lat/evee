package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeBooleanProofPreference;
import org.protege.editor.core.prefs.Preferences;

public abstract class AbstractEveeSuboptimalProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    protected final EveeBooleanProofPreference suboptimalWarningDefaultPreference;
    public final String SUBOPTIMAL_MSG = "showSuboptimalProofWarning";
    private final String SUBOPTIMAL_MSG_LABEL = "Show warning for suboptimal proof";
    private final String SUBOPTIMAL_MSG_TOOLTIP = "Show a warning if a suboptimal proof was found after cancellation of the proof service";

    public AbstractEveeSuboptimalProofPreferencesManager(String setId, String preferenceId, String identifier){
        super(setId, preferenceId, identifier);
        this.suboptimalWarningDefaultPreference = new EveeBooleanProofPreference(
                true, SUBOPTIMAL_MSG_LABEL, SUBOPTIMAL_MSG_TOOLTIP);
    }

    @Override
    abstract protected void createIdentifierSet();

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

}
