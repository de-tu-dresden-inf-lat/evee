package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeProofSignatureUIPreferenceManager;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.LoadingAbortedException;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.SignatureFileHandler;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.*;

public class EveeDynamicProofSignatureSelectionWindow extends ProtegeOWLAction implements ActionListener {

    private static final String INIT = "Manage signature";
    private static final String LOAD = "LOAD_SIGNATURE";
    private static final String LOAD_NAME = "Load from file";
    private static final String LOAD_TOOLTIP = "Load a signature from a file";
    private static final String SAVE = "SAVE_SIGNATURE";
    private static final String SAVE_NAME = "Save to file";
    private static final String SAVE_TOOLTIP = "Save a signature to a file";
    private static final String CANCEL = "CANCEL_SIGNATURE_SELECTION";
    private static final String APPLY = "APPLY_SIGNATURE";
//    private static final String USE_SIGNATURE_DELIMITER = "##### Use Signature: #####";
//    private static final String TRUE = "TRUE";
//    private static final String FALSE = "FALSE";
//    private static final String CLASSES_DELIMITER = "##### Classes: #####";
//    private static final String OBJECT_PROPERTIES_DELIMITER = "##### Object Properties: #####";
//    private static final String INDIVIDUAL_DELIMITER = "##### Individuals: #####";
    private static final String ANONYMOUS_ONTOLOGY_ERROR_MSG = "<html><center>Ontology has no IRI.</center><center>Changes to the signature are only allowed if the ontology has an IRI.</center>";
    private static final String SIGNATURE_SAVING_ERROR_MSG = "<html><center>Error while saving signature</center>";
//    todo: improve wording
    private final String topLabelText = "<html><center>Any proof step that contains only those OWL Entities in the right list will <b>not</b> be explained in any Evee proof.</center><center>This will also be considered when optimizing the Evee proofs.</center>";
    private final Insets insets = new Insets(5, 5, 5, 5);
    private JDialog dialog;
    private JPanel holderPanel;
    private JButton loadButton;
    private JButton saveButton;
    private JCheckBox useSignatureCheckBox;
    private final EveeProofSignatureUIPreferenceManager signaturePreferencesManager;
    private EveeDynamicProofSignatureSelectionCoreUI signatureSelectionUI;
    private OWLOntology activeOntology;
    private boolean signatureEnabled;
    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofSignatureSelectionWindow.class);

    public EveeDynamicProofSignatureSelectionWindow(){
        this.signaturePreferencesManager = new EveeProofSignatureUIPreferenceManager();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case INIT :
                this.createUI();
                break;
            case LOAD :
                this.load();
                break;
            case SAVE :
                this.save();
                break;
            case CANCEL :
                this.cancel();
                break;
            case APPLY :
                this.apply();
                break;
        }
    }

    @Override
    public void initialise(){
    }

    @Override
    public void dispose(){
        this.signatureSelectionUI.dispose();
    }

    private void createUI(){
        if (this.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().isPresent()){
            this.activeOntology = this.getOWLModelManager().getActiveOntology();
        }
        else{
            UIUtilities.showError(ANONYMOUS_ONTOLOGY_ERROR_MSG, this.getOWLEditorKit());
            return;
        }
        String ontoName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
        SwingUtilities.invokeLater(() -> {
            this.signatureSelectionUI = new EveeDynamicProofSignatureSelectionCoreUI(this,
                    this.getOWLEditorKit());
            this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
            this.dialog = new JDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()));
            this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.dialog.setTitle("Manage signature for " + ontoName);
            this.holderPanel = new JPanel();
            this.dialog.getContentPane().add(holderPanel);
            this.holderPanel.setLayout(new GridBagLayout());
            this.addTopLabel();
            this.addSignaturePanelComponents();
            this.addMiddleButtons();
            this.addLowerInteractiveElements();
            this.dialog.pack();
            this.dialog.setLocationRelativeTo(
                    ProtegeManager.getInstance().getFrame(this.getWorkspace()));
            this.dialog.setVisible(true);
        });
    }

    private JPanel createTopLabel(){
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        JLabel topCenterLabel = new JLabel(topLabelText);
        topCenterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        topCenterLabel.setVerticalTextPosition(JLabel.CENTER);
        topCenterLabel.setHorizontalTextPosition(JLabel.CENTER);
        labelPanel.add(topCenterLabel, Box.CENTER_ALIGNMENT);
        topCenterLabel.setHorizontalAlignment(JLabel.CENTER);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return labelPanel;
    }

    private void addTopLabel(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.insets = this.insets;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1;
        gbc.weighty = 0.3;
        JPanel topLabelPanel = this.createTopLabel();
        this.holderPanel.add(topLabelPanel, gbc);
    }

    private void addSignaturePanelComponents(){
//        getOntologyIRI().isPresent() was checked earlier during UI-creation
        String ontologyName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
        Set<OWLEntity> knownEntitySet = this.signaturePreferencesManager.getKnownSignatureForUI(
                this.activeOntology, ontologyName);
        this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
        boolean useSignature = this.signaturePreferencesManager.getUseSignatureForUI(ontologyName);
        this.signatureSelectionUI.enableSignature(useSignature);
        JComponent ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedComponent();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = this.insets;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        this.holderPanel.add(ontologySignaturePanel, gbc);
        JPanel selectedSignaturePanel = this.signatureSelectionUI.getSelectedSignaturePanel();
        gbc.gridx = 2;
        this.holderPanel.add(selectedSignaturePanel, gbc);
    }

    private void addMiddleButtons(){
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.PAGE_AXIS));
        toolBarPanel.add(Box.createGlue());
        toolBarPanel.add(this.signatureSelectionUI.getAddButton());
        toolBarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        toolBarPanel.add(this.signatureSelectionUI.getDeleteButton());
        toolBarPanel.add(Box.createGlue());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = this.insets;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 0.1;
        gbc.weightx = 0.1;
        this.holderPanel.add(toolBarPanel, gbc);
    }

    private void addLowerInteractiveElements(){
//        first row:
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.LINE_AXIS));
        JLabel checkBoxLabel = new JLabel("Use Signature for proofs:");
        checkBoxLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        checkBoxLabel.setVerticalTextPosition(JLabel.CENTER);
        checkBoxLabel.setHorizontalTextPosition(JLabel.CENTER);
        checkBoxLabel.setHorizontalAlignment(JLabel.CENTER);
        checkBoxPanel.add(checkBoxLabel);
        checkBoxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        this.useSignatureCheckBox = new JCheckBox();
