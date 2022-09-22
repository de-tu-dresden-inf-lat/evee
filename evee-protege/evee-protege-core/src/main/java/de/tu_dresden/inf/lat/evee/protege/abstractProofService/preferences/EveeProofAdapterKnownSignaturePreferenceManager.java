package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EveeProofAdapterKnownSignaturePreferenceManager extends AbstractEveeKnownSignaturePreferencesManager {

    public EveeProofAdapterKnownSignaturePreferenceManager(){
        super();
    }

    public Set<OWLEntity> getKnownSignatureForProofGeneration(OWLOntology activeOntology, String ontologyName){
        boolean currentUseSignature = this.loadUseSignature(ontologyName);
        this.lastUseSignature = currentUseSignature;
        if (! currentUseSignature){
            this.lastUsedClassesStringSignature.clear();
            this.lastUsedObjectPropertiesStringSignature.clear();
            this.lastUsedIndividualsStringSignature.clear();
            OWLClass top = activeOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
            OWLClass bot = activeOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing();
            this.lastUsedClassesStringSignature.add(top.getIRI().toString());
            this.lastUsedClassesStringSignature.add(bot.getIRI().toString());
            HashSet<OWLEntity> result = new HashSet<>();
            result.add(top);
            result.add(bot);
            return result;
        }
        else{
            return this.loadKnownSignature(activeOntology, ontologyName);
        }

    }

    public boolean signatureChanged(long otherTimeStamp, OWLOntology activeOntology, String ontologyName){
        if (timeStamp != otherTimeStamp){
            if (! this.loadUseSignature(ontologyName)){
                ArrayList<String> comparisonList = new ArrayList<>();
                comparisonList.add(activeOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing().getIRI().toString());
                comparisonList.add(activeOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing().getIRI().toString());
                return ! this.lastUsedClassesStringSignature.equals(comparisonList) ||
                        ! (this.lastUsedObjectPropertiesStringSignature.size() == 0) ||
                        ! (this.lastUsedIndividualsStringSignature.size() == 0);
            }
            else {
                boolean classesChanged = ! this.lastUsedClassesStringSignature.equals(
                        this.loadClassSignatureStringList(activeOntology, ontologyName));
                boolean propertiesChanged = ! this.lastUsedObjectPropertiesStringSignature.equals(
                        this.loadObjectPropertiesSignatureStringList(ontologyName));
                boolean individualsChanged = ! this.lastUsedIndividualsStringSignature.equals(
                        this.loadIndividualsSignatureStringList(ontologyName));
                return classesChanged || propertiesChanged || individualsChanged;
            }
        }
        return false;
    }

}
