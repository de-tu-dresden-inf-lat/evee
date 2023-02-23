package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public class EveeProofPreferenceBoolean extends AbstractEveeProofPreference {

    private boolean defaultValue;

    public EveeProofPreferenceBoolean(String proofServiceName, String preferenceName, boolean defaultValue, String uiLabel, String uiToolTip){
        super(proofServiceName, preferenceName, uiLabel, uiToolTip);
        this.defaultValue = defaultValue;
    }

    public boolean getBooleanDefaultValue(){
        return this.defaultValue;
    }

    public void setBooleanDefaultValue(boolean newValue){
        this.defaultValue = newValue;
    }

}
