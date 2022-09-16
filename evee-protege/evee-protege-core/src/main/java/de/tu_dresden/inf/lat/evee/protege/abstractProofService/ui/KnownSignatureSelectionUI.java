package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeKnownSignaturePreferencesManager;
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
public class KnownSignatureSelectionUI extends ProtegeOWLAction implements ActionListener {

    private static final String INIT = "Manage signature";
    private static final String LOAD = "LOAD_SIGNATURE";
    private static final String SAVE = "SAVE_SIGNATURE";
    private static final String CANCEL = "CANCEL_SIGNATURE_SELECTION";
    private static final String APPLY = "APPLY_SIGNATURE";
    private static final String CLASSES_DELIMITER = "##### Classes: #####";
    private static final String OBJECT_PROPERTIES_DELIMITER = "##### Object Properties: #####";
    private static final String INDIVIDUAL_DELIMITER = "##### Individuals: #####";
    private static final String ANONYMOUS_ONTOLOGY_ERROR_MSG = "<html><center>Ontology has no IRI.</center><center>Changes to the signature are only allowed if the ontology has an IRI.</center>";
//    todo: improve wording
    private final String topLabelText = "<html><center>Any proof step that contains only those OWL Entities in the right list will <b>not</b> be explained in any Evee proof.</center><center>This will also be considered when optimizing the Evee proofs.</center>";
    private final Insets insets = new Insets(5, 5, 5, 5);
    private JDialog dialog;
    private JPanel holderPanel;
    private JButton loadButton;
    private JButton saveButton;
//    private JCheckBox useSignatureCheckBox;
    private final EveeKnownSignaturePreferencesManager signaturePreferencesManager;
    private ProofSignatureSelectionUI signatureSelectionUI;
    private OWLOntology activeOntology;
    private final Logger logger = LoggerFactory.getLogger(KnownSignatureSelectionUI.class);

    public KnownSignatureSelectionUI(){
        this.signaturePreferencesManager = new EveeKnownSignaturePreferencesManager();
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
            this.signatureSelectionUI = new ProofSignatureSelectionUI(this.getOWLEditorKit());
            this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
            this.dialog = new JDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()));
            this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.dialog.setTitle("Manage signature for " + ontoName);
            this.holderPanel = new JPanel();
            this.dialog.getContentPane().add(holderPanel);
            this.holderPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.insets = this.insets;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.1;
            gbc.weighty = 0.1;
            JPanel topLabelPanel = this.createTopLabel();
            this.holderPanel.add(topLabelPanel, gbc);
            JPanel signaturePanels = this.createSignaturePanelComponent();
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.5;
            gbc.weighty = 0.5;
            this.holderPanel.add(signaturePanels, gbc);
            JPanel buttonPanel = this.createButtonPanel();
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.1;
            gbc.weighty = 0.1;
            this.holderPanel.add(buttonPanel, gbc);
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

    private JPanel createSignaturePanelComponent(){
        Set<OWLEntity> knownEntitySet = this.signaturePreferencesManager.loadKnownSignature(
                this.activeOntology, this.activeOntology.getOntologyID().getOntologyIRI().get().toString());
        if (knownEntitySet.size() == 0){
            OWLEntity top = this.getOWLDataFactory().getOWLThing();
            OWLEntity bot = this.getOWLDataFactory().getOWLNothing();
            knownEntitySet.add(top);
            knownEntitySet.add(bot);
        }
        this.signatureSelectionUI.setSelectedSignature(knownEntitySet);
        JPanel ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedPanel();
        JPanel signatureSelectionHolderPanel = new JPanel();
        signatureSelectionHolderPanel.setLayout(new BoxLayout(signatureSelectionHolderPanel, BoxLayout.LINE_AXIS));
        signatureSelectionHolderPanel.add(ontologySignaturePanel);
        signatureSelectionHolderPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        JPanel selectedSignaturePanel = this.signatureSelectionUI.getSelectedSignatureListPanel();
        signatureSelectionHolderPanel.add(selectedSignaturePanel);
        return signatureSelectionHolderPanel;
    }

    private JPanel createButtonPanel(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
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
        JCheckBox useSignatureCheckBox = new JCheckBox();
        useSignatureCheckBox.setSelected(true);
        useSignatureCheckBox.addItemListener(e -> enableButtons(e.getStateChange() == ItemEvent.SELECTED));
        checkBoxPanel.add(useSignatureCheckBox);
        buttonPanel.add(checkBoxPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));

