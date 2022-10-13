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

abstract public class AbstractEveeSignaturePreferencesManager {

//        NOTE: Protege internally saves Preferences via the Java class "Preferences"
//              For this, any key used to save preferences may not include a double slash, i.e. //.
//              Furthermore, the length of a key for these preferences is finite.
//              As we use ontology-IRIs as keys, we map these IRIs to integers before saving them to the Protege preferneces.
//              This avoids replacing any // in the IRI and
//              enables us to save preferences for ontologies with arbitrarily long IRIs.
//              The replacement is done via the LinkedHashMap "knownOntologies", which is created during initialisation
//              and updated if the known signature is saved for a new ontology.

    private static final String SET_ID = "EVEE_GENERAL_PREFERENCES";
    private final String PREFERENCE_ID = "EVEE_KNOWN_SIGNATURE";
    private final String KNOWN_ONTOLOGIES = "EVEE_KNOWN_ONTOLOGIES";
    protected final String CLASSES_SUFFIX = "_CLASSES";
    protected final String OBJECT_PROPERTIES_SUFFIX = "_OBJECT_PROPERTIES";
    protected final String INDIVIDUALS_SUFFIX = "_INDIVIDUALS";
    protected final String USE_SIGNATURE_SUFFIX = "_USE_SIGNATURE";
    private static Map<String, Integer> knownOntologies = null;
    protected static long timeStamp;
    private static boolean initialised = false;
    protected List<String> lastUsedClassesStringSignature;
    protected List<String> lastUsedObjectPropertiesStringSignature;
    protected List<String> lastUsedIndividualsStringSignature;
    protected Boolean lastUseSignature = Boolean.FALSE;
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeSignaturePreferencesManager.class);

    public AbstractEveeSignaturePreferencesManager(){
        this.lastUsedClassesStringSignature = new ArrayList<>();
        this.lastUsedObjectPropertiesStringSignature = new ArrayList<>();
        this.lastUsedIndividualsStringSignature = new ArrayList<>();
        if (! initialised){
            this.initialise();
        }
    }

    protected void initialise(){
        timeStamp =  System.currentTimeMillis();
        knownOntologies = new LinkedHashMap<>();
        Preferences preferences = this.loadPreferences();
        List<String> knownOntologiesStringList = preferences.getStringList(KNOWN_ONTOLOGIES, Collections.emptyList());
        int idx = 0;
        for (String IRI : knownOntologiesStringList){
            knownOntologies.put(IRI, idx);
            idx += 1;
        }
        initialised = true;
    }

    protected Set<OWLEntity> loadKnownSignature(OWLOntology activeOntology, String ontologyName) {
        Integer ontoID = knownOntologies.get(ontologyName);
        if (ontoID == null){
            this.lastUsedClassesStringSignature = new ArrayList<>();
            this.lastUsedObjectPropertiesStringSignature = new ArrayList<>();
            this.lastUsedIndividualsStringSignature = new ArrayList<>();
            return Collections.emptySet();
        }
        this.lastUsedClassesStringSignature = this.loadEntitiesSignatureStringList(ontoID + this.CLASSES_SUFFIX);
        this.lastUsedObjectPropertiesStringSignature = this.loadEntitiesSignatureStringList(ontoID + this.OBJECT_PROPERTIES_SUFFIX);
        this.lastUsedIndividualsStringSignature = this.loadEntitiesSignatureStringList(ontoID + this.INDIVIDUALS_SUFFIX);
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
        OWLDataFactory df = activeOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass top = df.getOWLThing();
        OWLClass bot = df.getOWLNothing();
        if (lastUsedClassesStringSignature.contains("http://www.w3.org/2002/07/owl#Nothing")){
            owlEntitySet.add(bot);
        }
        if (lastUsedClassesStringSignature.contains("http://www.w3.org/2002/07/owl#Thing")){
            owlEntitySet.add(top);
        }
        return owlEntitySet;
    }

