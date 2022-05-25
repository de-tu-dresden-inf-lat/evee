package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;

import javax.swing.*;
import java.util.TreeMap;

public abstract class AbstractEveeEliminationProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    protected final TreeMap<String, JSpinner> miscellaneousSpinners;
    private JSpinner timeOutSpinner;

    public AbstractEveeEliminationProofPreferencesUI(AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(proofPreferencesManager);
        this.miscellaneousSpinners = new TreeMap<>();
        this.labeledGroupNumber += 1;
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        AbstractEveeEliminationProofPreferencesManager proofPreferencesManager =
                (AbstractEveeEliminationProofPreferencesManager) this.proofPreferencesManager;
        SwingUtilities.invokeLater(() -> {
            JCheckBox protegeReasoner = new JCheckBox(
                    proofPreferencesManager.getUseProtegeReasonerUILabel(),
                    proofPreferencesManager.loadUseProtegeReasoner());
            protegeReasoner.setToolTipText(proofPreferencesManager.getUseProtegeReasonerUIToolTip());
            this.miscellaneousCheckBoxes.put(proofPreferencesManager.PROTEGE_REASONER, protegeReasoner);
            JCheckBox skipSteps = new JCheckBox(
                    proofPreferencesManager.getSkipStepsUILabel(),
                    proofPreferencesManager.loadSkipSteps());
            skipSteps.setToolTipText(proofPreferencesManager.getSkipStepsUIToolTip());
            this.miscellaneousCheckBoxes.put(proofPreferencesManager.SKIP_STEPS, skipSteps);
//            todo: what should minimum and maximum be here?
            SpinnerNumberModel timeOutSpinnerNumberModel = new SpinnerNumberModel(proofPreferencesManager.loadTimeOut(),
                    1, 10000, 1);
            this.timeOutSpinner = new JSpinner(timeOutSpinnerNumberModel);
            this.timeOutSpinner.setMaximumSize(this.timeOutSpinner.getPreferredSize());
            this.timeOutSpinner.setToolTipText(proofPreferencesManager.getTimeOutUIToolTip());
        });
    }

    @Override
    protected void addAdditionalPreferences(){
        SwingUtilities.invokeLater(() -> {
            PreferencesLayoutPanel timeOutPanel = new PreferencesLayoutPanel();
            timeOutPanel.addGroup(((AbstractEveeEliminationProofPreferencesManager)
                    this.proofPreferencesManager).getTimeOutUILabel());
            this.holderPanel.add(timeOutPanel);
            timeOutPanel.addGroupComponent(this.timeOutSpinner);
        });
    }

    @Override
    protected void saveAdditionalPreferences(){
        AbstractEveeEliminationProofPreferencesManager prefManager =
                ((AbstractEveeEliminationProofPreferencesManager) this.proofPreferencesManager);
        SwingUtilities.invokeLater(() -> {
            prefManager.saveIntegerPreferenceValue(prefManager.TIME_OUT, (int) this.timeOutSpinner.getValue());
        });
    }

}
