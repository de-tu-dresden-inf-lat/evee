package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui.AbstractEveeEliminationProofPreferencesUI;

import javax.swing.*;
import java.awt.*;

public class EveeLetheBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    private JSpinner timeOutSpinner;
    private final int SPINNER_WIDTH = 100;

    public EveeLetheBasedEliminationPreferencesUI(){
        super(new EveeLetheBasedEliminationProofPreferencesManager());
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        SwingUtilities.invokeLater(() -> {
            EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager =
                    (EveeLetheBasedEliminationProofPreferencesManager) this.proofPreferencesManager;
            SpinnerNumberModel timeOutSpinnerNumberModel = new SpinnerNumberModel(proofPreferencesManager.loadTimeOut(),
//                    todo: min required for non-negative numbers, what should stepSize be?
                    0d, null, 0.001);
            this.timeOutSpinner = new JSpinner(timeOutSpinnerNumberModel);
            this.timeOutSpinner.setPreferredSize(new Dimension(this.SPINNER_WIDTH, this.timeOutSpinner.getPreferredSize().height));
            this.timeOutSpinner.setToolTipText(proofPreferencesManager.getTimeOutUIToolTip());
        });
    }

    @Override
    protected void createAndFillHolderPanel(){
        super.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            EveeLetheBasedEliminationProofPreferencesManager prefManager =
                    (EveeLetheBasedEliminationProofPreferencesManager) this.proofPreferencesManager;
            JPanel timeOutPanel = new JPanel();
            timeOutPanel.setLayout(new BoxLayout(timeOutPanel, BoxLayout.X_AXIS));
            this.miscellaneousPreferencesPanel.addGroupComponent(timeOutPanel);
            JLabel timeOutLabel = new JLabel(prefManager.getTimeOutUILabel());
            timeOutPanel.add(timeOutLabel);
            timeOutPanel.add(this.timeOutSpinner);
            JLabel timeOutUnit = new JLabel(prefManager.TIME_OUT_UNIT);
            timeOutPanel.add(timeOutUnit);
            timeOutPanel.setMaximumSize(new Dimension(
                    timeOutLabel.getWidth() + this.SPINNER_WIDTH + timeOutUnit.getWidth(),
                    this.timeOutSpinner.getHeight()));
            this.logger.debug("front label preferred size: " + timeOutLabel.getPreferredSize());
            this.logger.debug("spinner preferred size: " + this.timeOutSpinner.getPreferredSize());
            this.logger.debug("back label preferred size: " + timeOutUnit.getPreferredSize());
            this.logger.debug("timeoutPanel max size: " + timeOutPanel.getMaximumSize());
            this.logger.debug("timeoutPanel pref size: " + timeOutPanel.getPreferredSize());
            this.logger.debug("timeoutPanel width: " + timeOutPanel.getWidth());
            this.logger.debug("window width? " + this.getWidth());
            this.logger.debug("spinner minimum width" + this.timeOutSpinner.getMinimumSize());
        });
    }

    @Override
    public void applyChanges(){
        EveeLetheBasedEliminationProofPreferencesManager prefManager =
                ((EveeLetheBasedEliminationProofPreferencesManager) this.proofPreferencesManager);
        SwingUtilities.invokeLater(() -> {
            prefManager.saveTimeOut((double) this.timeOutSpinner.getValue());
        });
    }

}