//        second row:
        JPanel fileOperationButtonPanel = new JPanel();
        fileOperationButtonPanel.setLayout(new BoxLayout(fileOperationButtonPanel, BoxLayout.LINE_AXIS));
        this.loadButton = this.createButton(
                LOAD, "Load from file",
                "Load a signature from a file");
        fileOperationButtonPanel.add(loadButton);
        fileOperationButtonPanel.add(Box.createRigidArea(new Dimension(25, 0)));
        this.saveButton = this.createButton(
                SAVE, "Save to file",
                "Save a signature to a file");
        fileOperationButtonPanel.add(saveButton);
        fileOperationButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel firstButtonRow = new JPanel();
        buttonPanel.add(fileOperationButtonPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));

//        third row:
        JPanel signatureSelectionButtonPanel = new JPanel();
        signatureSelectionButtonPanel.setLayout(new BoxLayout(signatureSelectionButtonPanel, BoxLayout.LINE_AXIS));
        signatureSelectionButtonPanel.add(this.signatureSelectionUI.getDeleteButton());
        signatureSelectionButtonPanel.add(Box.createRigidArea(new Dimension(25, 0)));
        signatureSelectionButtonPanel.add(this.signatureSelectionUI.getClearButton());
        signatureSelectionButtonPanel.add(Box.createRigidArea(new Dimension(25, 0)));
        signatureSelectionButtonPanel.add(this.signatureSelectionUI.getAddButton());
        signatureSelectionButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        buttonPanel.add(signatureSelectionButtonPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));

//        fourth row:
        JPanel cancelApplyButtonRow = new JPanel();
        cancelApplyButtonRow.setLayout(new BoxLayout(cancelApplyButtonRow, BoxLayout.LINE_AXIS));
        JButton cancelButton = this.createButton(
                CANCEL, "Cancel",
                "Cancel without saving any changes to the known signature");
        cancelApplyButtonRow.add(cancelButton);
        cancelApplyButtonRow.add(Box.createHorizontalGlue());
        JButton applyButton = this.createButton(
                APPLY, "Apply",
                "Apply current known signature to all Evee proofs");
        cancelApplyButtonRow.add(applyButton);
        cancelApplyButtonRow.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        buttonPanel.add(cancelApplyButtonRow);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setToolTipText(toolTip);
        newButton.setActionCommand(actionCommand);
        newButton.addActionListener(this);
        return newButton;
    }

    private void load(){
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
                        case CLASSES_DELIMITER:
                            this.logger.debug("switched to classes");
                            currentList = classes;
                            break;
                        case OBJECT_PROPERTIES_DELIMITER:
                            this.logger.debug("switched to object properties");
                            currentList = objectProperties;
                            break;
                        case INDIVIDUAL_DELIMITER:
                            this.logger.debug("switched to individuals");
                            currentList = individuals;
                            break;
                        default:
                            this.logger.debug("stuff added: " + IRI.create(line));
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
            this.signaturePreferencesManager.saveKnownSignature(
                    this.activeOntology.getOntologyID().getOntologyIRI().get().toString(),
                    this.signatureSelectionUI.getSelectedSignature());
            this.dialog.dispose();
            this.signatureSelectionUI.dispose();
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
        this.signatureSelectionUI.enableButtons(enable);
        this.signatureSelectionUI.resetSelectedSignatureList();
    }

}
