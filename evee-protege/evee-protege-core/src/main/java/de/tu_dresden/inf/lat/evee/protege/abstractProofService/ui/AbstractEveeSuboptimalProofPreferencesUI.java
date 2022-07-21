package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeSuboptimalProofPreferencesManager;

import javax.swing.*;

public class AbstractEveeSuboptimalProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    private JCheckBox suboptimalMsgCheckBox;
    private AbstractEveeSuboptimalProofPreferencesManager suboptimalProofPreferencesManager;

    public AbstractEveeSuboptimalProofPreferencesUI(){
        super();
    }

    public void setAbstractSuboptimalProofPreferencesManager(AbstractEveeSuboptimalProofPreferencesManager proofPreferencesManager){
        super.setAbstractProofPreferencesManager(proofPreferencesManager);
        this.suboptimalProofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        SwingUtilities.invokeLater(() -> {
            this.suboptimalMsgCheckBox = new JCheckBox(
                    this.suboptimalProofPreferencesManager.getSuboptimalProofWarningUILabel(),
                    this.suboptimalProofPreferencesManager.loadShowSuboptimalProofWarning());
            this.suboptimalMsgCheckBox.setToolTipText(
                    this.suboptimalProofPreferencesManager.getSuboptimalProofWarningUIToolTip());
        });
    }

    @Override
    protected void createAndFillHolderPanel(){
        super.createAndFillHolderPanel();
        this.addMiscellaneousGroup();
        SwingUtilities.invokeLater(() -> {
            this.holderPanel.addGroupComponent(this.suboptimalMsgCheckBox);
        });
    }

    @Override
    public void applyChanges(){
        SwingUtilities.invokeLater(() ->
                this.suboptimalProofPreferencesManager.saveShowSuboptimalProofWarning(this.suboptimalMsgCheckBox.isSelected()));
        super.applyChanges();
    }
}
