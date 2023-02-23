package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public class EveeProofPreferenceInteger extends AbstractEveeProofPreference {

    private int defaultValue;

    public EveeProofPreferenceInteger(String proofServiceName, String preferenceName, int defaultValue, String uiLabel, String uiToolTip){
        super(proofServiceName, preferenceName, uiLabel, uiToolTip);
        this.defaultValue = defaultValue;
    }

    public Integer getDefaultIntegerValue(){
        return this.defaultValue;
    }

    public void setIntegerDefaultValue(int newValue){
        this.defaultValue = newValue;
    }

}
