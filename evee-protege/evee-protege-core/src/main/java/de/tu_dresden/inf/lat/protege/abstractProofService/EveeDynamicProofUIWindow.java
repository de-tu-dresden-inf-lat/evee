package de.tu_dresden.inf.lat.protege.abstractProofService;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EveeDynamicProofUIWindow implements ItemListener {

    private JFrame frame;
    private JPanel panel;
    private JLabel label;
    private JButton button;
    private JProgressBar progressBar;
    private JDialog cancelDialog;
    private boolean showLoadingScreen;
    private boolean showCancelScreen;
    private boolean proofGenerationFinished;
    private final EveeDynamicProofAdapter proofAdapter;
    private final OWLEditorKit editorKit;
    private final String SET_ID, PREFERENCE_ID, PREFERENCE_KEY;
    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofUIWindow.class);

    public EveeDynamicProofUIWindow(EveeDynamicProofAdapter proofAdapter, String uiTitle, OWLEditorKit editorKit, String setId, String preferenceId, String preferenceKey){
        this.showLoadingScreen = false;
        this.showCancelScreen = false;
        this.proofGenerationFinished = false;
        this.proofAdapter = proofAdapter;
        this.editorKit = editorKit;
        this.SET_ID = setId;
        this.PREFERENCE_ID = preferenceId;
        this.PREFERENCE_KEY = preferenceKey;
        SwingUtilities.invokeLater(() -> {
            this.frame = new JFrame(uiTitle);
            this.panel = new JPanel(new GridLayout(3, 1, 5, 5));
            this.label = new JLabel("", SwingConstants.CENTER);
            this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setIndeterminate(true);
            this.label.setVerticalTextPosition(JLabel.CENTER);
            this.label.setHorizontalTextPosition(JLabel.CENTER);
            this.frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            this.button = new JButton("Cancel");
            this.button.addActionListener(e -> {
                this.cancelGeneration();
            });
            this.panel.add(this.label, BorderLayout.CENTER);
            this.panel.add(this.progressBar);
            this.panel.add(this.button);
            this.frame.getContentPane().add(this.panel);
            this.frame.pack();
            this.frame.setVisible(false);
            this.frame.setSize(600, 150);
            this.frame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this.editorKit.getOWLWorkspace()));
            this.frame.setAlwaysOnTop(true);
            this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    logger.debug("Evee Proof Service UI window closed");
                    cancelGeneration();
                };
            });
        });
    }

    public void showError(String message){
        this.disposeLoadingScreen();
        this.disposeCancelDialog();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(
                    this.editorKit.getOWLWorkspace()), message, "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
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
            this.frame.setVisible(true);
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
            this.frame.dispose();
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
            this.cancelDialog = new JDialog(SwingUtilities.getWindowAncestor(this.editorKit.getOWLWorkspace()));
            this.cancelDialog.setUndecorated(true);
            this.cancelDialog.setModal(true);
            JPanel cancelPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            JLabel cancelLabel = new JLabel("Cancelling proof generation, please wait.", SwingConstants.CENTER);
            cancelLabel.setVerticalTextPosition(JLabel.CENTER);
            cancelLabel.setHorizontalTextPosition(JLabel.CENTER);
            JProgressBar cancelProgressBar = new JProgressBar(0, 100);
            cancelProgressBar.setIndeterminate(true);
            cancelPanel.add(cancelLabel, BorderLayout.CENTER);
            cancelPanel.add(cancelProgressBar);
            this.cancelDialog.getContentPane().add(cancelPanel);
            this.cancelDialog.pack();
            this.cancelDialog.setSize(600, 100);
            this.cancelDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this.editorKit.getOWLWorkspace()));
            this.cancelDialog.setAlwaysOnTop(true);
            this.cancelDialog.setVisible(true);
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

    public void showSubOptimalProofMessage(){
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(this.SET_ID, this.PREFERENCE_ID);
        if (preferences.getBoolean(this.PREFERENCE_KEY, false)){
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

    public void proofGenerationFinished(){
        this.proofGenerationFinished = true;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Preferences preferences = PreferencesManager.getInstance().getPreferencesForSet(this.SET_ID, this.PREFERENCE_ID);
        preferences.putBoolean(this.PREFERENCE_KEY, true);
    }
}
