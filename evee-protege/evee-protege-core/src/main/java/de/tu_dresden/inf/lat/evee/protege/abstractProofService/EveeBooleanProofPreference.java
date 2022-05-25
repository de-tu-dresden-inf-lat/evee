package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public class EveeBooleanProofPreference extends AbstractEveeProofPreference {

    private boolean defaultValue;

    public EveeBooleanProofPreference(boolean defaultValue, String uiLabel, String uiToolTip){
        super(uiLabel, uiToolTip);
        this.defaultValue = defaultValue;
    }

    public boolean getBooleanDefaultValue(){
        return this.defaultValue;
    }

    public void setBooleanDefaultValue(boolean newValue){
        this.defaultValue = newValue;
    }

}
