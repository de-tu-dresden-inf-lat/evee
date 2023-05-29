package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEventType;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class NonEntailmentExplanationLoadingUIManager implements
        IExplanationLoadingUIListener, IExplanationLoadingUIEventGenerator {

    protected String uiTitle;
    private OWLEditorKit owlEditorKit;
    protected boolean paintProgressBarString;
    protected static final String DEFAULT_MESSAGE = "Generating Explanations";
    protected static final String DEFAULT_CANCELLATION_MESSAGE = "Cancelling generation, please wait";

    private IExplanationLoadingUIListener viewComponentListener;
    private JDialog loadingScreen;
    private JDialog cancellationScreen;
    private boolean loadingScreenDisposed = true;
    private boolean cancellationScreenDisposed = true;
    private JLabel loadingScreenProgressLabel;
    private JProgressBar loadingScreenProgressBar;
    private static final String CANCEL_BUTTON_NAME = "Cancel";


    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationLoadingUIManager.class);

    public NonEntailmentExplanationLoadingUIManager(String uiTitle){
        new NonEntailmentExplanationLoadingUIManager(uiTitle, false);
    }

    public NonEntailmentExplanationLoadingUIManager(String uiTitle, boolean paintProgressBarString){
        this.logger.debug("Creating NonEntailmentExplanationLoadingUIManager");
        this.uiTitle = uiTitle;
        this.paintProgressBarString = paintProgressBarString;
        this.logger.debug("NonEntailmentExplanationLoadingUIManager created successfully");
    }

    public void setup(OWLEditorKit editorKit){
        this.owlEditorKit = editorKit;
    }

    public void initialise(){
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Creating loading UI elements");
            this.initLoadingScreen();
            this.resetLoadingScreen();
            this.initCancellationScreen();
            this.resetCancellationScreen();
            this.logger.debug("All loading UI elements have been  created");
        });
    }

    public void dispose(){
        this.logger.debug("Disposing loading UI");
        if (! this.loadingScreenDisposed){
            this.loadingScreenDisposed = true;
            this.loadingScreen.dispose();
        }
        if (! this.cancellationScreenDisposed){
            this.cancellationScreenDisposed = true;
            this.cancellationScreen.dispose();
        }
        this.logger.debug("Completed disposing loading UI");
    }

    @Override
    public void registerLoadingUIListener(IExplanationLoadingUIListener listener) {
        this.viewComponentListener = listener;
    }

    protected void initLoadingScreen(){
        this.loadingScreen = new JDialog(ProtegeManager
                .getInstance().getFrame(this.owlEditorKit.getWorkspace()));
        this.loadingScreenDisposed = false;
        this.logger.debug("New loading screen initialised");
    }

    protected void initCancellationScreen(){
        this.cancellationScreen = new JDialog(ProtegeManager
                .getInstance().getFrame(owlEditorKit.getWorkspace()));
        this.cancellationScreenDisposed = false;
        this.logger.debug("New cancellation screen initialised");
    }

    /**
     * should be used right after every initLoadingScreen
     */
    protected void resetLoadingScreen(){
        this.logger.debug("Resetting loading screen");
        this.loadingScreen.setTitle(this.uiTitle);
        this.loadingScreen.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel holderPanel = new JPanel(new GridLayout(
                3,1, 5, 5));
        this.loadingScreenProgressLabel =
                UIUtilities.createProgressUILabel(DEFAULT_MESSAGE);
        holderPanel.add(this.loadingScreenProgressLabel, BorderLayout.CENTER);
        this.loadingScreenProgressBar = new JProgressBar(0, 100);
        this.loadingScreenProgressBar.setIndeterminate(true);
        holderPanel.add(this.loadingScreenProgressBar);
        JButton cancelButton = UIUtilities.createNamedButton(
                null, CANCEL_BUTTON_NAME, null,
                e -> {
                    this.cancelGeneration();
                });
        holderPanel.add(cancelButton);
        this.loadingScreen.getContentPane().add(holderPanel);
        this.loadingScreen.pack();
        this.loadingScreen.setVisible(false);
        this.loadingScreen.setSize(600, 150);
        this.loadingScreen.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                ProtegeManager.getInstance().getFrame(this.owlEditorKit.getWorkspace())));
        this.loadingScreen.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        this.loadingScreen.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cancelGeneration();
            }
        });
        this.logger.debug("Resetting loading screen completed");
    }

    /**
     * should be used right after every initCancellationScreen
     */
    protected void resetCancellationScreen(){
        this.logger.debug("Resetting cancellation screen");
        this.cancellationScreen.setResizable(false);
        this.cancellationScreen.setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE);
        this.cancellationScreen.addWindowListener(
                new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowClosingEvent){
                Toolkit.getDefaultToolkit().beep();
            }
        });
        this.cancellationScreen.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        JPanel cancelPanel = new JPanel(
                new GridLayout(2, 1, 5, 5));
        JLabel cancellationScreenProgressLabel = UIUtilities.createProgressUILabel(
                DEFAULT_CANCELLATION_MESSAGE);
        JProgressBar cancellationScreenProgressBar = new JProgressBar(0, 100);
        cancellationScreenProgressBar.setIndeterminate(true);
        cancelPanel.add(cancellationScreenProgressLabel, BorderLayout.CENTER);
        cancelPanel.add(cancellationScreenProgressBar);
        this.cancellationScreen.getContentPane().add(cancelPanel);
        this.cancellationScreen.pack();
        this.cancellationScreen.setSize(600, 100);
        this.cancellationScreen.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                ProtegeManager.getInstance().getFrame(
                        this.owlEditorKit.getWorkspace())));
        this.logger.debug("Resetting cancellation screen completed");
    }

    @Override
    public void handleUIEvent(ExplanationLoadingUIEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()){
                case UPDATE_LOADING_MESSAGE:
                    this.updateLoadingMessage(event.getMessage());
                    break;
                case UPDATE_LOADING_PROGRESS:
                    this.updateLoadingProgress(event.getProgressValue());
                    break;
                case UPDATE_LOADING_MAXIMUM:
                    this.setLoadingProgressbarMaximum(event.getMaximum());
                    break;
                case EXPLANATION_GENERATION_FINISHED:
                    this.finalizeLoadingScreen();
                    this.disposeLoadingScreen();
                    this.disposeCancellationScreen();
                    break;
//                case SHOW_ERROR:
//                    this.disposeLoadingScreen();
//                    this.disposeCancellationScreen();
//                    this.showError(event.getErrorMessage());
//                    break;
//                case SHOW_LOADING_SCREEN:
//                    this.initLoadingScreen();
//                    this.resetLoadingScreen();
//                    this.showLoadingScreen();
//                    break;
//                case DISPOSE_LOADING_SCREEN:
//                    this.disposeLoadingScreen();
//                    break;
//                case SHOW_CANCEL_SCREEN:
//                    this.initCancellationScreen();
//                    this.resetCancellationScreen();
//                    this.showCancellationScreen();
//                    break;
//                case DISPOSE_CANCEL_SCREEN:
//                    this.disposeCancellationScreen();
//                    break;
            }
        });
    }

    protected void setLoadingProgressbarMaximum(int maximum){
        this.logger.debug("Trying to update loading progress maximum");
        if (this.loadingScreenDisposed){
            this.logger.debug("Loading screen already disposed, cannot update maximum");
        } else {
            this.logger.debug("Loading screen progress maximum updated to: {}", maximum);
            this.loadingScreenProgressBar.setIndeterminate(false);
            this.loadingScreenProgressBar.setMaximum(maximum);
            this.loadingScreenProgressBar.setString("0 / " + maximum);
        }
        if (this.paintProgressBarString){
            this.logger.debug("Loading Screen Progress Bar String activated");
            this.loadingScreenProgressBar.setStringPainted(true);
        }
    }

    protected void finalizeLoadingScreen(){
        this.logger.debug("Trying to set loading screen progress bar value to max");
        if (this.loadingScreenDisposed){
            this.logger.debug("Loading screen already disposed, cannot set progress bar value to max");
        } else {
            this.logger.debug("Setting loading screen progress bar value to max");
            this.loadingScreenProgressBar.setValue(this.loadingScreenProgressBar.getMaximum());
        }
    }

    protected void updateLoadingMessage(String message){
        this.logger.debug("Trying to update loading screen message");
        if (message == null){
            this.logger.debug("Message is null, cannot update loading screen message");
        } else if (this.loadingScreenDisposed){
            this.logger.debug("Loading screen already disposed, cannot update message");
        } else {
            this.logger.debug("Updating loading screen message with: {}", message);
            this.loadingScreenProgressLabel.setText(message);
        }
    }

    protected void updateLoadingProgress(int progress){
        this.logger.debug("Trying to update loading screen progress");
        if (this.loadingScreenDisposed){
            this.logger.debug("Loading screen already disposed, cannot update progress");
        } else if (this.loadingScreenProgressBar.isIndeterminate()){
            this.logger.debug("Loading screen is indeterminate, cannot update progress");
        }
        else if (progress >= this.loadingScreenProgressBar.getMaximum()){
            this.logger.debug("Loading progress maximum reached, disposing loading screen");
            this.disposeLoadingScreen();
        }
        else {
            this.logger.debug("Updating progress with value: {}", progress);
            this.loadingScreenProgressBar.setValue(progress);
            this.loadingScreenProgressBar.setString(progress + " / " + this.loadingScreenProgressBar.getMaximum());
        }
    }

    /**
     * Called whenever the LOADING SCREEN is closed, either via closing the Window or
     * pressing the "Cancel"-button.
     * Starts cancellation process by informing the view component of the cancellation.
     * View component will delegate this information to the currently active
     * non entailment explanation service.
     * Note: Displays CANCELLATION SCREEN, which cannot be closed by the user.
     */
    protected void cancelGeneration(){
//        remove all loading screens
//        show cancel screen
//        somehow inform progressGenerator that generation should be cancelled
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Loading dialog closed by user interaction");
            this.disposeLoadingScreen();
            this.initCancellationScreen();
            this.resetCancellationScreen();
            this.showCancellationScreen();
        });
        this.viewComponentListener.handleUIEvent(
                new ExplanationLoadingUIEvent(
                        ExplanationLoadingUIEventType
                                .EXPLANATION_GENERATION_CANCELLED));
    }

    protected void showError(String message){
        UIUtilities.showError(message, this.owlEditorKit);
    }

    protected void showLoadingScreen(){
        this.logger.debug("Trying to show loading screen");
        if (this.loadingScreenDisposed){
            this.logger.debug("Current loading screen already disposed, " +
                    "cannot show loading screen");
        } else if (this.loadingScreen.isVisible()){
            this.logger.debug("Loading screen already shown");
        } else{
            this.logger.debug("Loading screen visibility set to true");
            loadingScreen.setVisible(true);
        }
    }

    public void disposeLoadingScreen(){
        this.logger.debug("Trying to dispose loading screen");
        if (this.loadingScreenDisposed){
            this.logger.debug("Loading screen already disposed");
        } else{
            this.loadingScreenDisposed = true;
            this.loadingScreen.dispose();
            this.logger.debug("Loading screen disposed");
        }
    }

    protected void showCancellationScreen(){
        this.logger.debug("Trying to show cancellation screen");
        if (this.cancellationScreenDisposed){
            this.logger.debug("Cancellation screen already disposed, " +
                    "cannot show cancellation screen");
        } else if (this.cancellationScreen.isVisible()){
            this.logger.debug("Cancellation screen already shown");
        } else{
            this.cancellationScreen.setVisible(true);
            this.logger.debug("Cancellation screen visibility set to true");
        }
    }

    protected void disposeCancellationScreen(){
        this.logger.debug("Trying to dispose cancellation screen");
        if (this.cancellationScreenDisposed){
            this.logger.debug("Cancellation screen already disposed");
        } else{
            this.cancellationScreenDisposed = true;
            this.cancellationScreen.dispose();
            this.logger.debug("Cancellation screen disposed");
        }
    }


    public void activeLoadingUI(){
        SwingUtilities.invokeLater(this::showLoadingScreen);
    }

    public void resetLoadingUI(){
        SwingUtilities.invokeLater(() -> {
            this.disposeLoadingScreen();
            this.initLoadingScreen();
            this.resetLoadingScreen();
            this.disposeCancellationScreen();
            this.initCancellationScreen();
            this.resetCancellationScreen();
        });
    }

}
