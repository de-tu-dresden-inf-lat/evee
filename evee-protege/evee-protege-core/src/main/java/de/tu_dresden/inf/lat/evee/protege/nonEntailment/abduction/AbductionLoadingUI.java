package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class AbductionLoadingUI {

    private JDialog loadingScreen;

    private final Logger logger = LoggerFactory.getLogger(AbductionLoadingUI.class);

    public AbductionLoadingUI(String uiTitle, OWLEditorKit editorKit){
        this.logger.debug("Creating AbductionLoadingUI");
        SwingUtilities.invokeLater(() -> {
            this.loadingScreen = new JDialog(ProtegeManager.getInstance().getFrame(editorKit.getWorkspace()));
            this.loadingScreen.setTitle(uiTitle);
//            todo: cancellation possible??
            this.loadingScreen.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            JPanel holderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            JLabel label = new JLabel("Generating hypotheses", SwingConstants.CENTER);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            holderPanel.add(label, BorderLayout.CENTER);
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setIndeterminate(true);
            holderPanel.add(progressBar);
            this.loadingScreen.getContentPane().add(holderPanel);
            this.loadingScreen.pack();
            this.loadingScreen.setSize(600, 150);
            this.loadingScreen.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(editorKit.getWorkspace())));
            this.loadingScreen.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.loadingScreen.setVisible(false);
        });
        this.logger.debug("AbductionLoadingUI created successfully");
    }

    public void showLoadingScreen(){
        this.logger.debug("Showing loading screen");
        SwingUtilities.invokeLater(() -> {
            this.loadingScreen.setVisible(true);
        });
    }

    public void disposeLoadingScreen(){
        this.logger.debug("Disposing loading screen");
        SwingUtilities.invokeLater(() -> {
            this.loadingScreen.dispose();
        });
    }

}
