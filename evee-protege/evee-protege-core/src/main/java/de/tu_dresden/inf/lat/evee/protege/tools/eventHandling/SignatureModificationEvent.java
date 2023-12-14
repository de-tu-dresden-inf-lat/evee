package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.Set;

public class SignatureModificationEvent {

    private final Set<OWLEntity> additionalSignatureNames;

    public SignatureModificationEvent(Set<OWLEntity> additionalSignatureNames){
        this.additionalSignatureNames = new HashSet<>(additionalSignatureNames);
    }

    public Set<OWLEntity> getAdditionalSignatureNames(){
        return this.additionalSignatureNames;
    }

}
