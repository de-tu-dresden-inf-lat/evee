package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeUIKnownSignaturePreferenceManager;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.*;
import java.util.List;

// todo: rename to make distinguishable from ProofSignatureSelectionUI
public class EveeDynamicProofKnownSignatureSelectionUI extends ProtegeOWLAction implements ActionListener {

    private static final String INIT = "Manage signature";
    private static final String LOAD = "LOAD_SIGNATURE";
    private static final String SAVE = "SAVE_SIGNATURE";
    private static final String CANCEL = "CANCEL_SIGNATURE_SELECTION";
    private static final String APPLY = "APPLY_SIGNATURE";
    private static final String USE_SIGNATURE_DELIMITER = "##### Use Signature: #####";
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String CLASSES_DELIMITER = "##### Classes: #####";
    private static final String OBJECT_PROPERTIES_DELIMITER = "##### Object Properties: #####";
    private static final String INDIVIDUAL_DELIMITER = "##### Individuals: #####";
    private static final String ANONYMOUS_ONTOLOGY_ERROR_MSG = "<html><center>Ontology has no IRI.</center><center>Changes to the signature are only allowed if the ontology has an IRI.</center>";
    private static final String SIGNATURE_SAVING_ERROR_MSG = "<html><center>Error while saving signature</center><center>The IRI of the active Ontology is too long to use a signature.</center>";
//    todo: improve wording
    private final String topLabelText = "<html><center>Any proof step that contains only those OWL Entities in the right list will <b>not</b> be explained in any Evee proof.</center><center>This will also be considered when optimizing the Evee proofs.</center>";
    private final Insets insets = new Insets(5, 5, 5, 5);
    private JDialog dialog;
    private JPanel holderPanel;
    private JButton loadButton;
    private JButton saveButton;
    private JCheckBox useSignatureCheckBox;
    private final EveeUIKnownSignaturePreferenceManager signaturePreferencesManager;
    private ProofSignatureSelectionUI signatureSelectionUI;
    private OWLOntology activeOntology;
    private boolean signatureEnabled;
    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofKnownSignatureSelectionUI.class);

    public EveeDynamicProofKnownSignatureSelectionUI(){
        this.signaturePreferencesManager = new EveeUIKnownSignaturePreferenceManager();
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
            this.showError(ANONYMOUS_ONTOLOGY_ERROR_MSG);
            return;
        }
        String ontoName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
        SwingUtilities.invokeLater(() -> {
            this.signatureSelectionUI = new ProofSignatureSelectionUI(this);
            this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
            this.dialog = new JDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()));
            this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.dialog.setTitle("Manage signature for " + ontoName);
            this.holderPanel = new JPanel();
            this.dialog.getContentPane().add(holderPanel);
            this.holderPanel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.insets = this.insets;
//            gbc.anchor = GridBagConstraints.CENTER;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            gbc.weightx = 0.1;
//            gbc.weighty = 0.1;
//            JPanel topLabelPanel = this.createTopLabel();
//            this.holderPanel.add(topLabelPanel, gbc);
            this.addTopLabel();
            this.addSignaturePanelComponents();
//            gbc.gridy = 1;
//            gbc.fill = GridBagConstraints.BOTH;
//            gbc.weightx = 0.5;
//            gbc.weighty = 0.5;
//            this.holderPanel.add(signaturePanels, gbc);
            this.addMiddleButtons();
            this.addLowerInteractiveElements();
//            JPanel buttonPanel = this.addLowerInteractiveElements();
//            gbc.gridy = 2;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            gbc.weightx = 0.1;
//            gbc.weighty = 0.1;
//            this.holderPanel.add(buttonPanel, gbc);
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
        Set<OWLEntity> knownEntitySet = this.signaturePreferencesManager.getKnownSignatureForUI(
                this.activeOntology,
                this.activeOntology.getOntologyID().getOntologyIRI().get().toString());
