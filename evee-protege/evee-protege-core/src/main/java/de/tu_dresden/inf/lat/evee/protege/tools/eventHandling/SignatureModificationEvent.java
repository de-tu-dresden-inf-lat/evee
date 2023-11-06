package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;

public class SignatureModificationEvent {

    private final ISignatureModificationEventGenerator source;

    public SignatureModificationEvent(ISignatureModificationEventGenerator source){
        this.source = source;
    }

    public ISignatureModificationEventGenerator getSource(){
        return this.source;
    }

}
