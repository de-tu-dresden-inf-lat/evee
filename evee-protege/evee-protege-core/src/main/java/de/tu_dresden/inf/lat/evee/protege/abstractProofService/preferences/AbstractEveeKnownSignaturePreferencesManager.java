package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

abstract public class AbstractEveeKnownSignaturePreferencesManager {

    private static final String SET_ID = "EVEE_GENERAL_PREFERENCES";
    private final String PREFERENCE_ID = "EVEE_KNOWN_SIGNATURE";
    protected final String CLASSES_SUFFIX = "_CLASSES";
    protected final String OBJECT_PROPERTIES_SUFFIX = "OBJECT_PROPERTIES";
    protected final String INDIVIDUALS_SUFFIX = "_INDIVIDUALS";
    protected final String USE_SIGNATURE_SUFFIX = "_USE_SIGNATURE";
    protected static long timeStamp;
    private static boolean initialised = false;
    protected List<String> lastUsedClassesStringSignature;
    protected List<String> lastUsedObjectPropertiesStringSignature;
    protected List<String> lastUsedIndividualsStringSignature;
    protected Boolean lastUseSignature = Boolean.FALSE;
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeKnownSignaturePreferencesManager.class);

    public AbstractEveeKnownSignaturePreferencesManager(){
        this.lastUsedClassesStringSignature = new ArrayList<>();
        this.lastUsedObjectPropertiesStringSignature = new ArrayList<>();
        this.lastUsedIndividualsStringSignature = new ArrayList<>();
        if (! initialised){
            timeStamp =  System.currentTimeMillis();
            initialised = true;
        }
    }

    protected Set<OWLEntity> loadKnownSignature(OWLOntology activeOntology, String ontologyName) {
        ontologyName = this.escapeOntologyName(ontologyName);
        this.lastUsedClassesStringSignature = this.loadClassSignatureStringList(activeOntology, ontologyName);
        this.lastUsedObjectPropertiesStringSignature = this.loadObjectPropertiesSignatureStringList(ontologyName);
        this.lastUsedIndividualsStringSignature = this.loadIndividualsSignatureStringList(ontologyName);
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

    protected List<String> loadClassSignatureStringList(OWLOntology activeOntology, String ontologyName){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        OWLDataFactory dataFactory = activeOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass top = dataFactory.getOWLThing();
        OWLClass bot = dataFactory.getOWLNothing();
        ArrayList<String> defaultList = new ArrayList<>();
        defaultList.add(top.getIRI().toString());
        defaultList.add(bot.getIRI().toString());
        return preferences.getStringList(ontologyName + this.CLASSES_SUFFIX, defaultList);
    }

    protected List<String> loadObjectPropertiesSignatureStringList(String ontologyName){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        return preferences.getStringList(ontologyName + this.OBJECT_PROPERTIES_SUFFIX, Collections.emptyList());
    }

    protected List<String> loadIndividualsSignatureStringList(String ontologyName){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        return preferences.getStringList(ontologyName + this.INDIVIDUALS_SUFFIX, Collections.emptyList());
    }

    private List<String> loadSignatureStringList(String ontologyName){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        return preferences.getStringList(ontologyName, Collections.emptyList());
    }

    public void saveKnownSignature(String ontologyName, Collection<OWLEntity> newSignature){
        this.logger.debug("Saving signature:");
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        ArrayList<String> classes = new ArrayList<>();
        ArrayList<String> objectProperties = new ArrayList<>();
        ArrayList<String> individuals = new ArrayList<>();
        newSignature.forEach(entity -> {
            logger.debug("saving entity: " + entity);
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

    public boolean loadUseSignature(String ontologyName){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        this.lastUseSignature = preferences.getBoolean(ontologyName + this.USE_SIGNATURE_SUFFIX, true);
        return this.lastUseSignature;
    }

    public void saveUseSignature(String ontologyName, boolean newValue){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        preferences.putBoolean(ontologyName + this.USE_SIGNATURE_SUFFIX, newValue);
        timeStamp = System.currentTimeMillis();
    }

    private String escapeOntologyName(String ontologyName){
        return ontologyName.replace("//", "/*/*");
    }

    public long getTimeStamp(){
        return timeStamp;
    }

}
