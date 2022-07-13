package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeDoubleProofPreference;
import org.protege.editor.core.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedEliminationProofPreferencesManager extends AbstractEveeEliminationProofPreferencesManager {

    protected static final String SET_ID = "EVEE_PROOF_SERVICE_LETHE_ELIMINATION";
    protected static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_LETHE_ELIMINATION";
    protected static final String HEURISTIC = "Elimination Proof (LETHE)";
    protected static final String SIZE_MINIMAL = "Elimination Proof, optimized for size (LETHE)";
    protected static final String SYMBOL_MINIMAL = "Elimination Proof, optimized for eliminated names (LETHE)";
    protected static final String WEIGHTED_SIZE_MINIMAL = "Elimination Proof, optimized for weighted size (LETHE)";
    public final String TIME_OUT = "timeOut";
    protected final double TIME_OUT_DEFAULT_VALUE = 2d;
    protected final String TIME_OUT_UNIT = "Seconds";
    protected final String TIME_OUT_LABEL = "Forgetting timeout:";
    protected final String TIME_OUT_TOOL_TIP = "Sets the timeout for each elimination step (in seconds)";
    private EveeDoubleProofPreference timeOutDefaultPreference;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedEliminationProofPreferencesManager.class);

    public EveeLetheBasedEliminationProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
        this.initializeTimeOutDefaultPreference();
        this.correctActivationDefaultPreferences();
    }

    public EveeLetheBasedEliminationProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeEliminationProofPreferencesManager.PREFERENCE_UI);
        this.initializeTimeOutDefaultPreference();
        this.correctActivationDefaultPreferences();
    }

    private void initializeTimeOutDefaultPreference(){
        this.timeOutDefaultPreference = new EveeDoubleProofPreference(
                this.TIME_OUT_DEFAULT_VALUE, this.TIME_OUT_LABEL, this.TIME_OUT_TOOL_TIP);
    }

    @Override
    protected void createIdentifierSet() {
        this.proofServiceNameSet.add(HEURISTIC);
        this.proofServiceNameSet.add(SIZE_MINIMAL);
        this.proofServiceNameSet.add(SYMBOL_MINIMAL);
        this.proofServiceNameSet.add(WEIGHTED_SIZE_MINIMAL);
    }

    protected void correctActivationDefaultPreferences(){
        this.activationDefaultPreferences.get(SIZE_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(SYMBOL_MINIMAL).setBooleanDefaultValue(false);
        this.activationDefaultPreferences.get(WEIGHTED_SIZE_MINIMAL).setBooleanDefaultValue(false);
    }

    protected double loadTimeOut(){
        Preferences preferences = this.getProtegePreferences();
        return preferences.getDouble(TIME_OUT, this.timeOutDefaultPreference.getDefaultDoubleValue());
    }

    protected void saveTimeOut(double newValue){
        Preferences preferences = this.getProtegePreferences();
        preferences.putDouble(TIME_OUT, newValue);
        this.logger.debug("Preference timeOut saved: " + newValue);
    }

    protected String getTimeOutUILabel(){
        return this.timeOutDefaultPreference.getUiLabel();
    }

    protected String getTimeOutUIToolTip(){
        return this.timeOutDefaultPreference.getUiToolTip();
    }

}
