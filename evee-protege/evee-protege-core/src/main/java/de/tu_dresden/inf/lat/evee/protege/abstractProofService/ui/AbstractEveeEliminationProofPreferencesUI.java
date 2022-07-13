package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;

import javax.swing.*;

public abstract class AbstractEveeEliminationProofPreferencesUI extends AbstractEveeSuboptimalProofPreferencesUI {

    private JCheckBox protegeReasonerCheckBox;
    private JCheckBox skipStepsCheckBox;
    private JCheckBox varyJustificationsCheckBox;
    private AbstractEveeEliminationProofPreferencesManager eliminationProofPreferencesManager;

    public AbstractEveeEliminationProofPreferencesUI() {
        super();
    }

    public void setAbstractEliminationProofPreferencesManager(AbstractEveeEliminationProofPreferencesManager proofPreferencesManager){
        super.setAbstractSuboptimalProofPreferencesManager(proofPreferencesManager);
        this.eliminationProofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        SwingUtilities.invokeLater(() -> {
            this.protegeReasonerCheckBox = new JCheckBox(
                    this.eliminationProofPreferencesManager.getUseProtegeReasonerUILabel(),
                    this.eliminationProofPreferencesManager.loadUseProtegeReasoner());
            this.protegeReasonerCheckBox.setToolTipText(
                    this.eliminationProofPreferencesManager.getUseProtegeReasonerUIToolTip());
            this.skipStepsCheckBox = new JCheckBox(
                    this.eliminationProofPreferencesManager.getSkipStepsUILabel(),
                    this.eliminationProofPreferencesManager.loadSkipSteps());
            this.skipStepsCheckBox.setToolTipText(
                    this.eliminationProofPreferencesManager.getSkipStepsUIToolTip());
            this.varyJustificationsCheckBox = new JCheckBox(
                    this.eliminationProofPreferencesManager.getVaryJustificationsUILabel(),
                    this.eliminationProofPreferencesManager.loadVaryJustifications());
            this.varyJustificationsCheckBox.setToolTipText(
                    this.eliminationProofPreferencesManager.getVaryJustificationsUIToolTip());
        });
    }

    @Override
    protected void createAndFillHolderPanel(){
        super.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            this.holderPanel.addGroupComponent(this.protegeReasonerCheckBox);
            this.holderPanel.addGroupComponent(this.skipStepsCheckBox);
            this.holderPanel.addGroupComponent(this.varyJustificationsCheckBox);
        });
    }

    @Override
    public void applyChanges(){
        super.applyChanges();
        SwingUtilities.invokeLater(() -> {
            this.eliminationProofPreferencesManager.saveUseProtegeReasoner(this.protegeReasonerCheckBox.isSelected());
            this.eliminationProofPreferencesManager.saveSkipSteps(this.skipStepsCheckBox.isSelected());
            this.eliminationProofPreferencesManager.saveVaryJustifications(this.varyJustificationsCheckBox.isSelected());
        });
    }

}
