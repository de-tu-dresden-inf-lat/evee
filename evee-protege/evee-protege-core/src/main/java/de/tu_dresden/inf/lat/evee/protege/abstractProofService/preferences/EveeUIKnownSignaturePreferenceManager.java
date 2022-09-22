package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public class EveeUIKnownSignaturePreferenceManager extends AbstractEveeKnownSignaturePreferencesManager {

    public EveeUIKnownSignaturePreferenceManager(){
        super();
    }

    public Set<OWLEntity> getKnownSignatureForUI(OWLOntology activeOntology, String ontologyName){
        return this.loadKnownSignature(activeOntology, ontologyName);
    }

    public boolean getUseSignatureForUI(String ontologyName){
        return this.loadUseSignature(ontologyName);
    }

}
