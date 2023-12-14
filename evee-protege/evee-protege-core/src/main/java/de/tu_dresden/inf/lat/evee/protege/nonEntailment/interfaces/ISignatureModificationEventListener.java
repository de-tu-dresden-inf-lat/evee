package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.SignatureModificationEvent;

public interface ISignatureModificationEventListener {

    void handleSignatureModificationEvent(SignatureModificationEvent event);

}
