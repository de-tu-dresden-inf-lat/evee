package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner.proofService;

import de.tu_dresden.inf.lat.evee.concreteDomains.ConstraintPropagationReasoner;
import de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain.GaussianEliminationProver;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;

import javax.swing.*;
import java.awt.*;

/**
 * The preferences page of the proof services for concrete domains: which services are active, and up to how many
 * variables the constraint proofs contain all alternatives, from which minimisation chooses.
 */
public class CDProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    private static final String ALL_ORDERS_LABEL = "All elimination orders (QLinear) up to";
    private static final String ALL_ORDERS_TOOL_TIP = "Proofs of linear constraints with at most this many variables "
            + "contain the Gaussian elimination in every order of the variables (n! orders for n variables); "
            + "with more variables, only one order is used";
    private static final String ALL_PATHS_LABEL = "All derivation paths (QDiff, QMult) up to";
    private static final String ALL_PATHS_TOOL_TIP = "Proofs of difference and multiplication constraints with at "
            + "most this many variables contain the derivations along all paths between the variables, whose number "
            + "can grow exponentially; with more variables, fewer derivations are used";
    private static final String UNIT = "variables (default: %d)";
    private static final int SPINNER_WIDTH = 60;

    private final CDProofPreferencesManager preferencesManager;
    private JSpinner allOrdersSpinner;
    private JSpinner allPathsSpinner;

    public CDProofPreferencesUI() {
        super();
        this.preferencesManager = new CDProofPreferencesManager();
        this.setAbstractProofPreferencesManager(this.preferencesManager);
    }

    @Override
    protected void createUiElements() {
        super.createUiElements();
        SwingUtilities.invokeLater(() -> {
            this.allOrdersSpinner = spinner(this.preferencesManager.loadMaxVariablesForAllOrders(), ALL_ORDERS_TOOL_TIP);
            this.allPathsSpinner = spinner(this.preferencesManager.loadMaxVariablesForAllPaths(), ALL_PATHS_TOOL_TIP);
        });
    }

    private JSpinner spinner(int value, String toolTip) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0, null, 1));
        spinner.setPreferredSize(new Dimension(SPINNER_WIDTH, spinner.getPreferredSize().height));
        spinner.setToolTipText(toolTip);
        return spinner;
    }

    @Override
    protected void createAndFillHolderPanel() {
        super.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            this.holderPanel.addSeparator();
            this.holderPanel.addGroup("Alternatives for minimisation:");
            this.holderPanel.addGroupComponent(row(ALL_ORDERS_LABEL, this.allOrdersSpinner,
                    GaussianEliminationProver.DEFAULT_MAX_VARIABLES_FOR_ALL_ORDERS, ALL_ORDERS_TOOL_TIP));
            this.holderPanel.addGroupComponent(row(ALL_PATHS_LABEL, this.allPathsSpinner,
                    ConstraintPropagationReasoner.DEFAULT_MAX_VARIABLES_FOR_ALL_PATHS(), ALL_PATHS_TOOL_TIP));
        });
    }

    private JPanel row(String label, JSpinner spinner, int defaultValue, String toolTip) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setToolTipText(toolTip);
        panel.add(labelComponent);
        panel.add(spinner);
        panel.add(new JLabel(String.format(UNIT, defaultValue)));
        return panel;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            this.preferencesManager.saveMaxVariablesForAllOrders((int) this.allOrdersSpinner.getValue());
            this.preferencesManager.saveMaxVariablesForAllPaths((int) this.allPathsSpinner.getValue());
        });
        super.applyChanges();
    }
}
