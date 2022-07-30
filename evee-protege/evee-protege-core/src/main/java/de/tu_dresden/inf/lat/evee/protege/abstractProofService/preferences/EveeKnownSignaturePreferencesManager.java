package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

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
    private static long timeStamp;
    private static boolean initialised = false;
    private List<String> lastUsedStringSignature;
    private final Logger logger = LoggerFactory.getLogger(EveeKnownSignaturePreferencesManager.class);

    public EveeKnownSignaturePreferencesManager(){
        this.lastUsedStringSignature = new ArrayList<>();
        if (! initialised){
            timeStamp =  System.currentTimeMillis();
        }
    }

    public Set<OWLEntity> loadKnownSignature(OWLOntology activeOntology, String ontologyName) {
        this.lastUsedStringSignature = this.loadSignatureStringList(ontologyName);
        Set<OWLEntity> owlEntitySet = new HashSet<>();
        this.lastUsedStringSignature.forEach(iriString -> {
            owlEntitySet.addAll(
                    activeOntology.getEntitiesInSignature(
                            IRI.create(iriString), Imports.INCLUDED));
        });
        return owlEntitySet;
    }

    private List<String> loadSignatureStringList(String ontologyName){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        return preferences.getStringList(ontologyName, Collections.emptyList());
    }

    public void saveKnownSignature(String ontologyName, List<String> newSignature){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        preferences.putStringList(ontologyName, newSignature);
        timeStamp = System.currentTimeMillis();
    }

    private String escapeOntologyName(String ontologyName){
        return ontologyName.replace("//", "/*/*");
    }

    public boolean signatureChanged(long otherTimeStamp, String ontologyName){
        if (timeStamp != otherTimeStamp){
            return ! this.lastUsedStringSignature.equals(this.loadSignatureStringList(ontologyName));
        }
        return false;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

}
