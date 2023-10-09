package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences.NonEntailmentGeneralPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.LoadingAbortedException;
import de.tu_dresden.inf.lat.evee.protege.tools.IO.SignatureFileHandler;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.renderer.ProtegeTreeNodeRenderer;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLEntityCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NonEntailmentVocabularySelectionUI implements ActionListener {

//    todo: handle calls to viewComponent via some listener
    private final NonEntailmentViewComponent nonEntailmentViewComponent;
    private final OWLEditorKit owlEditorKit;
    private final SignatureOWLModelChangeListener SignatureModelManagerListener;
    private final SignatureOntologyChangeListener SignatureOntologyChangeListener;
    private final NonEntailmentGeneralPreferencesManager preferencesManager;
    private JTabbedPane ontologySignatureTabbedPane;
    private OWLObjectTree<OWLClass> classesTree;
    private OWLObjectTree<OWLObjectProperty> propertyTree;
    private OWLObjectListModel<OWLNamedIndividual> ontologyIndividualsListModel;
    private JList<OWLNamedIndividual> ontologyIndividualsJList;
    private JTabbedPane vocabularyTabbedPane;
    private OWLObjectListModel<OWLEntity> permittedVocabularyListModel;
    private JList<OWLEntity> permittedVocabularyList;
    private OWLObjectListModel<OWLEntity> forbiddenVocabularyListModel;
    private JList<OWLEntity> forbiddenVocabularyList;
    private JPanel buttonHolderPanel;
    private static final String ADD_BTN_COMMAND = "ADD";
    private static final String ADD_BTN_ICON = "/DownArrow_Transparent.png";
    private static final String ADD_BTN_TOOLTIP = "Add selected OWLObjects to the vocabulary";
    private static final String ADD_ALL_BTN_COMMAND = "ADD_ALL";
    private static final String ADD_ALL_BTN_ICON = "/DoubleDownArrow_Transparent.png";
    private static final String ADD_ALL_BTN_TOOLTIP = "Add all entities to the vocabulary";
    private static final String DEL_BTN_COMMAND = "DELETE";
    private static final String DEL_BTN_ICON = "/UpArrow_Transparent.png";
    private static final String DEL_BTN_TOOLTIP = "Delete selected OWLObjects from the vocabulary";
    private static final String DEL_ALL_BTN_COMMAND = "DELETE_ALL";
    private static final String DEL_ALL_BTN_ICON = "/DoubleUpArrow_Transparent.png";
    private static final String DEL_ALL_BTN_TOOLTIP = "Delete all entities from the vocabulary";
    private static final String LOAD_SIGNATURE_COMMAND = "LOAD_SIGNATURE";
    private static final String LOAD_SIGNATURE_BUTTON_NAME = "Load permitted vocabulary";
    private static final String LOAD_SIGNATURE_BUTTON_TOOLTIP = "Load the permitted vocabulary from a file";
    private static final String SAVE_SIGNATURE_COMMAND = "SAVE_SIGNATURE";
    private static final String SAVE_SIGNATURE_BUTTON_NAME = "Save permitted vocabulary";
    private static final String SAVE_SIGNATURE_BUTTON_TOOLTIP = "Save the permitted vocabulary to a file";
    private static final String ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND = "ADD_MISSING_ENTAILMENT";
    private static final String ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME = "Add missing entailment vocabulary";
    private static final String ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP = "Adds vocabulary of missing entailment to the selected vocabulary tab";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentVocabularySelectionUI.class);



    private VocabularyTab tabIndex2Name(int tabIndex){
        if (tabIndex == 0){
            return VocabularyTab.Permitted;
        } else{
            return VocabularyTab.Forbidden;
        }
    }

    private VocabularyTab tabIndex2ComplementName(int tabIndex){
        return this.tabIndex2Name(1 - tabIndex);
    }

    public NonEntailmentVocabularySelectionUI(NonEntailmentViewComponent nonEntailmentViewComponent, OWLEditorKit editorKit){
        this.nonEntailmentViewComponent = nonEntailmentViewComponent;
        this.owlEditorKit = editorKit;
        this.SignatureModelManagerListener = new SignatureOWLModelChangeListener();
        this.owlEditorKit.getOWLModelManager().addListener(this.SignatureModelManagerListener);
        this.SignatureOntologyChangeListener = new SignatureOntologyChangeListener();
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(
                this.SignatureOntologyChangeListener);
        this.preferencesManager = new NonEntailmentGeneralPreferencesManager();
//        SwingUtilities.invokeLater(() -> {
            this.createOntologySignatureTabbedPane();
            this.createButtonHolderPanel();
            this.createSelectedVocabularyListPane();
//        });
    }

    private void createOntologySignatureTabbedPane(){
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(400, 400));
//        todo: highlighting keywords for classes + properties? see method "initialiseView" in Protege's "AbstractOWLEntityHierarchyViewComponent"
//        classes
        this.classesTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        JScrollPane classesPane = new JScrollPane(this.classesTree);
        classesPane.getViewport().setBackground(Color.WHITE);
        this.classesTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.classesTree.setOWLObjectComparator(this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Classes", classesPane);
//        object properties
        this.propertyTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager()
                        .getOWLObjectPropertyHierarchyProvider());
        JScrollPane propertyPane = new JScrollPane(this.propertyTree);
        propertyPane.getViewport().setBackground(Color.WHITE);
        this.propertyTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.propertyTree.setOWLObjectComparator(this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Object properties", propertyPane);
//        individuals
        this.ontologyIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.ontologyIndividualsJList = new JList<>(this.ontologyIndividualsListModel);
        this.ontologyIndividualsJList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        Set<OWLNamedIndividual> individuals = this.owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature(Imports.INCLUDED);
        this.ontologyIndividualsListModel.addElements(individuals);
        tabbedPane.addTab("Individuals", this.ontologyIndividualsJList);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Ontology vocabulary:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.ontologySignatureTabbedPane = tabbedPane;
    }