//        note: getOntologyIRI().isPresent() was checked earlier during UI-creation
        boolean enabled = this.signaturePreferencesManager.getUseSignatureForUI(
                this.activeOntology.getOntologyID().getOntologyIRI().get().toString());
        this.useSignatureCheckBox.setSelected(enabled);
        this.signatureEnabled = enabled;
        this.useSignatureCheckBox.addItemListener(e -> enableButtons(e.getStateChange() == ItemEvent.SELECTED));
        this.addLowerInteractiveElementBorder(checkBoxPanel);
        checkBoxPanel.add(this.useSignatureCheckBox);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = this.insets;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.1;
        gbc.weightx = 0.1;
        this.holderPanel.add(checkBoxPanel, gbc);
        JPanel clearPanel = new JPanel();
        clearPanel.setLayout(new BoxLayout(clearPanel, BoxLayout.LINE_AXIS));
        clearPanel.add(Box.createGlue());
        clearPanel.add(this.signatureSelectionUI.getClearButton());
        clearPanel.add(Box.createGlue());
        this.addLowerInteractiveElementBorder(clearPanel);
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        this.holderPanel.add(clearPanel, gbc);

//        second row:
        JPanel fileOperationButtonPanel = new JPanel();
        fileOperationButtonPanel.setLayout(new BoxLayout(fileOperationButtonPanel, BoxLayout.LINE_AXIS));
        this.loadButton = this.createButton(
                LOAD, LOAD_NAME, LOAD_TOOLTIP);
        this.loadButton.setEnabled(this.signatureEnabled);
        fileOperationButtonPanel.add(loadButton);
        fileOperationButtonPanel.add(Box.createGlue());
        this.saveButton = this.createButton(
                SAVE, SAVE_NAME, SAVE_TOOLTIP);
        this.saveButton.setEnabled(this.signatureEnabled);
        fileOperationButtonPanel.add(saveButton);
        this.addLowerInteractiveElementBorder(fileOperationButtonPanel);
        gbc.gridy = 3;
        gbc.gridx = 0;
        this.addLowerInteractiveElementBorder(fileOperationButtonPanel);
        this.holderPanel.add(fileOperationButtonPanel, gbc);

