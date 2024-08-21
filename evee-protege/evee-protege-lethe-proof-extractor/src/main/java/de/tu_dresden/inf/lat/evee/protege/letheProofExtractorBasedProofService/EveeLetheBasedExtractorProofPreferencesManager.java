package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

import java.util.SortedSet;
import java.util.TreeSet;

public class EveeLetheBasedExtractorProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_LETHE_DETAILED";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_DETAILED";
    protected static final String PROOF_SERVICE_ID = "LetheDetailed";
    protected static final String PROOF_SERVICE_NAME = "Detailed Proof (LETHE)";

    public EveeLetheBasedExtractorProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public EveeLetheBasedExtractorProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    public SortedSet<String> getProofServiceIDs(){
        SortedSet<String> proofServicesIDSet = new TreeSet<>();
        proofServicesIDSet.add(PROOF_SERVICE_ID);
        return proofServicesIDSet;
    }

    @Override
    protected String parseProofServiceID2Name(String proofServiceID) {
        if (proofServiceID.equals(PROOF_SERVICE_ID)){
            return PROOF_SERVICE_NAME;
        } else {
            return null;
        }
    }

    @Override
    public String getIsActiveUILabel(String key) {
        return this.parseProofServiceID2Name(key);
    }

}
