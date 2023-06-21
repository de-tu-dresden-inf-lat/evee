package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EveeProofSignatureUIPreferenceManager extends AbstractEveeSignaturePreferencesManager {

    private Logger logger = LoggerFactory.getLogger(EveeProofSignatureUIPreferenceManager.class);

    public EveeProofSignatureUIPreferenceManager(){
        super();
    }

    public Set<OWLEntity> getKnownSignatureForUI(OWLOntology activeOntology, String ontologyName){
        if (this.isNewOntology(ontologyName)){
            OWLDataFactory df = activeOntology.getOWLOntologyManager().getOWLDataFactory();
            OWLClass top = df.getOWLThing();
            OWLClass bot = df.getOWLNothing();
            Set<OWLEntity> result = new HashSet<>();
            result.add(top);
            result.add(bot);
            return result;
        }
        return this.loadKnownSignature(activeOntology, ontologyName);
    }

    public boolean getUseSignatureForUI(String ontologyName){
        return this.loadUseSignature(ontologyName);
    }

}
