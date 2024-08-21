package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import org.protege.editor.core.prefs.Preferences;

import java.util.SortedSet;
import java.util.TreeSet;

public class EveeElkBasedExtractorProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_ELK";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_ELK";
    protected static final String PROOF_SERVICE_ID1 = "EveeElkExtractor1";
    protected static final String PROOF_SERVICE_NAME1 = "ELK Proof, optimized for depth";
    protected static final String PROOF_SERVICE_ID2 = "EveeElkExtractor2";
    protected static final String PROOF_SERVICE_NAME2 = "ELK Proof, optimized for size";
    protected static final String PROOF_SERVICE_ID3 = "EveeElkExtractor3";
    protected static final String PROOF_SERVICE_NAME3 = "ELK Proof, optimized for weighted size";

    public EveeElkBasedExtractorProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeElkBasedExtractorProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    public SortedSet<String> getProofServiceIDs() {
        SortedSet<String> proofServiceIDSet = new TreeSet<>();
        proofServiceIDSet.add(PROOF_SERVICE_ID1);
        proofServiceIDSet.add(PROOF_SERVICE_ID2);
        proofServiceIDSet.add(PROOF_SERVICE_ID3);
        return proofServiceIDSet;
    }

    @Override
    protected String parseProofServiceID2Name(String proofServiceID) {
        switch (proofServiceID){
            case PROOF_SERVICE_ID1:
                return PROOF_SERVICE_NAME1;
            case PROOF_SERVICE_ID2:
                return PROOF_SERVICE_NAME2;
            case PROOF_SERVICE_ID3:
                return PROOF_SERVICE_NAME3;
            default:
                return null;
        }
    }

    @Override
    public String getIsActiveUILabel(String key) {
        return this.parseProofServiceID2Name(key);
    }
}
