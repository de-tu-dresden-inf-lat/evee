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
//                    todo: what should stepSize be? set min/max to avoid checking before value is used for proofGenerator?
                    null, null, 0.001);
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
            JPanel timeOutPanel = new JPanel(new GridBagLayout());
            this.miscellaneousPreferencesPanel.addGroupComponent(timeOutPanel);
            JLabel timeOutLabel = new JLabel(prefManager.getTimeOutUILabel());
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = 0;
            timeOutPanel.add(timeOutLabel, labelConstraints);
            GridBagConstraints spinnerConstraints = new GridBagConstraints();
            spinnerConstraints.gridx = 1;
            spinnerConstraints.gridy = 0;
            timeOutPanel.add(this.timeOutSpinner, spinnerConstraints);
            JLabel timeOutUnit = new JLabel(prefManager.TIME_OUT_UNIT);
            GridBagConstraints unitConstraints = new GridBagConstraints();
            unitConstraints.gridx = 2;
            unitConstraints.gridy = 0;
            timeOutPanel.add(timeOutUnit, unitConstraints);
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
