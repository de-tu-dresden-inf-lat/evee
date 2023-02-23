package de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
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
            JFrame subOptimalProofMessageFrame = new JFrame("Proof generation cancelled");
            subOptimalProofMessageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel subOptimalProofMessagePanel = new JPanel(new GridLayout(3, 1, 5, 5));
            JLabel subOptimalProofMessageLabel = new JLabel(
                    "Due to cancellation of the proof generation, you might be seeing a sub-optimal proof",
                    SwingConstants.CENTER);
            subOptimalProofMessageLabel.setHorizontalTextPosition(JLabel.CENTER);
            subOptimalProofMessageLabel.setVerticalTextPosition(JLabel.CENTER);
            JCheckBox subOptimalProofMessageCheckBox = new JCheckBox("Don't show this message again", false);
            subOptimalProofMessageCheckBox.addItemListener(this);
            JButton subOptimalProofMessageButton = new JButton("OK");
            subOptimalProofMessageButton.addActionListener(e -> subOptimalProofMessageFrame.dispose());
            subOptimalProofMessageCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
            subOptimalProofMessagePanel.add(subOptimalProofMessageLabel);
            subOptimalProofMessagePanel.add(subOptimalProofMessageCheckBox);
            subOptimalProofMessagePanel.add(subOptimalProofMessageButton);
            subOptimalProofMessageFrame.getContentPane().add(subOptimalProofMessagePanel);
            subOptimalProofMessageFrame.pack();
            subOptimalProofMessageFrame.setSize(600, 150);
            subOptimalProofMessageFrame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this.editorKit.getOWLWorkspace()));
            subOptimalProofMessageFrame.setVisible(true);
        });
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        this.proofPreferencesManager.saveShowSuboptimalProofWarning(false);
    }

}
