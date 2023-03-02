package de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EveeDynamicEliminationProofLoadingUI extends EveeDynamicProofLoadingUI implements ItemListener {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;

    public EveeDynamicEliminationProofLoadingUI(AbstractEveeDynamicProofAdapter proofAdapter, String uiTitle, OWLEditorKit editorKit, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(proofAdapter, uiTitle, editorKit);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    public void showSubOptimalProofMessage(){
        if (! this.proofPreferencesManager.loadShowSuboptimalProofWarning()){
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JDialog subOptimalProofMessageDialog = new JDialog(ProtegeManager.getInstance().getFrame(this.editorKit.getWorkspace()));
            subOptimalProofMessageDialog.setTitle("Proof generation cancelled");
            subOptimalProofMessageDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            JPanel subOptimalProofMessagePanel = new JPanel(new GridLayout(3, 1, 5, 5));
            JLabel subOptimalProofMessageLabel = new JLabel(
                    "Due to cancellation of the proof generation, you might be seeing a sub-optimal proof",
                    SwingConstants.CENTER);
            subOptimalProofMessageLabel.setHorizontalTextPosition(JLabel.CENTER);
            subOptimalProofMessageLabel.setVerticalTextPosition(JLabel.CENTER);
            JCheckBox subOptimalProofMessageCheckBox = new JCheckBox("Don't show this message again", false);
            subOptimalProofMessageCheckBox.addItemListener(this);
            JButton subOptimalProofMessageButton = new JButton("OK");
            subOptimalProofMessageButton.addActionListener(e -> subOptimalProofMessageDialog.dispose());
            subOptimalProofMessageCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
            subOptimalProofMessagePanel.add(subOptimalProofMessageLabel);
            subOptimalProofMessagePanel.add(subOptimalProofMessageCheckBox);
            subOptimalProofMessagePanel.add(subOptimalProofMessageButton);
            subOptimalProofMessageDialog.getContentPane().add(subOptimalProofMessagePanel);
            subOptimalProofMessageDialog.pack();
            subOptimalProofMessageDialog.setSize(600, 150);
            subOptimalProofMessageDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.editorKit.getWorkspace())));
            subOptimalProofMessageDialog.setVisible(true);
        });
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        this.proofPreferencesManager.saveShowSuboptimalProofWarning(false);
    }

}
