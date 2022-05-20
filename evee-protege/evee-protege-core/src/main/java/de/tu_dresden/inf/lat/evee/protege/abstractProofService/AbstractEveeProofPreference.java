package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public abstract class AbstractEveeProofPreference {

    private final String proofServiceName;
    private final String preferenceName;
    private final String uiLabel;
    private final String uiToolTip;

    protected AbstractEveeProofPreference(String proofServiceName, String preferenceName, String uiLabel, String uiToolTip) {
        this.proofServiceName = proofServiceName;
        this.preferenceName = preferenceName;
        this.uiLabel = uiLabel;
        this.uiToolTip = uiToolTip;
    }

    public String getProofServiceName(){
        return this.proofServiceName;
    }

    public String getPreferenceName(){
        return this.preferenceName;
    };

    public String getUiLabel(){
        return this.uiLabel;
    }

    public String getUiToolTip(){
        return this.uiToolTip;
    }

}
