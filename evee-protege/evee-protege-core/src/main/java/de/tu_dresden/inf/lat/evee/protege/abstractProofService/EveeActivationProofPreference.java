package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

public class EveeActivationProofPreference extends EveeBooleanProofPreference {

    private static final String UI_TOOL_TIP = "Turns the proof service on and off";

    public EveeActivationProofPreference(boolean defaultValue, String proofServiceName) {
        super(defaultValue, proofServiceName, UI_TOOL_TIP);
    }
}
