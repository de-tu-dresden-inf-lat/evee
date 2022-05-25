package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public class EveeIntegerProofPreference extends AbstractEveeProofPreference {

    private int defaultValue;

    public EveeIntegerProofPreference(int defaultValue, String uiLabel, String uiToolTip){
        super(uiLabel, uiToolTip);
        this.defaultValue = defaultValue;
    }

    public Integer getDefaultIntegerValue(){
        return this.defaultValue;
    }

    public void setIntegerDefaultValue(int newValue){
        this.defaultValue = newValue;
    }

}
