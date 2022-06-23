package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeEliminationProofPreferencesUI;

import javax.swing.*;
import java.awt.*;

public class EveeLetheBasedEliminationPreferencesUI extends AbstractEveeEliminationProofPreferencesUI {

    private JSpinner timeOutSpinner;
    private final int SPINNER_WIDTH = 100;
    private static final double STEP_SIZE = 0.05;
    private EveeLetheBasedEliminationProofPreferencesManager letheEliminationPreferencesManager;

    public EveeLetheBasedEliminationPreferencesUI(){
        super();
        this.setLetheBasedEliminationProofPreferencesManager(new EveeLetheBasedEliminationProofPreferencesManager());
    }

    public void setLetheBasedEliminationProofPreferencesManager(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager){
        super.setAbstractEliminationProofPreferencesManager(proofPreferencesManager);
        this.letheEliminationPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        SwingUtilities.invokeLater(() -> {
            SpinnerNumberModel timeOutSpinnerNumberModel = new SpinnerNumberModel(this.letheEliminationPreferencesManager.loadTimeOut(),
//                    todo: what should stepSize be? set min/max to avoid checking before value is used for proofGenerator?
                    0d, null, STEP_SIZE);
            this.timeOutSpinner = new JSpinner(timeOutSpinnerNumberModel);
            this.timeOutSpinner.setPreferredSize(new Dimension(this.SPINNER_WIDTH, this.timeOutSpinner.getPreferredSize().height));
            this.timeOutSpinner.setToolTipText(this.letheEliminationPreferencesManager.getTimeOutUIToolTip());
        });
    }

    @Override
    protected void createAndFillHolderPanel(){
        super.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            JPanel timeOutPanel = new JPanel(new GridBagLayout());
            this.holderPanel.addGroupComponent(timeOutPanel);
            JLabel timeOutLabel = new JLabel(this.letheEliminationPreferencesManager.getTimeOutUILabel());
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = 0;
            timeOutPanel.add(timeOutLabel, labelConstraints);
            GridBagConstraints spinnerConstraints = new GridBagConstraints();
            spinnerConstraints.gridx = 1;
            spinnerConstraints.gridy = 0;
            timeOutPanel.add(this.timeOutSpinner, spinnerConstraints);
            JLabel timeOutUnit = new JLabel(this.letheEliminationPreferencesManager.TIME_OUT_UNIT);
            GridBagConstraints unitConstraints = new GridBagConstraints();
            unitConstraints.gridx = 2;
            unitConstraints.gridy = 0;
            timeOutPanel.add(timeOutUnit, unitConstraints);
        });
    }

    @Override
    public void applyChanges(){
        SwingUtilities.invokeLater(() -> {
            this.letheEliminationPreferencesManager.saveTimeOut((double) this.timeOutSpinner.getValue());
        });
    }

}
