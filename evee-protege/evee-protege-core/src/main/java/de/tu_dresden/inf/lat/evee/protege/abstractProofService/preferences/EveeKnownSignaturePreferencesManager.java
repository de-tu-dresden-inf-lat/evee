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
    private static long lastSaved;
    private List<String> lastUsedStringSignature;
    private final Set<OWLEntity> lastUsedOWLEntitySignature;
    private final Logger logger = LoggerFactory.getLogger(EveeKnownSignaturePreferencesManager.class);

    public EveeKnownSignaturePreferencesManager(){
        this.lastUsedStringSignature = new ArrayList<>();
        this.lastUsedOWLEntitySignature = new HashSet<>();
        lastSaved =  System.currentTimeMillis();
    }

    public Set<OWLEntity> loadKnownSignature(OWLOntology activeOntology, String ontologyName) {
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        List<String> currentStringSignature = preferences.getStringList(ontologyName, Collections.emptyList());
        if (! this.lastUsedStringSignature.equals(currentStringSignature)){
            this.lastUsedStringSignature = currentStringSignature;
            this.lastUsedOWLEntitySignature.clear();
            this.lastUsedStringSignature.forEach(iriString -> {
                this.lastUsedOWLEntitySignature.addAll(
                        activeOntology.getEntitiesInSignature(
                                IRI.create(iriString), Imports.INCLUDED));
            });
        }
        return this.lastUsedOWLEntitySignature;
    }

    public void saveKnownSignature(String ontologyName, List<String> newSignature){
        ontologyName = this.escapeOntologyName(ontologyName);
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, preferenceId);
        preferences.putStringList(ontologyName, newSignature);
        lastSaved = System.currentTimeMillis();
    }

    private String escapeOntologyName(String ontologyName){
        return ontologyName.replace("//", "/*/*");
    }

    public boolean signatureChanged(long lastUsed){
        return lastUsed < lastSaved;
    }

}
