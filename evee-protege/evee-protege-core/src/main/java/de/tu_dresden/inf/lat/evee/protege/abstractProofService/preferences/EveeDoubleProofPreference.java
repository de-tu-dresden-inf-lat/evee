package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

public class EveeDoubleProofPreference extends AbstractEveeProofPreference {

    private double defaultValue;

    public EveeDoubleProofPreference(double defaultValue, String uiLabel, String uiToolTip){
        super(uiLabel, uiToolTip);
        this.defaultValue = defaultValue;
    }

    public Double getDefaultDoubleValue(){
        return this.defaultValue;
    }

    public void setIntegerDefaultValue(int newValue){
        this.defaultValue = newValue;
    }

}
