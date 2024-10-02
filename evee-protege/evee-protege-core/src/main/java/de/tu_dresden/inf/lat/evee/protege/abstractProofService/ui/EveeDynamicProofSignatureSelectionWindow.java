package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeProofSignatureUIPreferenceManager;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.LoadingAbortedException;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.SignatureFileHandler;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.renderer.ProtegeTreeNodeRenderer;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.protege.editor.owl.ui.tree.OWLObjectTreeRootNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.*;

public class EveeDynamicProofSignatureSelectionWindow extends ProtegeOWLAction implements ActionListener {

    protected JPanel selectedSignaturePanel;
    protected JTabbedPane signatureTabPane;
    protected OWLObjectTree<OWLClass> classesTree;
    protected OWLObjectTree<OWLObjectProperty> propertyTree;
    protected OWLObjectListModel<OWLNamedIndividual> ontologyIndividualsListModel;
    protected JList<OWLNamedIndividual> ontologyIndividualsJList;
    protected OWLObjectListModel<OWLEntity> selectedSignatureListModel;
    protected JList<OWLEntity> selectedSignatureJList;
    protected JButton addButton;
    protected final String ADD_BTN_COMMAND = "ADD_TO_SIGNATURE";
    protected String ADD_BTN_NAME = ">";
    protected String ADD_BTN_TOOLTIP = "Add selected entries to known signature";
    protected JButton deleteButton;
    protected final String DEL_BTN_COMMAND = "DELETE_FROM_SIGNATURE";
    protected String DEL_BTN_NAME = "<";
    protected String DEL_BTN_TOOLTIP = "Delete selected entries from known signature";
    protected JButton clearButton;
    protected final String CLR_BTN_COMMAND = "CLEAR_SIGNATURE";
    protected String CLR_BTN_NAME = "Reset";
    protected String CLR_BTN_TOOLTIP = "Remove all entries from known signature except owl:Thing and owl:Nothing";
    private static final String INIT = "Manage signature";
    private static final String LOAD = "LOAD_SIGNATURE";
    private static final String LOAD_NAME = "Load from file";
    private static final String LOAD_TOOLTIP = "Load a signature from a file";
    private static final String SAVE = "SAVE_SIGNATURE";
    private static final String SAVE_NAME = "Save to file";
    private static final String SAVE_TOOLTIP = "Save a signature to a file";
    private static final String CANCEL = "CANCEL_SIGNATURE_SELECTION";
    private static final String APPLY = "APPLY_SIGNATURE";
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
    private OWLOntology activeOntology;
    private boolean signatureEnabled;
    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofSignatureSelectionWindow.class);

    public EveeDynamicProofSignatureSelectionWindow(){
        this.signaturePreferencesManager = new EveeProofSignatureUIPreferenceManager();
    }

    @Override
    public void initialise(){
    }

    @Override
    public void dispose(){
        if (this.classesTree != null){
            this.classesTree.dispose();
        }
        if (this.propertyTree != null){
            this.propertyTree.dispose();
        }
        this.ontologyIndividualsListModel.dispose();
        this.selectedSignatureListModel.dispose();
        this.dialog.dispose();
    }