//    private void addBottomEntities2Trees(){
//        OWLEntity owlNothing = this.owlEditorKit.getModelManager().getOWLDataFactory().getOWLNothing();
//        OWLObjectTreeNode<OWLClass> owlNothingNode = new OWLObjectTreeNode<>(
//                owlNothing, this.classesTree);
//        DefaultMutableTreeNode owlThingNode = ((DefaultMutableTreeNode) (
//                (OWLObjectTreeRootNode<OWLClass>) this.classesTree.getModel().getRoot()).getFirstChild());
//        ((DefaultTreeModel) this.classesTree.getModel()).insertNodeInto(
//                owlNothingNode, owlThingNode, 0);
//        OWLEntity owlBotObjectProperty = this.owlEditorKit.getOWLModelManager().getOWLDataFactory()
//                .getOWLBottomObjectProperty();
//        OWLObjectTreeNode<OWLObjectProperty> owlBotObjectPropertyNode = new OWLObjectTreeNode<>(
//                owlBotObjectProperty, this.propertyTree);
//        DefaultMutableTreeNode owlTopObjectPropertyNode = ((DefaultMutableTreeNode) (
//                (OWLObjectTreeRootNode<OWLObjectProperty>) this.propertyTree.getModel().getRoot()).getFirstChild());
//        ((DefaultTreeModel) this.propertyTree.getModel()).insertNodeInto(
//                owlBotObjectPropertyNode, owlTopObjectPropertyNode, 0);
//    }

    private void removeTopAndBottomEntities(Collection<? extends OWLEntity> entities){
        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
        entities.remove(dataFactory.getOWLThing());
        entities.remove(dataFactory.getOWLNothing());
        entities.remove(dataFactory.getOWLTopObjectProperty());
        entities.remove(dataFactory.getOWLBottomObjectProperty());
    }

    private void createButtonHolderPanel(){
        this.buttonHolderPanel = new JPanel();
        this.buttonHolderPanel.setLayout(new BoxLayout(this.buttonHolderPanel, BoxLayout.PAGE_AXIS));
        this.buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        ArrayList<JButton> buttonList = new ArrayList<>();
        URL addUrl = getClass().getResource(ADD_BTN_ICON);
        JButton addButton = UIUtilities.createIconButton(ADD_BTN_COMMAND, addUrl,
                ADD_BTN_TOOLTIP, this);
        buttonList.add(addButton);
        URL delUrl = getClass().getResource(DEL_BTN_ICON);
        JButton deleteButton = UIUtilities.createIconButton(DEL_BTN_COMMAND, delUrl,
                DEL_BTN_TOOLTIP, this);
        buttonList.add(deleteButton);
        JPanel firstButtonRowPanel = this.createButtonPanel(buttonList);
        this.buttonHolderPanel.add(firstButtonRowPanel);
        this.buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonList.clear();
        URL addAllUrl = getClass().getResource(ADD_ALL_BTN_ICON);
        JButton addAllButton = UIUtilities.createIconButton(ADD_ALL_BTN_COMMAND, addAllUrl,
                ADD_ALL_BTN_TOOLTIP, this);
        buttonList.add(addAllButton);
        URL delAllUrl = getClass().getResource(DEL_ALL_BTN_ICON);
        JButton deleteAllButton = UIUtilities.createIconButton(DEL_ALL_BTN_COMMAND, delAllUrl,
                DEL_ALL_BTN_TOOLTIP, this);
        buttonList.add(deleteAllButton);
        JPanel secondButtonRowPanel = this.createButtonPanel(buttonList);
        this.buttonHolderPanel.add(secondButtonRowPanel);
        this.buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonList.clear();
        JButton addMissingEntailmentSignatureButton = UIUtilities.createNamedButton(ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND,
                ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME, ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP, this);
        buttonList.add(addMissingEntailmentSignatureButton);
        JPanel thirdButtonRowPanel = this.createButtonPanel(buttonList);
        this.buttonHolderPanel.add(thirdButtonRowPanel);
        this.buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonList.clear();
        JButton loadSignatureButton = UIUtilities.createNamedButton(LOAD_SIGNATURE_COMMAND,
                LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP, this);
        buttonList.add(loadSignatureButton);
        JButton saveSignatureButton = UIUtilities.createNamedButton(SAVE_SIGNATURE_COMMAND,
                SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP, this);
        buttonList.add(saveSignatureButton);
        JPanel fourthButtonRowPanel = this.createButtonPanel(buttonList);
        this.buttonHolderPanel.add(fourthButtonRowPanel);
    }

    private JPanel createButtonPanel(List<JButton> buttons){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(buttons.get(0));
        for (int idx = 1; idx < buttons.size(); idx++){
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPanel.add(buttons.get(idx));
        }
        return buttonPanel;
    }

    private void createSelectedVocabularyListPane(){
        this.permittedVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.permittedVocabularyList = new JList<>(this.permittedVocabularyListModel);
        this.permittedVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.forbiddenVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.forbiddenVocabularyList = new JList<>(this.forbiddenVocabularyListModel);
        this.forbiddenVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.resetVocabularyListModels();
        this.vocabularyTabbedPane = new JTabbedPane();
        this.vocabularyTabbedPane.setPreferredSize(new Dimension(400, 400));
        this.vocabularyTabbedPane.addTab("Permitted Vocabulary",
                ComponentFactory.createScrollPane(this.permittedVocabularyList));
        this.vocabularyTabbedPane.addTab("Forbidden Vocabulary",
                ComponentFactory.createScrollPane(this.forbiddenVocabularyList));
        this.vocabularyTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Vocabulary:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void resetVocabularyListModels(){
        this.permittedVocabularyListModel.removeAll();
        this.forbiddenVocabularyListModel.removeAll();
        if (this.preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
            this.permittedVocabularyListModel.addElements(this.getCompleteOntologySignature());
        } else{
            this.forbiddenVocabularyListModel.addElements(this.getCompleteOntologySignature());
        }
    }

    private Collection<OWLEntity> getCompleteOntologySignature(){
        Collection<OWLEntity> completeSignature = this.owlEditorKit.getOWLModelManager()
                .getActiveOntology().getSignature(Imports.INCLUDED)
                .stream().filter(ax ->
                        ax instanceof OWLClass || ax instanceof OWLObjectProperty ||
                                ax instanceof OWLNamedIndividual)
                .collect(Collectors.toSet());
        this.removeTopAndBottomEntities(completeSignature);
        return completeSignature;
    }

//    private void addTopAndBottomEntities2Collection(Collection<OWLEntity> collection){
//        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
//        if (! collection.contains(dataFactory.getOWLThing())){
//            collection.add(dataFactory.getOWLThing());
//        }
//        if (! collection.contains(dataFactory.getOWLNothing())){
//            collection.add(dataFactory.getOWLNothing());
//        }
//        if (! collection.contains(dataFactory.getOWLTopObjectProperty())){
//            collection.add(dataFactory.getOWLTopObjectProperty());
//        }
//        if (!collection.contains(dataFactory.getOWLBottomObjectProperty())) {
//            collection.add(dataFactory.getOWLBottomObjectProperty());
//        }
//    }

    public void dispose(OWLModelManager modelManager){
        if (this.classesTree != null){
            this.classesTree.dispose();
        }
        if (this.propertyTree != null){
            this.propertyTree.dispose();
        }
        if (this.ontologyIndividualsListModel != null){
            this.ontologyIndividualsListModel.dispose();
        }
        this.permittedVocabularyListModel.dispose();
        this.forbiddenVocabularyListModel.dispose();
        modelManager.removeListener(this.SignatureModelManagerListener);
        modelManager.removeOntologyChangeListener(this.SignatureOntologyChangeListener);
    }

    public JComponent getOntologySignatureTabbedComponent(){
        return this.ontologySignatureTabbedPane;
    }

    public JComponent getSelectedVocabularyPanel(){
        return this.vocabularyTabbedPane;
    }

    public JComponent getSignatureSelectionButtonPanel(){
        return this.buttonHolderPanel;
    }

    public Collection<OWLEntity> getPermittedVocabulary(){
        return this.permittedVocabularyListModel.getOwlObjects();
    }

//    ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ADD_BTN_COMMAND:
                this.addAction();
                break;
            case DEL_BTN_COMMAND:
                this.deleteAction();
                break;
            case ADD_ALL_BTN_COMMAND:
                this.addAllAction();
                break;
            case DEL_ALL_BTN_COMMAND:
                this.deleteAllAction();
                break;
            case LOAD_SIGNATURE_COMMAND:
                this.loadSignatureAction();
                break;
            case SAVE_SIGNATURE_COMMAND:
                this.saveSignatureAction();
                break;
            case ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND:
                this.addMissingEntailmentSignatureAction();
                break;
        }
    }

    private void addAction(){
        SwingUtilities.invokeLater(() -> {
            int ontologySignatureTabIndex = this.ontologySignatureTabbedPane.getSelectedIndex();
            List<? extends OWLEntity> entitiesToAdd;
            if (ontologySignatureTabIndex == 0){
                entitiesToAdd = this.classesTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else if (ontologySignatureTabIndex == 1){
                entitiesToAdd = this.propertyTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else{
                entitiesToAdd = this.ontologyIndividualsJList.getSelectedValuesList();
            }
            int selectedVocabularyTabIndex = this.vocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(entitiesToAdd,
                    this.tabIndex2Name(selectedVocabularyTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void addAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.tabIndex2Name(this.vocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void deleteAction(){
        SwingUtilities.invokeLater(() -> {
            int deleteFromTabIndex = this.vocabularyTabbedPane.getSelectedIndex();
            List<OWLEntity> entitiesToDelete;
            if (deleteFromTabIndex == 0){
                entitiesToDelete = this.permittedVocabularyList.getSelectedValuesList();
            } else{
                entitiesToDelete = this.forbiddenVocabularyList.getSelectedValuesList();
            }
//            deleting entities from one list = adding entities to other list
            this.moveEntities2VocabularyList(entitiesToDelete,
                    this.tabIndex2ComplementName(deleteFromTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void deleteAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.tabIndex2ComplementName(this.vocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void addMissingEntailmentSignatureAction(){
        SwingUtilities.invokeLater(() -> {
            ArrayList<OWLObject> missingEntailmentList = this.nonEntailmentViewComponent.getMissingEntailments();
            HashSet<OWLEntity> missingEntailmentSet = new HashSet<>();
            missingEntailmentList.forEach(missingEntailment -> missingEntailmentSet.addAll(missingEntailment.getSignature()));
            this.removeTopAndBottomEntities(missingEntailmentSet);
//            deleting entities from one list = adding entities to other list
            int tabIndex = this.vocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(missingEntailmentSet,
                    this.tabIndex2Name(tabIndex));
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void moveEntities2VocabularyList(Collection<? extends OWLEntity> entitiesToAdd, VocabularyTab addToTab){
        OWLObjectListModel<OWLEntity> listToAdd;
        OWLObjectListModel<OWLEntity> listToDelete;
        if (addToTab == VocabularyTab.Permitted){
            listToAdd = this.permittedVocabularyListModel;
            listToDelete = this.forbiddenVocabularyListModel;
        } else{
            listToAdd = this.forbiddenVocabularyListModel;
            listToDelete = this.permittedVocabularyListModel;
        }
        listToAdd.checkAndAddElements(entitiesToAdd);
        listToDelete.removeElements(entitiesToAdd);
    }

    private void clearVocabularySelection(){
        this.classesTree.clearSelection();
        this.propertyTree.clearSelection();
        this.ontologyIndividualsJList.clearSelection();
        this.permittedVocabularyList.clearSelection();
        this.forbiddenVocabularyList.clearSelection();
    }

    private void addAll2VocabularyList(VocabularyTab addToTab){
        this.moveEntities2VocabularyList(this.getCompleteOntologySignature(), addToTab);
    }

    private void loadSignatureAction(){
        SwingUtilities.invokeLater(() -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.owlEditorKit);
                signatureFileHandler.loadFile();
                Set<OWLEntity> knownEntitySet = new HashSet<>(signatureFileHandler.getSignature());
                this.removeTopAndBottomEntities(knownEntitySet);
//                deleting from one list = adding to other list
                this.addAll2VocabularyList(VocabularyTab.Forbidden);
                this.moveEntities2VocabularyList(knownEntitySet, VocabularyTab.Permitted);
                this.clearVocabularySelection();
//                this.vocabularyTabbedPane.setSelectedIndex(0);
            } catch (IOException ignored){
//                error-message already shown in SignatureFileHandler
            } catch (LoadingAbortedException ignored){
//                no handling necessary
            }
        });
    }

    private void saveSignatureAction(){
        SwingUtilities.invokeLater( () -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.owlEditorKit);
                signatureFileHandler.setSignature(this.permittedVocabularyListModel.getOwlObjects());
                signatureFileHandler.setUseSignature(true);
                signatureFileHandler.saveSignature();
                this.clearVocabularySelection();
            } catch (IOException ignored){
//                error-message already shown in SignatureFileHandler
            }
        });
    }

    private class SignatureOWLModelChangeListener implements OWLModelManagerListener {

        private SignatureOWLModelChangeListener(){

        }

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || changeEvent.isType(EventType.ONTOLOGY_RELOADED)){
//                    ontology signature component:
                    ontologyIndividualsListModel.removeAll();
                    ontologyIndividualsListModel.addElements(
                            owlEditorKit.getOWLModelManager().getActiveOntology()
                                    .getIndividualsInSignature(Imports.INCLUDED));
                    ontologyIndividualsJList.clearSelection();
//                    selected vocabulary component:
                    resetVocabularyListModels();
                    clearVocabularySelection();
                }
            });
        }
    }

    private class SignatureOntologyChangeListener implements OWLOntologyChangeListener {

        private SignatureOntologyChangeListener(){
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> ontologyChanges) throws OWLException {
            SwingUtilities.invokeLater(() -> {
                boolean changeForActiveOntology = false;
                for (OWLOntologyChange change: ontologyChanges){
                    if (change.getOntology().equals(owlEditorKit.getOWLModelManager().getActiveOntology())){
                        changeForActiveOntology = true;
                        break;
                    }
                }
                if (! changeForActiveOntology){
                    return;
                }
//                ontology signature component:
                ontologyIndividualsListModel.removeAll();
                ontologyIndividualsListModel.addElements(
                        owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature(
                                Imports.INCLUDED));
                ontologyIndividualsJList.clearSelection();
//                selected vocabulary component:
//                deleting individuals removed from vocabulary (permitted and forbidden):
                Set<OWLEntity> deletedEntities = new HashSet<>();
                OWLEntityCollector deletedEntityCollector = new OWLEntityCollector(deletedEntities);
                ontologyChanges.stream().filter(OWLOntologyChange::isRemoveAxiom).forEach(
                        removedAxiom -> removedAxiom.getAxiom().accept(deletedEntityCollector));
                Set<OWLEntity> entitiesToDelete = new HashSet<>();
                for (OWLEntity deletedEntity : deletedEntities){
                    if (! owlEditorKit.getOWLModelManager().getActiveOntology()
                            .containsEntityInSignature(deletedEntity)){
                        entitiesToDelete.add(deletedEntity);
                    }
                }
                permittedVocabularyListModel.removeElements(entitiesToDelete);
                forbiddenVocabularyListModel.removeElements(entitiesToDelete);
//                adding new entities to default vocabulary list:
                OWLObjectListModel<OWLEntity> listToAdd;
                OWLObjectListModel<OWLEntity> listToCheck;
                if (preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
                    listToAdd = permittedVocabularyListModel;
                    listToCheck = forbiddenVocabularyListModel;
                } else{
                    listToAdd = forbiddenVocabularyListModel;
                    listToCheck = permittedVocabularyListModel;
                }
                OWLOntology activeOntology = owlEditorKit.getOWLModelManager().getActiveOntology();
                Set<OWLEntity> entitiesToAdd = new HashSet<>(activeOntology
                        .getClassesInSignature(Imports.INCLUDED));
                entitiesToAdd.addAll(activeOntology
                        .getObjectPropertiesInSignature(Imports.INCLUDED));
                entitiesToAdd.addAll(activeOntology
                        .getIndividualsInSignature(Imports.INCLUDED));
                entitiesToAdd = entitiesToAdd.stream().filter(entity ->
                        ! listToCheck.getOwlObjects().contains(entity))
                        .collect(Collectors.toSet());
                removeTopAndBottomEntities(entitiesToAdd);
                listToAdd.checkAndAddElements(entitiesToAdd);
            });
        }
    }

}
