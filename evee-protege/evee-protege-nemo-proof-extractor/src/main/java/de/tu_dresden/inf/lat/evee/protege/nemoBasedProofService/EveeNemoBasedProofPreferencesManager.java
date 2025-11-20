package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import org.protege.editor.core.prefs.Preferences;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

import java.util.SortedSet;
import java.util.TreeSet;

public class EveeNemoBasedProofPreferencesManager extends AbstractEveeProofPreferencesManager{

    private static final String SET_ID = "EVEE_PROOF_NEMO";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_NEMO";
    private static final String NEMO_PATH = "NEMO_PATH";

    protected static final String ELK_NAME =  "ELK calculus proof (Nemo)";
    protected static final String TEXTBOOK_NAME =  "TEXTBOOK calculus proof (Nemo)";
    protected static final String ENVELOPE_NAME =  "ENVELOPE calculus proof (Nemo)";
    protected static final String ELK_ID = "EveeNemoElk";
    protected static final String TEXTBOOK_ID = "EveeNemoTextbook";
    protected static final String ENVELOPE_ID = "EveeNemoEnvelope";

    public EveeNemoBasedProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeNemoBasedProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    public String loadNemoPath() {
        Preferences preferences = getProtegePreferences();
        return preferences.getString(NEMO_PATH, "");
    }

    public void saveNemoPath(String newPath){
        Preferences preferences = getProtegePreferences();
        preferences.putString(NEMO_PATH, newPath);
    }

    @Override
    public SortedSet<String> getProofServiceIDs() {
        SortedSet<String> proofServiceIDSet = new TreeSet<>();
        proofServiceIDSet.add(ELK_ID);
        proofServiceIDSet.add(TEXTBOOK_ID);
        proofServiceIDSet.add(ENVELOPE_ID);
        return proofServiceIDSet;
    }

    @Override
    protected String parseProofServiceID2Name(String proofServiceID) {
        switch (proofServiceID){
            case ELK_ID:
                return ELK_NAME;
            case TEXTBOOK_ID:
                return TEXTBOOK_NAME;
            case ENVELOPE_ID:
                return ENVELOPE_NAME;
            default:
                return null;
        }
    }

    @Override
    public String getIsActiveUILabel(String key) {
        return this.parseProofServiceID2Name(key);
    }
}