//  directly UI related
    private void createUI(){
        if (this.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI().isPresent()){
            this.activeOntology = this.getOWLModelManager().getActiveOntology();
        }
        else{
            UIUtilities.showError(ANONYMOUS_ONTOLOGY_ERROR_MSG, this.getOWLEditorKit());
            return;
        }
        SwingUtilities.invokeLater(() -> {
            this.createOntologySignatureTabbedPanel();
            this.createButtons();
            this.createSelectedSignatureListPane();
            this.createDialog();
            this.addTopLabel();
            this.addSignaturePanelComponents();
            this.addMiddleButtons();
            this.addLowerInteractiveElements();
            UIUtilities.packAndSetWindow(this.dialog, this.getOWLEditorKit(), true);
        });
    }

    private void createOntologySignatureTabbedPanel(){
        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.setPreferredSize(new Dimension(400, 400));
//        todo: highlighting keywords for classes + properties necessary? see method "initialiseView" in Protege's "AbstractOWLEntityHierarchyViewComponent"
//        classes
        this.classesTree = new OWLModelManagerTree<>(
                this.getOWLEditorKit(),
                this.getOWLEditorKit().getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        JScrollPane classesPane = new JScrollPane(this.classesTree);
        classesPane.getViewport().setBackground(Color.WHITE);
        this.classesTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.getOWLEditorKit()));
        this.classesTree.setOWLObjectComparator(this.getOWLEditorKit().getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Classes", classesPane);
//        object properties
        this.propertyTree = new OWLModelManagerTree<>(
                this.getOWLEditorKit(),
                this.getOWLEditorKit().getOWLModelManager().getOWLHierarchyManager().getOWLObjectPropertyHierarchyProvider());
        JScrollPane propertyPane = new JScrollPane(this.propertyTree);
        propertyPane.getViewport().setBackground(Color.WHITE);
        this.propertyTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.getOWLEditorKit()));
        this.propertyTree.setOWLObjectComparator(this.getOWLEditorKit().getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Object properties", propertyPane);
//        individuals
        this.ontologyIndividualsListModel = new OWLObjectListModel<>(this.getOWLEditorKit());
        this.ontologyIndividualsJList = new JList<>(this.ontologyIndividualsListModel);
        this.ontologyIndividualsJList.setCellRenderer(new OWLCellRendererSimple(this.getOWLEditorKit()));
        Set<OWLNamedIndividual> individuals = this.getOWLEditorKit().getOWLModelManager().getActiveOntology().getIndividualsInSignature(Imports.INCLUDED);
        this.ontologyIndividualsListModel.addElements(individuals);
        tabbedPane.addTab("Individuals", this.ontologyIndividualsJList);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Ontology vocabulary:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        tabbedPane.setPreferredSize(new Dimension(400, 600));
        this.signatureTabPane = tabbedPane;
        OWLEntity bot = this.getOWLEditorKit().getModelManager().getOWLDataFactory().getOWLNothing();
        OWLObjectTreeNode<OWLClass> newNode = new OWLObjectTreeNode<>(
                bot, this.classesTree);
        DefaultMutableTreeNode parentNode = ((DefaultMutableTreeNode) ((OWLObjectTreeRootNode<OWLClass>) this.classesTree.getModel().getRoot()).getFirstChild());
        ((DefaultTreeModel) this.classesTree.getModel()).insertNodeInto(
                newNode, parentNode, 0);
    }

    private void createButtons(){
        this.addButton = UIUtilities.createNamedButton(this.ADD_BTN_COMMAND, this.ADD_BTN_NAME, this.ADD_BTN_TOOLTIP, this);
        this.deleteButton = UIUtilities.createNamedButton(this.DEL_BTN_COMMAND, this.DEL_BTN_NAME, this.DEL_BTN_TOOLTIP, this);
        this.clearButton = UIUtilities.createNamedButton(this.CLR_BTN_COMMAND, this.CLR_BTN_NAME, this.CLR_BTN_TOOLTIP, this);
    }

    private void createSelectedSignatureListPane(){
        this.selectedSignaturePanel = new JPanel();
        this.selectedSignaturePanel.setLayout(new BoxLayout(this.selectedSignaturePanel, BoxLayout.PAGE_AXIS));
        this.selectedSignaturePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Known vocabulary:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.selectedSignaturePanel.setPreferredSize(new Dimension(400, 600));
        this.selectedSignatureListModel = new OWLObjectListModel<>(this.getOWLEditorKit());
        this.selectedSignatureJList = new JList<>(this.selectedSignatureListModel);
        this.selectedSignatureJList.setCellRenderer(new OWLCellRendererSimple(this.getOWLEditorKit()));
        JScrollPane scrollPane = new JScrollPane(this.selectedSignatureJList);
        scrollPane.getViewport().setBackground(Color.WHITE);
//        scrollPane.setPreferredSize(new Dimension(400, 400));
        this.selectedSignaturePanel.add(scrollPane);
    }

    private void createDialog(){
        String ontoName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
        this.dialog = new JDialog(ProtegeManager.getInstance().getFrame(this.getEditorKit().getWorkspace()));
        this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.setTitle("Manage vocabulary for " + ontoName);
        this.holderPanel = new JPanel();
        this.dialog.getContentPane().add(holderPanel);
        this.holderPanel.setLayout(new GridBagLayout());
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
        this.selectedSignatureListModel.removeAll();
        this.selectedSignatureListModel.addElements(knownEntitySet);
        this.selectedSignatureJList.clearSelection();
        boolean useSignature = this.signaturePreferencesManager.getUseSignatureForUI(ontologyName);
        this.enableSignature(useSignature);
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
        this.holderPanel.add(this.signatureTabPane, gbc);
        JPanel selectedSignaturePanel = this.selectedSignaturePanel;
        gbc.gridx = 2;
        this.holderPanel.add(selectedSignaturePanel, gbc);
    }

    private void addMiddleButtons(){
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.PAGE_AXIS));
        toolBarPanel.add(Box.createGlue());
        toolBarPanel.add(this.addButton);
        toolBarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        toolBarPanel.add(this.deleteButton);
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
        JLabel checkBoxLabel = new JLabel("Use vocabulary for proofs:");
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
        clearPanel.add(this.clearButton);
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

    private void enableButtons(boolean enable){
        this.loadButton.setEnabled(enable);
        this.saveButton.setEnabled(enable);
        this.enableSignature(enable);
        this.signatureEnabled = enable;
    }

    public void enableSignature(boolean enable){
        this.addButton.setEnabled(enable);
        this.deleteButton.setEnabled(enable);
        this.clearButton.setEnabled(enable);
        this.selectedSignatureJList.setEnabled(enable);
    }

