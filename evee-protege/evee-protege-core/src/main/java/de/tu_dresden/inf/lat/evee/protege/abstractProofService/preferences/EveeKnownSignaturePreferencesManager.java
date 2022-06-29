package de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class EveeKnownSignaturePreferencesManager {

    private static final String SET_ID = "EVEE_GENERAL_PREFERENCES";
    private static final String PREFERENCE_ID = "EVEE_KNOWN_ONTOLOGY_SIGNATURE";
    private final String knownOntology;
    private final Logger logger = LoggerFactory.getLogger(EveeKnownSignaturePreferencesManager.class);

    public EveeKnownSignaturePreferencesManager(String knownOntology){
        this.knownOntology = knownOntology.replace("//", "/*/*");
//        this.knownOntology = knownOntology.substring(knownOntology.indexOf("//") +2, knownOntology.length() -1 );
    }

    public List<String> loadKnownSignature(){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        return preferences.getStringList(this.knownOntology, Collections.emptyList());
    }

    public void saveKnownSignature(List<String> newSignature){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(SET_ID, PREFERENCE_ID);
        preferences.putStringList(this.knownOntology, newSignature);
    }

}
