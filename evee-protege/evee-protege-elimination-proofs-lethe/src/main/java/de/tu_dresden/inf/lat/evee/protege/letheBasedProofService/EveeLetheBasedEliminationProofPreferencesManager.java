package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeDoubleProofPreference;
import org.protege.editor.core.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.generic.Sorted;

import java.util.SortedSet;
import java.util.TreeSet;

public class EveeLetheBasedEliminationProofPreferencesManager extends AbstractEveeEliminationProofPreferencesManager {

    protected static final String SET_ID = "EVEE_PROOF_SERVICE_LETHE_ELIMINATION";
    protected static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_ELIMINATION";
    protected static final String PROOF_SERVICE_ID1 = "EveeLetheElimination1";
    protected static final String PROOF_SERVICE_NAME1 = "Elimination Proof (LETHE)";
    protected static final String PROOF_SERVICE_ID2 = "EveeLetheElimination2";
    protected static final String PROOF_SERVICE_NAME2 = "Elimination Proof, optimized for size (LETHE)";
    protected static final String PROOF_SERVICE_ID3 = "EveeLetheElimination3";
    protected static final String PROOF_SERVICE_NAME3 = "Elimination Proof, optimized for eliminated names (LETHE)";
    protected static final String PROOF_SERVICE_ID4 = "EveeLetheElimination4";
    protected static final String PROOF_SERVICE_NAME4 = "Elimination Proof, optimized for weighted size (LETHE)";
    public final String TIME_OUT = "timeOut";
    protected final String TIME_OUT_UNIT = "Seconds";
    protected final String TIME_OUT_LABEL = "Forgetting timeout:";
    protected final String TIME_OUT_TOOL_TIP = "Sets the timeout for each elimination step (in seconds)";
    private final double TIME_OUT_DEFAULT_VALUE = 2d;
    private static long timeOutTimeStamp;
    private EveeDoubleProofPreference timeOutDefaultPreference;
    private double timeOutLastUsedValue;
    private static boolean initialised = false;
    private final String proofServiceID;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedEliminationProofPreferencesManager.class);

    public EveeLetheBasedEliminationProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
        this.proofServiceID = identifier;
        this.initializeTimeOutDefaultPreference();
    }

    public EveeLetheBasedEliminationProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeEliminationProofPreferencesManager.PREFERENCE_UI);
        this.proofServiceID = AbstractEveeProofPreferencesManager.PREFERENCE_UI;
        this.initializeTimeOutDefaultPreference();
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

    private void initializeTimeOutDefaultPreference(){
        this.timeOutDefaultPreference = new EveeDoubleProofPreference(
                this.TIME_OUT_DEFAULT_VALUE, this.TIME_OUT_LABEL, this.TIME_OUT_TOOL_TIP);
        this.timeOutLastUsedValue = TIME_OUT_DEFAULT_VALUE;
        if (! initialised){
            timeOutTimeStamp = System.currentTimeMillis();
            initialised = true;
        }
    }

    public double loadTimeOutSeconds(){
        this.timeOutLastUsedValue = this.internalLoadTimeOut();
        return timeOutLastUsedValue;
    }

    public boolean timeOutChanged(long otherTimeStamp){
        if (timeOutTimeStamp != otherTimeStamp){
            return ! (this.timeOutLastUsedValue == this.internalLoadTimeOut());
        }
        return false;
    }

    private double internalLoadTimeOut(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getDouble(TIME_OUT,
                this.timeOutDefaultPreference.getDefaultDoubleValue());
    }

    public long getTimeOutTimeStamp(){
        return timeOutTimeStamp;
    }

    protected void saveTimeOut(double newValue){
        Preferences preferences = this.getProtegePreferences();
        preferences.putDouble(TIME_OUT, newValue);
        this.logger.debug("Preference timeOut saved: " + newValue);
        timeOutTimeStamp = System.currentTimeMillis();
    }

    protected String getTimeOutUILabel(){
        return this.timeOutDefaultPreference.getUiLabel();
    }

    protected String getTimeOutUIToolTip(){
        return this.timeOutDefaultPreference.getUiToolTip();
    }

}