//    ActionListener implementation
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
            case ADD_BTN_COMMAND:
                this.addAction();
                break;
            case DEL_BTN_COMMAND:
                this.deleteAction();
                break;
            case CLR_BTN_COMMAND:
                this.clearAction();
                break;
        }
    }

    private void load(){
        SwingUtilities.invokeLater(() -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.getOWLEditorKit());
                signatureFileHandler.loadFile();
                boolean enableSignature = signatureFileHandler.getUseSignature();
                Collection<OWLEntity> knownEntitySet = signatureFileHandler.getSignature();
                this.enableSignature(enableSignature);
                this.useSignatureCheckBox.setSelected(enableSignature);
                this.selectedSignatureListModel.removeAll();
                this.selectedSignatureListModel.addElements(knownEntitySet);
                this.selectedSignatureJList.clearSelection();
                this.selectedSignatureJList.clearSelection();
            } catch (IOException e) {
//                error-message already shown and logged in SignatureFileHandler
                this.dispose();
            } catch(LoadingAbortedException ignored){
//                no handling necessary, logging already done in SignatureFileHandler
            }
        });
    }

    private void save(){
        SwingUtilities.invokeLater(() -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.getOWLEditorKit());
                //        note: getOntologyIRI().isPresent() was checked earlier during UI-creation
                signatureFileHandler.setUseSignature(this.useSignatureCheckBox.isSelected());
                signatureFileHandler.setSignature(this.selectedSignatureListModel.getOwlObjects());
                signatureFileHandler.saveSignature();
                this.selectedSignatureJList.clearSelection();
            } catch (IOException e){
//                error-message already shown and logged in SignatureFileHandler
                this.dispose();
            }
        });
    }

    private void cancel(){
        SwingUtilities.invokeLater(this::dispose);
    }

    private void apply(){
        SwingUtilities.invokeLater(() -> {
//            getOntologyIRI().isPresent() should have been checked at UI-creation
            assert(this.activeOntology.getOntologyID().getOntologyIRI().isPresent());
            String ontologyName = this.activeOntology.getOntologyID().getOntologyIRI().get().toString();
            try{
                this.signaturePreferencesManager.saveSignature(ontologyName,
                        this.useSignatureCheckBox.isSelected(),
                        this.selectedSignatureListModel.getOwlObjects());
//                this.signaturePreferencesManager.saveKnownSignature(ontologyName,
//                        this.signatureSelectionUI.getSelectedSignature());
//                this.signaturePreferencesManager.saveUseSignature(ontologyName,
//                        this.useSignatureCheckBox.isSelected());
            }
            catch (IllegalArgumentException e){
                this.logger.error("Error while saving signature to Protege Preferences: ", e);
                String errorString = "<center>" + e + "</center>";
                UIUtilities.showError(SIGNATURE_SAVING_ERROR_MSG + errorString, this.getOWLEditorKit());
            }
            finally{
                this.dispose();
            }
        });
    }

    private void addAction(){
        SwingUtilities.invokeLater(() -> {
            int tabIndex = this.signatureTabPane.getSelectedIndex();
            if (tabIndex == 0){
                java.util.List<OWLClass> entitiesToAdd = this.classesTree.getSelectedOWLObjects();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.classesTree.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
            else if (tabIndex == 1){
                java.util.List<OWLObjectProperty> entitiesToAdd = this.propertyTree.getSelectedOWLObjects();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.propertyTree.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
            else{
                java.util.List<OWLNamedIndividual> entitiesToAdd = this.ontologyIndividualsJList.getSelectedValuesList();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.ontologyIndividualsJList.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
        });
    }

    private void deleteAction(){
        SwingUtilities.invokeLater(() -> {
            java.util.List<OWLEntity> entitiesToDelete = this.selectedSignatureJList.getSelectedValuesList();
            this.selectedSignatureListModel.removeElements(entitiesToDelete);
            this.selectedSignatureJList.clearSelection();
        });
    }

    protected void clearAction(){
        SwingUtilities.invokeLater(() -> {
            this.selectedSignatureListModel.removeAll();
            ArrayList<OWLEntity> helperList = new ArrayList<>();
            helperList.add(this.getOWLDataFactory().getOWLThing());
            helperList.add(this.getOWLDataFactory().getOWLNothing());
            this.selectedSignatureListModel.addElements(helperList);
            this.selectedSignatureJList.clearSelection();
        });
    }

}
