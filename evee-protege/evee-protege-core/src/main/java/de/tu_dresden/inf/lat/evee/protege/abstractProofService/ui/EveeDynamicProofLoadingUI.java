package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

public class EveeDynamicProofLoadingUI {

    private JDialog loadingDialog;
    private JPanel panel;
    private JLabel label;
    private JButton button;
    private JProgressBar progressBar;
    private JDialog cancelDialog;
    private boolean showLoadingScreen;
    private boolean showCancelScreen;
    private boolean proofGenerationFinished;
    private AbstractEveeDynamicProofAdapter proofAdapter;
    protected OWLEditorKit editorKit;
    private final String uiTitle;
    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofLoadingUI.class);

    public EveeDynamicProofLoadingUI(String uiTitle){
        this.uiTitle = uiTitle;
    }

    public void setProofAdapter(AbstractEveeDynamicProofAdapter proofAdapter){
        this.proofAdapter = proofAdapter;
    }

    public void initialize(OWLEditorKit editorKit){
        this.editorKit = editorKit;
        this.showLoadingScreen = false;
        this.showCancelScreen = false;
        this.proofGenerationFinished = false;
        SwingUtilities.invokeLater(() -> {
            this.loadingDialog = new JDialog(ProtegeManager.getInstance().getFrame(this.editorKit.getWorkspace()));
            this.loadingDialog.setTitle(uiTitle);
            this.panel = new JPanel(new GridLayout(3, 1, 5, 5));
            this.label = new JLabel("", SwingConstants.CENTER);
            this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setIndeterminate(true);
            this.label.setVerticalTextPosition(JLabel.CENTER);
            this.label.setHorizontalTextPosition(JLabel.CENTER);
            this.loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            this.button = new JButton("Cancel");
            this.button.addActionListener(e -> {
                this.cancelGeneration();
            });
            this.panel.add(this.label, BorderLayout.CENTER);
            this.panel.add(this.progressBar);
            this.panel.add(this.button);
            this.loadingDialog.getContentPane().add(this.panel);
            this.loadingDialog.setSize(600, 150);
            this.loadingDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.loadingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    logger.debug("Evee Proof Service UI window closed");
                    cancelGeneration();
                };
            });
            UIUtilities.packAndSetWindow(this.loadingDialog, this.editorKit, false);
        });
    }

    public void showError(String message){
        this.disposeLoadingScreen();
        this.disposeCancelDialog();
        UIUtilities.showError(message, this.editorKit);
    }

    public void updateMessage(String message){
        if (message != null  && !this.showCancelScreen){
            SwingUtilities.invokeLater( () -> {
                this.label.setText(message);
            });
        }
    }

    public void updateProgress(Integer progress){
        SwingUtilities.invokeLater(() ->{
            if (this.progressBar.isIndeterminate()){
                return;
            }
            if (progress >= this.progressBar.getMaximum()){
                this.disposeLoadingScreen();
            }
            else {
                this.progressBar.setValue(progress);
                this.progressBar.setString(progress + " / " + this.progressBar.getMaximum());
            }
        });
    }

    public void showWindow(){
        SwingUtilities.invokeLater(() -> {
            this.showLoadingScreen = true;
            this.loadingDialog.setVisible(true);
        });
    }

    public void setupProgressBar(Integer progressMaximum){
        SwingUtilities.invokeLater(() -> {
            this.progressBar.setIndeterminate(false);
            this.progressBar.setMaximum(progressMaximum);
            this.progressBar.setString("0 / " + progressMaximum);
//            todo: implement methods so that inherited class is able activate this feature
//            this.progressBar.setStringPainted(true);
        });
    }

    public void disposeLoadingScreen(){
        this.disposeCancelDialog();
        SwingUtilities.invokeLater( () -> {
            if (this.showLoadingScreen){
                this.showLoadingScreen = false;
                if (! this.progressBar.isIndeterminate()){
                    this.progressBar.setValue(this.progressBar.getMaximum());
                }
                this.loadingDialog.dispose();
            }
        });
    }

    public void cancelGeneration(){
        this.disposeLoadingScreen();
        if (this.proofGenerationFinished){
            return;
        }
        SwingUtilities.invokeLater(() -> {
            this.showCancelScreen = true;
            this.cancelDialog = new JDialog(ProtegeManager.getInstance()
                    .getFrame(this.editorKit.getWorkspace()));
//            this.cancelDialog.setUndecorated(true);
            this.cancelDialog.setResizable(false);
            this.cancelDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.cancelDialog.addWindowListener(new java.awt.event.WindowAdapter(){
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowClosingEvent){
                    Toolkit.getDefaultToolkit().beep();
                }
            });
            this.cancelDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            JPanel cancelPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            JLabel cancelLabel = new JLabel("Cancelling proof generation, please wait.", SwingConstants.CENTER);
            cancelLabel.setVerticalTextPosition(JLabel.CENTER);
            cancelLabel.setHorizontalTextPosition(JLabel.CENTER);
            JProgressBar cancelProgressBar = new JProgressBar(0, 100);
            cancelProgressBar.setIndeterminate(true);
            cancelPanel.add(cancelLabel, BorderLayout.CENTER);
            cancelPanel.add(cancelProgressBar);
            this.cancelDialog.getContentPane().add(cancelPanel);
            this.cancelDialog.setSize(600, 100);
            UIUtilities.packAndSetWindow(this.cancelDialog, this.editorKit, true);
        });
        this.proofAdapter.cancelProofGeneration();
    }

    public void disposeCancelDialog(){
        SwingUtilities.invokeLater(() ->{
            if (this.showCancelScreen){
                this.showCancelScreen = false;
                this.cancelDialog.setModal(false);
                this.cancelDialog.dispose();
            }
        });
    }

    public void proofGenerationFinished(){
        this.proofGenerationFinished = true;
    }

}