//        if (knownEntitySet.size() == 0){
//            OWLEntity top = this.getOWLDataFactory().getOWLThing();
//            OWLEntity bot = this.getOWLDataFactory().getOWLNothing();
//            knownEntitySet.add(top);
//            knownEntitySet.add(bot);
//        }
        this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
        JPanel ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedPanel();
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
//        JPanel signatureSelectionHolderPanel = new JPanel();
//        signatureSelectionHolderPanel.setLayout(new BoxLayout(signatureSelectionHolderPanel, BoxLayout.LINE_AXIS));
//        signatureSelectionHolderPanel.add(ontologySignaturePanel);
//        signatureSelectionHolderPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        JPanel selectedSignaturePanel = this.signatureSelectionUI.getSelectedSignatureListPanel();
//        signatureSelectionHolderPanel.add(selectedSignaturePanel);
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
//        getOntologyIRI().isPresent() was checked earlier during UI-creation
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
                LOAD, "Load from file",
                "Load a signature from a file");
        fileOperationButtonPanel.add(loadButton);
        fileOperationButtonPanel.add(Box.createGlue());
        this.saveButton = this.createButton(
                SAVE, "Save to file",
                "Save a signature to a file");
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
        this.logger.debug("Loading known signature form file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this.dialog);
        List<IRI> classes = new ArrayList<>();
        List<IRI> objectProperties = new ArrayList<>();
        List<IRI> individuals = new ArrayList<>();
        List<IRI> currentList = classes;
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)){
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    this.logger.debug("line:" + line);
                    switch (line) {
                        case USE_SIGNATURE_DELIMITER:
                            break;
                        case TRUE:
                            this.useSignatureCheckBox.setSelected(true);
                            this.enableButtons(true);
                            this.logger.debug("UseSignature activated");
                            break;
                        case FALSE:
                            this.useSignatureCheckBox.setSelected(false);
                            this.enableButtons(false);
                            this.logger.debug("UseSignature deactivated");
                            break;
                        case CLASSES_DELIMITER:
                            this.logger.debug("loading classes");
                            currentList = classes;
                            break;
                        case OBJECT_PROPERTIES_DELIMITER:
                            this.logger.debug("loading object properties");
                            currentList = objectProperties;
                            break;
                        case INDIVIDUAL_DELIMITER:
                            this.logger.debug("loading individuals");
                            currentList = individuals;
                            break;
                        default:
                            currentList.add(IRI.create(line));
                            break;
                    }
                }
            }
            catch (IOException e){
                this.logger.error("Error when loading from file: ", e);
                this.signatureSelectionUI.dispose();
                this.dialog.dispose();
                this.showError("Error: " + e);
            }
        }
        if (classes.size() == 0 && objectProperties.size() == 0 && individuals.size() == 0){
            return;
        }
        Set<OWLEntity> knownEntitySet = new HashSet<>();
        this.activeOntology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            if (classes.contains(owlClass.getIRI())){
                knownEntitySet.add(owlClass);
            }});
        this.activeOntology.getObjectPropertiesInSignature(Imports.INCLUDED).forEach(objectProperty -> {
            if (objectProperties.contains(objectProperty.getIRI())){
                knownEntitySet.add(objectProperty);
            }});
        this.activeOntology.getIndividualsInSignature(Imports.INCLUDED).forEach(individual -> {
            if (individuals.contains(individual.getIRI())){
                knownEntitySet.add(individual);
            }});
//        classes.forEach(iri ->
//                knownEntitySet.addAll(
//                        this.activeOntology.getEntitiesInSignature(
//                                iri)));
//        ontologyEntitySet.removeAll(knownEntitySet);
        this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
        this.signatureSelectionUI.clearSelectedSignatureUISelection();
    }

    private void save(){
        ArrayList<OWLEntity> classes = new ArrayList<>();
        ArrayList<OWLEntity> objectProperties = new ArrayList<>();
        ArrayList<OWLEntity> individuals = new ArrayList<>();
        this.signatureSelectionUI.getSelectedSignature().forEach(owlEntity -> {
            if (owlEntity.isOWLClass()){
                classes.add(owlEntity);
            }
            else if (owlEntity.isOWLObjectProperty()){
                objectProperties.add(owlEntity);
            }
            else if (owlEntity.isOWLNamedIndividual()){
                individuals.add(owlEntity);
            }
        });
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this.dialog);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
                bufferedWriter.write(USE_SIGNATURE_DELIMITER + "\n");
                if (this.signaturePreferencesManager.loadUseSignature(
                        this.activeOntology.getOntologyID().getOntologyIRI().get().toString())){
                    bufferedWriter.write(TRUE + "\n");
                }
                else {
                    bufferedWriter.write(FALSE + "\n");
                }
                bufferedWriter.write(CLASSES_DELIMITER + "\n");
                for (OWLEntity entity : classes){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
                bufferedWriter.write(OBJECT_PROPERTIES_DELIMITER + "\n");
                for (OWLEntity entity : objectProperties){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
                bufferedWriter.write(INDIVIDUAL_DELIMITER + "\n");
                for (OWLEntity entity : individuals){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
            }
            catch (IOException e){
                this.logger.error("Error when saving to file: ", e);
                this.signatureSelectionUI.dispose();
                this.dialog.dispose();
                this.showError("Error: " + e);
            }
        }
        this.signatureSelectionUI.clearSelectedSignatureUISelection();
    }

    private JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
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
                this.signaturePreferencesManager.saveKnownSignature(ontologyName,
                        this.signatureSelectionUI.getSelectedSignature());
                this.signaturePreferencesManager.saveUseSignature(ontologyName,
                        this.useSignatureCheckBox.isSelected());
            }
            catch (IllegalArgumentException e){
                this.logger.error("Error while saving signature to Protege Preferences: Ontology-IRI + Delimiters is too long.");
                this.logger.error(e.toString());
                this.showError(SIGNATURE_SAVING_ERROR_MSG);
            }
            finally{
                this.dialog.dispose();
                this.signatureSelectionUI.dispose();
            }
        });
    }

    private void showError(String message){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace())));
            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            errorDialog.setVisible(true);
        });
    }

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
