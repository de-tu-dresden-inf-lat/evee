package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import org.protege.editor.core.prefs.Preferences;

import java.util.SortedSet;
import java.util.TreeSet;

public class EveeFameBasedEliminationProofPreferencesManager extends AbstractEveeEliminationProofPreferencesManager {

    protected static final String SET_ID = "EVEE_PROOF_SERVICE_FAME_ELIMINATION";
    protected static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_FAME_ELIMINATION";
    protected static final String PROOF_SERVICE_ID1 = "EveeFameElimination1";
    protected static final String PROOF_SERVICE_NAME1 = "Elimination Proof (FAME)";
    protected static final String PROOF_SERVICE_ID2 = "EveeFameElimination2";
    protected static final String PROOF_SERVICE_NAME2 = "Elimination Proof, optimized for size (FAME)";
    protected static final String PROOF_SERVICE_ID3 = "EveeFameElimination3";
    protected static final String PROOF_SERVICE_NAME3 = "Elimination Proof, optimized for eliminated names (FAME)";
    protected static final String PROOF_SERVICE_ID4 = "EveeFameElimination4";
    protected static final String PROOF_SERVICE_NAME4 = "Elimination Proof, optimized for weighted size (FAME)";
    private final String proofServiceID;

    public EveeFameBasedEliminationProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
        this.proofServiceID = identifier;
    }

    public EveeFameBasedEliminationProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeEliminationProofPreferencesManager.PREFERENCE_UI);
        this.proofServiceID = AbstractEveeProofPreferencesManager.PREFERENCE_UI;
    }

    @Override
    public SortedSet<String> getProofServiceIDs() {
        SortedSet<String> proofServiceIDSet = new TreeSet<>();
        proofServiceIDSet.add(PROOF_SERVICE_ID1);
        proofServiceIDSet.add(PROOF_SERVICE_ID2);
        proofServiceIDSet.add(PROOF_SERVICE_ID3);
        proofServiceIDSet.add(PROOF_SERVICE_ID4);
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
            case PROOF_SERVICE_ID4:
                return PROOF_SERVICE_NAME4;
            default:
                return null;
        }
    }

    @Override
    public boolean loadIsActive(){
        Preferences preferences = this.getProtegePreferences();
        assert this.getProofServiceIDs().contains(this.proofServiceID);
        boolean defaultValue = this.proofServiceID.equals(PROOF_SERVICE_ID1);
        return preferences.getBoolean(this.proofServiceID, defaultValue);
    }

    @Override
    public boolean loadIsActive(String key){
        Preferences preferences = this.getProtegePreferences();
        assert this.getProofServiceIDs().contains(key);
        boolean defaultValue = key.equals(PROOF_SERVICE_ID1);
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public String getIsActiveUILabel(String key) {
        return this.parseProofServiceID2Name(key);
    }

}