//        third row:
        JPanel cancelApplyButtonRow = new JPanel();
        cancelApplyButtonRow.setLayout(new BoxLayout(cancelApplyButtonRow, BoxLayout.LINE_AXIS));
        JButton applyButton = this.createButton(
                APPLY, "Apply",
                "Apply current known signature to all Evee proofs");
        cancelApplyButtonRow.add(applyButton);
        cancelApplyButtonRow.add(Box.createHorizontalGlue());
        JButton cancelButton = this.createButton(
                CANCEL, "Cancel",
                "Cancel without saving any changes to the known signature");
        cancelApplyButtonRow.add(cancelButton);
        this.addLowerInteractiveElementBorder(cancelApplyButtonRow);
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        this.holderPanel.add(cancelApplyButtonRow, gbc);
    }

    private void addLowerInteractiveElementBorder(JComponent component){
        component.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setToolTipText(toolTip);
        newButton.setActionCommand(actionCommand);
        newButton.addActionListener(this);
        return newButton;
    }

    private void load(){
        SwingUtilities.invokeLater(() -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.getOWLEditorKit());
                signatureFileHandler.loadFile();
                boolean enableSignature = signatureFileHandler.getUseSignature();
                Collection<OWLEntity> knownEntitySet = signatureFileHandler.getSignature();
                this.signatureSelectionUI.enableSignature(enableSignature);
                this.useSignatureCheckBox.setSelected(enableSignature);
                this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
                this.signatureSelectionUI.clearSelectedSignatureUISelection();
            } catch (IOException e) {
//                error-message already shown in SignatureFileHandler
                this.signatureSelectionUI.dispose();
                this.dialog.dispose();
            } catch(LoadingAbortedException ignored){
//                no handling necessary
            }
        });
    }

    private void save(){
        SwingUtilities.invokeLater(() -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.getOWLEditorKit());
                //        note: getOntologyIRI().isPresent() was checked earlier during UI-creation
                signatureFileHandler.setUseSignature(this.useSignatureCheckBox.isSelected());
                signatureFileHandler.setSignature(this.signatureSelectionUI.getSelectedSignature());
                signatureFileHandler.saveSignature();
                this.signatureSelectionUI.clearSelectedSignatureUISelection();
            } catch (IOException e){
//                error-message already shown in SignatureFileHandler
                this.signatureSelectionUI.dispose();
                this.dialog.dispose();
            }
        });
    }

    private void cancel(){
        SwingUtilities.invokeLater(() -> {
            this.dialog.dispose();
            this.signatureSelectionUI.dispose();
        });
    }

    private void apply(){
        SwingUtilities.invokeLater(() -> {
//            getOntologyIRI().isPresent() should have been checked at UI-creation
            assert(this.activeOntology.getOntologyID().getOntologyIRI().isPresent());
            String ontologyName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
            try{
                this.signaturePreferencesManager.saveSignature(ontologyName,
                        this.useSignatureCheckBox.isSelected(),
                        this.signatureSelectionUI.getSelectedSignature());
//                this.signaturePreferencesManager.saveKnownSignature(ontologyName,
//                        this.signatureSelectionUI.getSelectedSignature());
//                this.signaturePreferencesManager.saveUseSignature(ontologyName,
//                        this.useSignatureCheckBox.isSelected());
            }
            catch (IllegalArgumentException e){
                this.logger.error("Error while saving signature to Protege Preferences.", e);
                String errorString = "<center>" + e + "</center>";
                UIUtilities.showError(SIGNATURE_SAVING_ERROR_MSG + errorString, this.getOWLEditorKit());
            }
            finally{
                this.dialog.dispose();
                this.signatureSelectionUI.dispose();
            }
        });
    }

//    private void showError(String message){
//        SwingUtilities.invokeLater(() -> {
//            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
//            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()), "Error");
//            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
//            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
//                    ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace())));
//            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//            errorDialog.setVisible(true);
//        });
//    }

    private void enableButtons(boolean enable){
        this.loadButton.setEnabled(enable);
        this.saveButton.setEnabled(enable);
        this.signatureSelectionUI.enableSignature(enable);
//        this.signatureSelectionUI.resetSelectedSignatureList();
        this.signatureEnabled = enable;
    }

    public boolean isSignatureEnabled(){
        return this.signatureEnabled;
    }

}
