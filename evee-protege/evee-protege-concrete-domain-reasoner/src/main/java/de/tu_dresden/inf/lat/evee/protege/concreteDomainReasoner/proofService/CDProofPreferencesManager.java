package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.ConstraintPropagationReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationProver;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The preferences of the proof services for concrete domains: which services are active, and up to how many variables
 * the constraint proofs contain all alternatives.
 */
public class CDProofPreferencesManager extends AbstractEveeProofPreferencesManager {

    private static final String SET_ID = "EVEE_PROOF_CONCRETE_DOMAIN";
    private static final String PREFERENCE_ID = "EVEE_PREFERENCES_MANAGER_CONCRETE_DOMAIN";
    protected static final String DEPTH_ID = "EveeConcreteDomain1";
    protected static final String DEPTH_NAME = "PACER Proof, optimized for depth";
    protected static final String SIZE_ID = "EveeConcreteDomain2";
    protected static final String SIZE_NAME = "PACER Proof, optimized for size";
    protected static final String WEIGHTED_SIZE_ID = "EveeConcreteDomain3";
    protected static final String WEIGHTED_SIZE_NAME = "PACER Proof, optimized for weighted size";
    private static final String MAX_VARIABLES_FOR_ALL_ORDERS = "MAX_VARIABLES_FOR_ALL_ORDERS";
    private static final String MAX_VARIABLES_FOR_ALL_PATHS = "MAX_VARIABLES_FOR_ALL_PATHS";

    public CDProofPreferencesManager(String identifier) {
        super(SET_ID, PREFERENCE_ID, identifier);
    }

    public CDProofPreferencesManager() {
        super(SET_ID, PREFERENCE_ID, AbstractEveeProofPreferencesManager.PREFERENCE_UI);
    }

    @Override
    public SortedSet<String> getProofServiceIDs() {
        SortedSet<String> ids = new TreeSet<>();
        ids.add(DEPTH_ID);
        ids.add(SIZE_ID);
        ids.add(WEIGHTED_SIZE_ID);
        return ids;
    }

    @Override
    protected String parseProofServiceID2Name(String proofServiceID) {
        switch (proofServiceID) {
            case DEPTH_ID:
                return DEPTH_NAME;
            case SIZE_ID:
                return SIZE_NAME;
            case WEIGHTED_SIZE_ID:
                return WEIGHTED_SIZE_NAME;
            default:
                return null;
        }
    }

    @Override
    public String getIsActiveUILabel(String key) {
        return this.parseProofServiceID2Name(key);
    }

    /** for QLinear, see {@link GaussianEliminationProver#setMaxVariablesForAllOrders(int)} */
    public int loadMaxVariablesForAllOrders() {
        return this.getProtegePreferences().getInt(MAX_VARIABLES_FOR_ALL_ORDERS,
                GaussianEliminationProver.DEFAULT_MAX_VARIABLES_FOR_ALL_ORDERS);
    }

    public void saveMaxVariablesForAllOrders(int value) {
        this.getProtegePreferences().putInt(MAX_VARIABLES_FOR_ALL_ORDERS, value);
    }

    /** for QDiff and QMult, see {@link ConstraintPropagationReasoner#setMaxVariablesForAllPaths(int)} */
    public int loadMaxVariablesForAllPaths() {
        return this.getProtegePreferences().getInt(MAX_VARIABLES_FOR_ALL_PATHS,
                ConstraintPropagationReasoner.DEFAULT_MAX_VARIABLES_FOR_ALL_PATHS());
    }

    public void saveMaxVariablesForAllPaths(int value) {
        this.getProtegePreferences().putInt(MAX_VARIABLES_FOR_ALL_PATHS, value);
    }
}
