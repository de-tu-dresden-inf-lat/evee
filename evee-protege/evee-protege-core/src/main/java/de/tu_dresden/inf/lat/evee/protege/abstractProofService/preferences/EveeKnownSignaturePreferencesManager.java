package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import com.kitfox.svg.A;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class EveeKnownSignaturePreferencesManager {

    private static final String SET_ID = "EVEE_GENERAL_PREFERENCES";
    private final String preferenceId = "EVEE_KNOWN_SIGNATURE";
    private final String CLASSES_SUFFIX = "_CLASSES";
    private final String OBJECT_PROPERTIES_SUFFIX = "OBJECT_PROPERTIES";
    private final String INDIVIDUALS_SUFFIX = "_INDIVIDUALS";
    private static long timeStamp;
    private static boolean initialised = false;
    private List<String> lastUsedClassesStringSignature;
    private List<String> lastUsedObjectPropertiesStringSignature;
    private List<String> lastUsedIndividualsStringSignature;
    private final Logger logger = LoggerFactory.getLogger(EveeKnownSignaturePreferencesManager.class);

    public EveeKnownSignaturePreferencesManager(){
        this.lastUsedClassesStringSignature = new ArrayList<>();
        this.lastUsedObjectPropertiesStringSignature = new ArrayList<>();
        this.lastUsedIndividualsStringSignature = new ArrayList<>();
        if (! initialised){
            timeStamp =  System.currentTimeMillis();
        }
    }

    public Set<OWLEntity> loadKnownSignature(OWLOntology activeOntology, String ontologyName) {
        ontologyName = this.escapeOntologyName(ontologyName);
        this.lastUsedClassesStringSignature = this.loadSignatureStringList(ontologyName + this.CLASSES_SUFFIX);
        this.lastUsedObjectPropertiesStringSignature = this.loadSignatureStringList(ontologyName + this.OBJECT_PROPERTIES_SUFFIX);
        this.lastUsedIndividualsStringSignature = this.loadSignatureStringList(ontologyName + this.INDIVIDUALS_SUFFIX);
        Set<OWLEntity> owlEntitySet = new HashSet<>();
        activeOntology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            if (lastUsedClassesStringSignature.contains(owlClass.getIRI().toString())) {
                owlEntitySet.add(owlClass);
            }});
        activeOntology.getObjectPropertiesInSignature(Imports.INCLUDED).forEach(owlObjectProperty -> {
            if (lastUsedObjectPropertiesStringSignature.contains(owlObjectProperty.getIRI().toString())){
                owlEntitySet.add(owlObjectProperty);
            }});
        activeOntology.getIndividualsInSignature(Imports.INCLUDED).forEach(owlIndividual -> {
            if (lastUsedIndividualsStringSignature.contains(owlIndividual.getIRI().toString())){
                owlEntitySet.add(owlIndividual);
            }});
        return owlEntitySet;
    }

    private List<String> loadSignatureStringList(String ontologyName){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        return preferences.getStringList(ontologyName, Collections.emptyList());
    }

    public void saveKnownSignature(String ontologyName, Collection<OWLEntity> newSignature){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        ArrayList<String> classes = new ArrayList<>();
        ArrayList<String> objectProperties = new ArrayList<>();
        ArrayList<String> individuals = new ArrayList<>();
        newSignature.forEach(entity -> {
            if (entity.isOWLClass()){
                classes.add(entity.getIRI().toString());
            }
            else if (entity.isOWLObjectProperty()){
                objectProperties.add(entity.getIRI().toString());
            }
            else if (entity.isOWLNamedIndividual()){
                individuals.add(entity.getIRI().toString());
            }});
        preferences.putStringList(ontologyName + this.CLASSES_SUFFIX, classes);
        preferences.putStringList(ontologyName + this.OBJECT_PROPERTIES_SUFFIX, objectProperties);
        preferences.putStringList(ontologyName + this.INDIVIDUALS_SUFFIX, individuals);
        timeStamp = System.currentTimeMillis();
    }

    private String escapeOntologyName(String ontologyName){
        return ontologyName.replace("//", "/*/*");
    }

    public boolean signatureChanged(long otherTimeStamp, String ontologyName){
        if (timeStamp != otherTimeStamp){
            return ! this.lastUsedClassesStringSignature.equals(this.loadSignatureStringList(ontologyName));
        }
        return false;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

}
