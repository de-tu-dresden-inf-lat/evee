package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

public interface ISignatureModificationEventGenerator {

    void registerSignatureModificationEventListener(ISignatureModificationEventListener listener);

}
