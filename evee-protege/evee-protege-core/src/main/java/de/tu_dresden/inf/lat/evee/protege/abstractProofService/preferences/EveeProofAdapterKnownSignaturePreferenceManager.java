package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

public class EveeProofAdapterKnownSignaturePreferenceManager extends AbstractEveeSignaturePreferencesManager {



    public EveeProofAdapterKnownSignaturePreferenceManager(){
        super();
    }

    @Override
    protected void initialise(){
        super.initialise();

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

    public boolean useSignature(String ontologyName){
        return this.loadUseSignature(ontologyName);
    }

//    may only be called if useSignature() returned true
    public boolean signatureChanged(long otherTimeStamp, String ontologyName){
        if (timeStamp != otherTimeStamp){
            Integer ontologyID = this.getOntologyID(ontologyName);
            boolean classesChanged = ! this.lastUsedClassesStringSignature.equals(
                    this.loadEntitiesSignatureStringList(ontologyID + this.CLASSES_SUFFIX));
            boolean propertiesChanged = ! this.lastUsedObjectPropertiesStringSignature.equals(
                    this.loadEntitiesSignatureStringList(ontologyID + this.OBJECT_PROPERTIES_SUFFIX));
            boolean individualsChanged = ! this.lastUsedIndividualsStringSignature.equals(
                    this.loadEntitiesSignatureStringList(ontologyID + this.INDIVIDUALS_SUFFIX));
            return classesChanged || propertiesChanged || individualsChanged;
            }
        return false;
    }

    public boolean useSignatureChanged(long otherTimeStamp, String ontologyName){
        if (timeStamp != otherTimeStamp){
            Integer ontologyID = this.getOntologyID(ontologyName);
            if (ontologyID == null) {
                return false;
            }
            Preferences preferences = this.loadPreferences();
            return this.lastUseSignature == preferences.getBoolean(ontologyID + this.USE_SIGNATURE_SUFFIX, true);
        }
        return false;
    }

}