//    protected List<String> loadClassSignatureStringList(String ontologyName){
//        Preferences preferences = this.loadPreferences();
//        return preferences.getStringList(ontologyName + this.CLASSES_SUFFIX, Collections.emptyList());
//    }
//
//    protected List<String> loadObjectPropertiesSignatureStringList(String ontologyName){
//        Preferences preferences = this.loadPreferences();
//        return preferences.getStringList(ontologyName + this.OBJECT_PROPERTIES_SUFFIX, Collections.emptyList());
//    }
//
//    protected List<String> loadIndividualsSignatureStringList(String ontologyName){
//        Preferences preferences = this.loadPreferences();
//        return preferences.getStringList(ontologyName + this.INDIVIDUALS_SUFFIX, Collections.emptyList());
//    }

    protected List<String> loadEntitiesSignatureStringList(String key){
        Preferences preferences = this.loadPreferences();
        return preferences.getStringList(key, Collections.emptyList());
    }

//    private List<String> loadSignatureStringList(String ontologyName){
//        Preferences preferences = this.loadPreferences();
//        return preferences.getStringList(ontologyName, Collections.emptyList());
//    }

//    public void saveKnownSignature(String ontologyName, Collection<OWLEntity> newSignature){
//        this.logger.debug("Saving signature:");
//        ontologyName = this.escapeOntologyName(ontologyName);
//        Preferences preferences = this.loadPreferences();
//        ArrayList<String> classes = new ArrayList<>();
//        ArrayList<String> objectProperties = new ArrayList<>();
//        ArrayList<String> individuals = new ArrayList<>();
//        newSignature.forEach(entity -> {
//            logger.debug("saving entity: " + entity);
//            if (entity.isOWLClass()){
//                classes.add(entity.getIRI().toString());
//            }
//            else if (entity.isOWLObjectProperty()){
//                objectProperties.add(entity.getIRI().toString());
//            }
//            else if (entity.isOWLNamedIndividual()){
//                individuals.add(entity.getIRI().toString());
//            }});
//        preferences.putStringList(ontologyName + this.CLASSES_SUFFIX, classes);
//        preferences.putStringList(ontologyName + this.OBJECT_PROPERTIES_SUFFIX, objectProperties);
//        preferences.putStringList(ontologyName + this.INDIVIDUALS_SUFFIX, individuals);
//        timeStamp = System.currentTimeMillis();
//    }

    protected boolean loadUseSignature(String ontologyName){
        Integer ontologyID = knownOntologies.get(ontologyName);
        if (ontologyID == null){
            this.lastUseSignature = false;
        }
        else {
            Preferences preferences = this.loadPreferences();
            this.lastUseSignature = preferences.getBoolean(ontologyID + this.USE_SIGNATURE_SUFFIX, true);
        }
        return this.lastUseSignature;
    }

//    public void saveUseSignature(String ontologyName, boolean newValue){
//        ontologyName = this.escapeOntologyName(ontologyName);
//        Preferences preferences = this.loadPreferences();
//        preferences.putBoolean(ontologyName + this.USE_SIGNATURE_SUFFIX, newValue);
//        timeStamp = System.currentTimeMillis();
//    }

//    private String escapeOntologyName(String ontologyName){
//        return ontologyName.replace("//", "/*/*");
//    }

    public long getTimeStamp(){
        return timeStamp;
    }

    protected Preferences loadPreferences(){
        return PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
    }

    public void saveSignature(String ontologyName, boolean useSignature, Collection<OWLEntity> newSignature){
        Preferences preferences = this.loadPreferences();
//        NOTE: for further information on why this implementation was chosen, please see top of this class
        if (knownOntologies.get(ontologyName) == null){
            knownOntologies.put(ontologyName, knownOntologies.keySet().size());
            ArrayList<String> ontologyList = new ArrayList<>(knownOntologies.keySet());
            preferences.putStringList(KNOWN_ONTOLOGIES, ontologyList);
        }
        Integer ontoID = knownOntologies.get(ontologyName);
        preferences.putBoolean(ontoID + this.USE_SIGNATURE_SUFFIX, useSignature);
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
        preferences.putStringList(ontoID + this.CLASSES_SUFFIX, classes);
        preferences.putStringList(ontoID + this.OBJECT_PROPERTIES_SUFFIX, objectProperties);
        preferences.putStringList(ontoID + this.INDIVIDUALS_SUFFIX, individuals);
        timeStamp = System.currentTimeMillis();
    }

    protected Integer getOntologyID(String ontologyName){
        return knownOntologies.get(ontologyName);
    }

    public boolean isNewOntology(String ontologyName){
        return knownOntologies.get(ontologyName) == null;
    }

}
