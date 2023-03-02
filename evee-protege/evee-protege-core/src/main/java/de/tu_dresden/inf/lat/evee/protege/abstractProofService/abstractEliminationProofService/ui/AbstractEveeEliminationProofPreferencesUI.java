package de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractEveeEliminationProofPreferencesUI extends AbstractEveeProofPreferencesUI {

    private JCheckBox protegeReasonerCheckBox;
    private JCheckBox skipStepsCheckBox;
    private JCheckBox suboptimalMsgCheckBox;
    protected PreferencesLayoutPanel miscellaneousPreferencesPanel;
    private final String MISC_OPTIONS = "Miscellaneous Options:";

    public AbstractEveeEliminationProofPreferencesUI(AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(proofPreferencesManager);
    }

    @Override
    protected void createUiElements(){
        super.createUiElements();
        AbstractEveeEliminationProofPreferencesManager proofPreferencesManager =
                (AbstractEveeEliminationProofPreferencesManager) this.proofPreferencesManager;
        SwingUtilities.invokeLater(() -> {
//            this.protegeReasonerCheckBox = new JCheckBox(
//                    proofPreferencesManager.getUseProtegeReasonerUILabel(),
//                    proofPreferencesManager.loadUseProtegeReasoner());
//            this.protegeReasonerCheckBox.setToolTipText(
//                    proofPreferencesManager.getUseProtegeReasonerUIToolTip());
//            this.skipStepsCheckBox = new JCheckBox(proofPreferencesManager.getSkipStepsUILabel(),
//                    proofPreferencesManager.loadSkipSteps());
//            this.skipStepsCheckBox.setToolTipText(proofPreferencesManager.getSkipStepsUIToolTip());
            this.suboptimalMsgCheckBox = new JCheckBox(
                    proofPreferencesManager.getSuboptimalProofWarningUILabel(),
                    proofPreferencesManager.loadShowSuboptimalProofWarning());
            this.suboptimalMsgCheckBox.setToolTipText(proofPreferencesManager.getSuboptimalProofWarningUIToolTip());
        });
    }

    @Override
    protected void createAndFillHolderPanel(){
        super.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            this.miscellaneousPreferencesPanel = new PreferencesLayoutPanel();
            this.holderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            this.holderPanel.add(this.miscellaneousPreferencesPanel);
//            this.miscellaneousPreferencesPanel.addGroup(MISC_OPTIONS);
//            this.miscellaneousPreferencesPanel.addGroupComponent(this.protegeReasonerCheckBox);
//            this.miscellaneousPreferencesPanel.addGroupComponent(this.skipStepsCheckBox);
            this.miscellaneousPreferencesPanel.addGroupComponent(this.suboptimalMsgCheckBox);
        });
    }

    @Override
    public void applyChanges(){
        super.applyChanges();
        SwingUtilities.invokeLater(() -> {
            AbstractEveeEliminationProofPreferencesManager prefManager =
                    (AbstractEveeEliminationProofPreferencesManager) this.proofPreferencesManager;
//            prefManager.saveUseProtegeReasoner(this.protegeReasonerCheckBox.isSelected());
//            prefManager.saveSkipSteps(this.skipStepsCheckBox.isSelected());
            prefManager.saveShowSuboptimalProofWarning(this.suboptimalMsgCheckBox.isSelected());
        });
    }

}
