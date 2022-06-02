package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

public abstract class AbstractEveeProofPreference {

    private final String uiLabel;
    private final String uiToolTip;

    protected AbstractEveeProofPreference(String uiLabel, String uiToolTip) {
        this.uiLabel = uiLabel;
        this.uiToolTip = uiToolTip;
    }

    public String getUiLabel(){
        return this.uiLabel;
    }

    public String getUiToolTip(){
        return this.uiToolTip;
    }

}
