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
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.ceil;
import static java.util.Collections.max;

public class NonEntailmentVocabularySelectionUI implements ActionListener {

//    todo: handle calls to viewComponent via some listener
    private final NonEntailmentViewComponent nonEntailmentViewComponent;
    private final OWLEditorKit owlEditorKit;
    private final SignatureOWLModelChangeListener SignatureModelManagerListener;
    private final SignatureOntologyChangeListener SignatureOntologyChangeListener;
    private final NonEntailmentGeneralPreferencesManager preferencesManager;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);

//    standard layout UI-elements
    private JTabbedPane standardLayoutOntologySignatureTabbedPane;
    private OWLObjectTree<OWLClass> standardLayoutClassesTree;
    private OWLObjectTree<OWLObjectProperty> standardLayoutPropertyTree;
    private OWLObjectListModel<OWLNamedIndividual> standardLayoutOntologyIndividualsListModel;
    private JList<OWLNamedIndividual> standardLayoutOntologyIndividualsJList;
    private JTabbedPane standardLayoutVocabularyTabbedPane;
    private OWLObjectListModel<OWLEntity> standardLayoutPermittedVocabularyListModel;
    private JList<OWLEntity> standardLayoutPermittedVocabularyList;
    private OWLObjectListModel<OWLEntity> standardLayoutForbiddenVocabularyListModel;
    private JList<OWLEntity> standardLayoutForbiddenVocabularyList;
    private JPanel standardLayoutButtonHolderPanel;
    private JPanel standardVocabularyManagementPanel;
    private final List<Dimension> standardLayoutWideComponentDimensionList;

//   standard layout button elements
    private static final String STANDARD_ADD_BTN_COMMAND = "STANDARD_ADD";
    private static final String STANDARD_ADD_BTN_TOOLTIP = "Add selected OWLObjects to the vocabulary";
    private static final String STANDARD_ADD_ALL_BTN_COMMAND = "STANDARD_ADD_ALL";
    private static final String STANDARD_ADD_ALL_BTN_TOOLTIP = "Add all entities to the vocabulary";
    private static final String STANDARD_DEL_BTN_COMMAND = "STANDARD_DELETE";
    private static final String STANDARD_DEL_BTN_TOOLTIP = "Delete selected OWLObjects from the vocabulary";
    private static final String STANDARD_DEL_ALL_BTN_COMMAND = "STANDARD_DELETE_ALL";
    private static final String STANDARD_DEL_ALL_BTN_TOOLTIP = "Delete all entities from the vocabulary";
    private static final String STANDARD_LOAD_SIGNATURE_COMMAND = "STANDARD_LOAD_SIGNATURE";
    private static final String STANDARD_SAVE_SIGNATURE_COMMAND = "STANDARD_SAVE_SIGNATURE";
    private static final String STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND =
            "STANDARD_ADD_MISSING_ENTAILMENT";
    private static final String STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME =
            "Add missing entailment vocabulary";
    private static final String ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP =
            "Adds vocabulary of missing entailment to the selected vocabulary tab";

//    alternative layout UI-elements
    private JPanel alternativeLayoutMiddleButtonHolderPanel;
    private JPanel alternativeLayoutBottomButtonHolderPanel;
    private OWLObjectListModel<OWLClass> alternativeLayoutPermittedClassesListModel;
    private OWLObjectListModel<OWLObjectProperty> alternativeLayoutPermittedPropertiesListModel;
    private OWLObjectListModel<OWLNamedIndividual> alternativeLayoutPermittedIndividualsListModel;
    private OWLObjectListModel<OWLClass> alternativeLayoutForbiddenClassesListModel;
    private OWLObjectListModel<OWLObjectProperty> alternativeLayoutForbiddenPropertiesListModel;
    private OWLObjectListModel<OWLNamedIndividual> alternativeLayoutForbiddenIndividualsListModel;
    private JList<OWLClass> alternativeLayoutPermittedClassesList;
    private JList<OWLClass> alternativeLayoutForbiddenClassesList;
    private JList<OWLObjectProperty> alternativeLayoutPermittedPropertiesList;
    private JList<OWLObjectProperty> alternativeLayoutForbiddenPropertiesList;
    private JList<OWLNamedIndividual> alternativeLayoutPermittedIndividualsList;
    private JList<OWLNamedIndividual> alternativeLayoutForbiddenIndividualsList;
    private JTabbedPane alternativeLayoutPermittedSignatureTabbedPane;
    private JTabbedPane alternativeLayoutForbiddenSignatureTabbedPane;
    private JPanel alternativeVocabularyManagementPanel;
    private final List<Dimension> alternativeLayoutWideComponentDimensionList;

//   alternative layout button elements
    private static final String ALTERNATIVE_ADD_BTN_COMMAND = "ALTERNATIVE_ADD";
    private static final String ALTERNATIVE_ADD_BTN_TOOLTIP = "Add selected OWLObjects to the forbidden vocabulary";
    private static final String ALTERNATIVE_ADD_ALL_BTN_COMMAND = "ALTERNATIVE_ADD_ALL";
    private static final String ALTERNATIVE_ADD_ALL_BTN_TOOLTIP = "Add all entities to the forbidden vocabulary";
    private static final String ALTERNATIVE_DEL_BTN_COMMAND = "ALTERNATIVE_DELETE";
    private static final String ALTERNATIVE_DEL_BTN_TOOLTIP = "Delete selected OWLObjects from the forbidden vocabulary";
    private static final String ALTERNATIVE_DEL_ALL_BTN_COMMAND = "ALTERNATIVE_DELETE_ALL";
    private static final String ALTERNATIVE_DEL_ALL_BTN_TOOLTIP = "Delete all entities from the forbidden vocabulary";
    private static final String ALTERNATIVE_LOAD_SIGNATURE_COMMAND = "ALTERNATIVE_LOAD_SIGNATURE";
    private static final String ALTERNATIVE_SAVE_SIGNATURE_COMMAND = "ALTERNATIVE_SAVE_SIGNATURE";
    private static final String ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND =
            "ALTERNATIVE_ADD_MISSING_ENTAILMENT";
    private static final String ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME =
            "Forbid missing entailment vocabulary";
    private static final String ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP =
            "Adds vocabulary of missing entailment to the forbidden vocabulary";

//    general button elements
    private static final String ADD_BTN_ICON = "/DownArrow_Transparent_Big_Mid.png";
    private static final String ADD_ALL_BTN_ICON = "/DoubleDownArrow_Transparent.png";
    private static final String DEL_BTN_ICON = "/UpArrow_Transparent_Big_Mid.png";
    private static final String DEL_ALL_BTN_ICON = "/DoubleUpArrow_Transparent.png";
    private static final String LOAD_SIGNATURE_BUTTON_NAME = "Load permitted vocabulary";
    private static final String LOAD_SIGNATURE_BUTTON_TOOLTIP = "Load the permitted vocabulary from a file";
    private static final String SAVE_SIGNATURE_BUTTON_NAME = "Save permitted vocabulary";
    private static final String SAVE_SIGNATURE_BUTTON_TOOLTIP = "Save the permitted vocabulary to a file";


    private final Logger logger = LoggerFactory.getLogger(NonEntailmentVocabularySelectionUI.class);



    private VocabularyTab standardLayoutTabIndex2Name(int tabIndex){
        if (tabIndex == 0){
            return VocabularyTab.Permitted;
        } else{
            return VocabularyTab.Forbidden;
        }
    }

    private VocabularyTab tabIndex2ComplementName(int tabIndex){
        return this.standardLayoutTabIndex2Name(1 - tabIndex);
    }

    public NonEntailmentVocabularySelectionUI(NonEntailmentViewComponent nonEntailmentViewComponent, OWLEditorKit editorKit){
        this.nonEntailmentViewComponent = nonEntailmentViewComponent;
        this.owlEditorKit = editorKit;
        this.SignatureModelManagerListener = new SignatureOWLModelChangeListener();
        this.owlEditorKit.getOWLModelManager().addListener(this.SignatureModelManagerListener);
        this.SignatureOntologyChangeListener = new SignatureOntologyChangeListener();
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(
                this.SignatureOntologyChangeListener);
        this.preferencesManager = NonEntailmentGeneralPreferencesManager.getInstance();
        this.standardLayoutWideComponentDimensionList = new ArrayList<>();
        this.alternativeLayoutWideComponentDimensionList = new ArrayList<>();
//        SwingUtilities.invokeLater(() -> {
            this.createStandardLayoutComponents();
            this.createAlternativeLayoutComponents();
            this.resetVocabularyListModels();
//        });
    }

    public JPanel getSignatureManagementComponent(){
        if (this.preferencesManager.loadSignatureComponentLayout().equals(
                NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
            return this.standardVocabularyManagementPanel;
        } else {
            return this.alternativeVocabularyManagementPanel;
        }
    }

    private void resetVocabularyListModels(){
//        reset standard layout models:
        this.standardLayoutPermittedVocabularyListModel.removeAll();
        this.standardLayoutForbiddenVocabularyListModel.removeAll();
        if (this.preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
            this.standardLayoutPermittedVocabularyListModel.addElements(this.getCompleteOntologySignature());
        } else{
            this.standardLayoutForbiddenVocabularyListModel.addElements(this.getCompleteOntologySignature());
        }
//        reset alternative layout models:
        this.alternativeLayoutPermittedClassesListModel.removeAll();
        this.alternativeLayoutForbiddenClassesListModel.removeAll();
        this.alternativeLayoutPermittedPropertiesListModel.removeAll();
        this.alternativeLayoutForbiddenPropertiesListModel.removeAll();
        this.alternativeLayoutPermittedIndividualsListModel.removeAll();
        this.alternativeLayoutForbiddenIndividualsListModel.removeAll();
        Set<OWLClass> classSignature = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getClassesInSignature();
        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
        classSignature.remove(dataFactory.getOWLThing());
        classSignature.remove(dataFactory.getOWLNothing());
        this.alternativeLayoutPermittedClassesListModel.checkAndAddElements(classSignature);
        Set<OWLObjectProperty> propertySignature = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getObjectPropertiesInSignature();
        propertySignature.remove(dataFactory.getOWLTopObjectProperty());
        propertySignature.remove(dataFactory.getOWLBottomObjectProperty());
        this.alternativeLayoutPermittedPropertiesListModel.checkAndAddElements(propertySignature);
        this.alternativeLayoutPermittedIndividualsListModel.checkAndAddElements(
                this.owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature());
        this.clearVocabularySelection();
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

    public void dispose(OWLModelManager modelManager){
        if (this.standardLayoutClassesTree != null){
            this.standardLayoutClassesTree.dispose();
        }
        if (this.standardLayoutPropertyTree != null){
            this.standardLayoutPropertyTree.dispose();
        }
        if (this.standardLayoutOntologyIndividualsListModel != null){
            this.standardLayoutOntologyIndividualsListModel.dispose();
        }
        this.standardLayoutPermittedVocabularyListModel.dispose();
        this.standardLayoutForbiddenVocabularyListModel.dispose();
        this.alternativeLayoutPermittedClassesListModel.dispose();
        this.alternativeLayoutForbiddenClassesListModel.dispose();
        this.alternativeLayoutPermittedPropertiesListModel.dispose();
        this.alternativeLayoutForbiddenPropertiesListModel.dispose();
        this.alternativeLayoutPermittedIndividualsListModel.dispose();
        this.alternativeLayoutForbiddenIndividualsListModel.dispose();
        modelManager.removeListener(this.SignatureModelManagerListener);
        modelManager.removeOntologyChangeListener(this.SignatureOntologyChangeListener);

    }

    public Collection<OWLEntity> getPermittedVocabulary(){
        if (this.preferencesManager.loadSignatureComponentLayout().equals(
                NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
            return this.standardLayoutPermittedVocabularyListModel.getOwlObjects();
        } else {
            ArrayList<OWLEntity> result = new ArrayList<>();
            result.addAll(this.alternativeLayoutPermittedClassesListModel.getOwlObjects());
            result.addAll(this.alternativeLayoutPermittedPropertiesListModel.getOwlObjects());
            result.addAll(this.alternativeLayoutPermittedIndividualsListModel.getOwlObjects());
            return result;
        }
    }

//    standard layout element creation
    private void createStandardLayoutComponents(){
        this.createStandardLayoutOntologySignatureTabbedPane();
        this.createStandardLayoutButtonHolderPanel();
        this.createStandardLayoutSelectedVocabularyListPane();
        this.createStandardVocabularyManagementPanel();
    }
    private void createStandardLayoutOntologySignatureTabbedPane(){
        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.setPreferredSize(new Dimension(400, 400));
//        todo: highlighting keywords for classes + properties? see method "initialiseView" in Protege's "AbstractOWLEntityHierarchyViewComponent"
//        classes
        this.standardLayoutClassesTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        JScrollPane classesPane = new JScrollPane(this.standardLayoutClassesTree);
        classesPane.getViewport().setBackground(Color.WHITE);
        this.standardLayoutClassesTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.standardLayoutClassesTree.setOWLObjectComparator(
                this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Classes", classesPane);
//        object properties
        this.standardLayoutPropertyTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager()
                        .getOWLObjectPropertyHierarchyProvider());
        JScrollPane propertyPane = new JScrollPane(this.standardLayoutPropertyTree);
        propertyPane.getViewport().setBackground(Color.WHITE);
        this.standardLayoutPropertyTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.standardLayoutPropertyTree.setOWLObjectComparator(
                this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Object properties", propertyPane);
//        individuals
        this.standardLayoutOntologyIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutOntologyIndividualsJList = new JList<>(this.standardLayoutOntologyIndividualsListModel);
        this.standardLayoutOntologyIndividualsJList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        Set<OWLNamedIndividual> individuals = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getIndividualsInSignature(Imports.INCLUDED);
        this.standardLayoutOntologyIndividualsListModel.addElements(individuals);
        tabbedPane.addTab("Individuals", this.standardLayoutOntologyIndividualsJList);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),"Ontology vocabulary:");
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(titledBorder,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int upperComponentTitleWidth = (int) titledBorder.getMinimumSize(tabbedPane).getWidth() + 5;
        int upperComponentTitleHeight = (int) titledBorder.getMinimumSize(tabbedPane).getHeight() + 5;
        this.standardLayoutWideComponentDimensionList.add(new Dimension(
                upperComponentTitleWidth, upperComponentTitleHeight));
        this.standardLayoutOntologySignatureTabbedPane = tabbedPane;
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

    private void createStandardLayoutButtonHolderPanel(){
        this.standardLayoutButtonHolderPanel = new JPanel();
        this.standardLayoutButtonHolderPanel.setLayout(
                new BoxLayout(this.standardLayoutButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.standardLayoutButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        ArrayList<JButton> buttonList = new ArrayList<>();
        URL addUrl = getClass().getResource(ADD_BTN_ICON);
        JButton addButton = UIUtilities.createIconButton(STANDARD_ADD_BTN_COMMAND, addUrl,
                STANDARD_ADD_BTN_TOOLTIP, this);
        buttonList.add(addButton);
        URL delUrl = getClass().getResource(DEL_BTN_ICON);
        JButton deleteButton = UIUtilities.createIconButton(STANDARD_DEL_BTN_COMMAND, delUrl,
                STANDARD_DEL_BTN_TOOLTIP, this);
        buttonList.add(deleteButton);
        JPanel firstButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
        this.standardLayoutButtonHolderPanel.add(firstButtonRowPanel);
        this.standardLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonList.clear();
        URL addAllUrl = getClass().getResource(ADD_ALL_BTN_ICON);
        JButton addAllButton = UIUtilities.createIconButton(STANDARD_ADD_ALL_BTN_COMMAND, addAllUrl,
                STANDARD_ADD_ALL_BTN_TOOLTIP, this);
        buttonList.add(addAllButton);
        URL delAllUrl = getClass().getResource(DEL_ALL_BTN_ICON);
        JButton deleteAllButton = UIUtilities.createIconButton(STANDARD_DEL_ALL_BTN_COMMAND, delAllUrl,
                STANDARD_DEL_ALL_BTN_TOOLTIP, this);
        buttonList.add(deleteAllButton);
        JPanel secondButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
        this.standardLayoutButtonHolderPanel.add(secondButtonRowPanel);
        buttonList.clear();
        if (! this.preferencesManager.loadUseSimpleMode()){
            this.standardLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JButton addMissingEntailmentSignatureButton = UIUtilities.createNamedButton(
                    STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND,
                    STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME,
                    ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP, this);
            buttonList.add(addMissingEntailmentSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(addMissingEntailmentSignatureButton.getMinimumSize());
            JPanel thirdButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
            this.standardLayoutButtonHolderPanel.add(thirdButtonRowPanel);
            this.standardLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            buttonList.clear();
            JButton loadSignatureButton = UIUtilities.createNamedButton(STANDARD_LOAD_SIGNATURE_COMMAND,
                    LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(loadSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(loadSignatureButton.getMinimumSize());
            JButton saveSignatureButton = UIUtilities.createNamedButton(STANDARD_SAVE_SIGNATURE_COMMAND,
                    SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(saveSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(saveSignatureButton.getMinimumSize());
            JPanel fourthButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.PAGE_AXIS);
            this.standardLayoutButtonHolderPanel.add(fourthButtonRowPanel);
        }
    }

    private JPanel createButtonPanelFromList(List<JButton> buttons, int orientation){
        if (! (orientation == BoxLayout.PAGE_AXIS || orientation == BoxLayout.LINE_AXIS)){
            orientation = BoxLayout.LINE_AXIS;
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, orientation));
        buttonPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        buttonPanel.add(buttons.get(0));
        for (int idx = 1; idx < buttons.size(); idx++){
            if (orientation == BoxLayout.PAGE_AXIS){
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            } else {
                buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            }
            buttonPanel.add(buttons.get(idx));
        }
        return buttonPanel;
    }

    private void createStandardLayoutSelectedVocabularyListPane(){
        this.standardLayoutPermittedVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutPermittedVocabularyList = new JList<>(this.standardLayoutPermittedVocabularyListModel);
        this.standardLayoutPermittedVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutForbiddenVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutForbiddenVocabularyList = new JList<>(this.standardLayoutForbiddenVocabularyListModel);
        this.standardLayoutForbiddenVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutVocabularyTabbedPane = new JTabbedPane();
//        this.vocabularyTabbedPane.setPreferredSize(new Dimension(400, 400));
        this.standardLayoutVocabularyTabbedPane.addTab("Permitted vocabulary",
                ComponentFactory.createScrollPane(this.standardLayoutPermittedVocabularyList));
        this.standardLayoutVocabularyTabbedPane.addTab("Forbidden vocabulary",
                ComponentFactory.createScrollPane(this.standardLayoutForbiddenVocabularyList));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "Vocabulary:");
        int titleWidth = (int) titledBorder.getMinimumSize(this.standardLayoutVocabularyTabbedPane).
                getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(this.standardLayoutVocabularyTabbedPane).
                getHeight() + 5;
        this.standardLayoutWideComponentDimensionList.add(new Dimension(titleWidth, titleHeight));
        this.standardLayoutVocabularyTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void createStandardVocabularyManagementPanel(){
        this.standardVocabularyManagementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for given signature tabbed pane:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.standardVocabularyManagementPanel.add(this.standardLayoutOntologySignatureTabbedPane, constraints);
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.standardVocabularyManagementPanel.add(this.standardLayoutButtonHolderPanel, constraints);
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.standardVocabularyManagementPanel.add(this.standardLayoutVocabularyTabbedPane, constraints);
        this.standardVocabularyManagementPanel.setMinimumSize(new Dimension(
//                width of each component
                (int) ceil(max(this.standardLayoutWideComponentDimensionList.stream().map(Dimension::getWidth).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of standardVocabularyManagementPanel
                        + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                height of each component
                (int) ceil(max(this.standardLayoutWideComponentDimensionList.stream().map(Dimension::getHeight).
                        collect(Collectors.toList())))
//                        component embedded in gridBagLayout of standardVocabularyManagementPanel
                        + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom

        ));
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


//    alternative layout element creation
    private void createAlternativeLayoutComponents(){
        this.createAlternativeLayoutUpperPanel();
        this.createAlternativeLayoutMiddleButtonPanel();
        this.createAlternativeLayoutLowerPanel();
        this.createAlternativeLayoutBottomButtonPanel();
        this.createAlternativeVocabularyManagementPanel();
        this.addAlternativeLayoutTabbedPaneStateChangeListeners();
    }
    private void createAlternativeLayoutUpperPanel() {
//        general
        this.alternativeLayoutPermittedSignatureTabbedPane = new JTabbedPane();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Permitted vocabulary:");
        this.alternativeLayoutPermittedSignatureTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int titleWidth = (int) titledBorder.getMinimumSize(
                this.alternativeLayoutPermittedSignatureTabbedPane).getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(
                this.alternativeLayoutPermittedSignatureTabbedPane).getHeight() + 5;
        this.alternativeLayoutWideComponentDimensionList.add(new Dimension(titleWidth,
                titleHeight));
//        classes
        this.alternativeLayoutPermittedClassesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutPermittedClassesList =
                new JList<>(this.alternativeLayoutPermittedClassesListModel);
        this.alternativeLayoutPermittedClassesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutPermittedSignatureTabbedPane.addTab("Classes",
                ComponentFactory.createScrollPane(this.alternativeLayoutPermittedClassesList));
//        properties
        this.alternativeLayoutPermittedPropertiesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutPermittedPropertiesList =
                new JList<>(this.alternativeLayoutPermittedPropertiesListModel);
        this.alternativeLayoutPermittedPropertiesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutPermittedSignatureTabbedPane.addTab("Object properties",
                ComponentFactory.createScrollPane(this.alternativeLayoutPermittedPropertiesList));
//        individuals
        this.alternativeLayoutPermittedIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutPermittedIndividualsList =
                new JList<>(this.alternativeLayoutPermittedIndividualsListModel);
        this.alternativeLayoutPermittedIndividualsList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutPermittedSignatureTabbedPane.addTab("Individuals",
                ComponentFactory.createScrollPane(this.alternativeLayoutPermittedIndividualsList));
    }

    private void createAlternativeLayoutLowerPanel() {
//        general
        this.alternativeLayoutForbiddenSignatureTabbedPane = new JTabbedPane();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Forbidden vocabulary:");
        this.alternativeLayoutForbiddenSignatureTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int lowerComponentTitleWidth = (int) titledBorder.getMinimumSize(
                this.alternativeLayoutForbiddenSignatureTabbedPane).getWidth();
        int lowerComponentTitleHeight = (int) titledBorder.getMinimumSize(
                this.alternativeLayoutForbiddenSignatureTabbedPane).getHeight();
        this.alternativeLayoutWideComponentDimensionList.add(new Dimension(lowerComponentTitleWidth,
                lowerComponentTitleHeight));
//        classes
        this.alternativeLayoutForbiddenClassesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutForbiddenClassesList = new JList<>(this.alternativeLayoutForbiddenClassesListModel);
        this.alternativeLayoutForbiddenClassesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutForbiddenSignatureTabbedPane.addTab("Classes:",
                ComponentFactory.createScrollPane(this.alternativeLayoutForbiddenClassesList));
//        properties
        this.alternativeLayoutForbiddenPropertiesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutForbiddenPropertiesList =
                new JList<>(this.alternativeLayoutForbiddenPropertiesListModel);
        this.alternativeLayoutForbiddenPropertiesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutForbiddenSignatureTabbedPane.addTab("Object properties",
                ComponentFactory.createScrollPane(this.alternativeLayoutForbiddenPropertiesList));
//        individuals
        this.alternativeLayoutForbiddenIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutForbiddenIndividualsList =
                new JList<>(this.alternativeLayoutForbiddenIndividualsListModel);
        this.alternativeLayoutForbiddenIndividualsList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutForbiddenSignatureTabbedPane.addTab("Individuals",
                ComponentFactory.createScrollPane(this.alternativeLayoutForbiddenIndividualsList));
    }

    private void createAlternativeLayoutMiddleButtonPanel(){
        this.alternativeLayoutMiddleButtonHolderPanel = new JPanel();
        this.alternativeLayoutMiddleButtonHolderPanel.setLayout(
                new BoxLayout(this.alternativeLayoutMiddleButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.alternativeLayoutMiddleButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        ArrayList<JButton> buttonList = new ArrayList<>();
        URL addUrl = getClass().getResource(ADD_BTN_ICON);
        JButton addButton = UIUtilities.createIconButton(ALTERNATIVE_ADD_BTN_COMMAND, addUrl,
                ALTERNATIVE_ADD_BTN_TOOLTIP, this);
        buttonList.add(addButton);
        URL delUrl = getClass().getResource(DEL_BTN_ICON);
        JButton deleteButton = UIUtilities.createIconButton(ALTERNATIVE_DEL_BTN_COMMAND, delUrl,
                ALTERNATIVE_DEL_BTN_TOOLTIP, this);
        buttonList.add(deleteButton);
        JPanel firstButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
        this.alternativeLayoutMiddleButtonHolderPanel.add(firstButtonRowPanel);
        this.alternativeLayoutMiddleButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonList.clear();
        URL addAllUrl = getClass().getResource(ADD_ALL_BTN_ICON);
        JButton addAllButton = UIUtilities.createIconButton(ALTERNATIVE_ADD_ALL_BTN_COMMAND, addAllUrl,
                ALTERNATIVE_ADD_ALL_BTN_TOOLTIP, this);
        buttonList.add(addAllButton);
        URL delAllUrl = getClass().getResource(DEL_ALL_BTN_ICON);
        JButton deleteAllButton = UIUtilities.createIconButton(ALTERNATIVE_DEL_ALL_BTN_COMMAND, delAllUrl,
                ALTERNATIVE_DEL_ALL_BTN_TOOLTIP, this);
        buttonList.add(deleteAllButton);
        JPanel secondButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
        this.alternativeLayoutMiddleButtonHolderPanel.add(secondButtonRowPanel);
    }

    private void createAlternativeLayoutBottomButtonPanel(){
        this.alternativeLayoutBottomButtonHolderPanel = new JPanel();
        this.alternativeLayoutBottomButtonHolderPanel.setLayout(
                new BoxLayout(this.alternativeLayoutBottomButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.alternativeLayoutBottomButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        if (! this.preferencesManager.loadUseSimpleMode()){
            ArrayList<JButton> buttonList = new ArrayList<>();
            JButton addMissingEntailmentSignatureButton =
                    UIUtilities.createNamedButton(
                            ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND,
                            ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME,
                            ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP, this);
            buttonList.add(addMissingEntailmentSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(addMissingEntailmentSignatureButton.getMinimumSize());
            JPanel firstButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
            this.alternativeLayoutBottomButtonHolderPanel.add(firstButtonRowPanel);
            this.alternativeLayoutBottomButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            buttonList.clear();
            JButton loadSignatureButton = UIUtilities.createNamedButton(ALTERNATIVE_LOAD_SIGNATURE_COMMAND,
                    LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(loadSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(loadSignatureButton.getMinimumSize());
            JButton saveSignatureButton = UIUtilities.createNamedButton(ALTERNATIVE_SAVE_SIGNATURE_COMMAND,
                    SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(saveSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(saveSignatureButton.getMinimumSize());
            JPanel secondButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.PAGE_AXIS);
            this.alternativeLayoutBottomButtonHolderPanel.add(secondButtonRowPanel);
        }
    }

    private void createAlternativeVocabularyManagementPanel(){
        this.alternativeVocabularyManagementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for given signature tabbed pane:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutPermittedSignatureTabbedPane, constraints);
//        specific for middle buttons:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutMiddleButtonHolderPanel, constraints);
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutForbiddenSignatureTabbedPane, constraints);
//            specific for bottom buttons:
        constraints.gridy = 3;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutBottomButtonHolderPanel, constraints);
        this.alternativeVocabularyManagementPanel.setMinimumSize(new Dimension(
//                width of each component
                (int) ceil(max(this.alternativeLayoutWideComponentDimensionList.stream().map(Dimension::getWidth).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of alternativeVocabularyManagementPanel
                        + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                height of each component
                (int) ceil(max(this.alternativeLayoutWideComponentDimensionList.stream().map(Dimension::getHeight).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of alternativeVocabularyManagementPanel
                        + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom
        ));
    }

    private void addAlternativeLayoutTabbedPaneStateChangeListeners(){
        this.alternativeLayoutForbiddenSignatureTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                alternativeLayoutPermittedSignatureTabbedPane.setSelectedIndex(
                        alternativeLayoutForbiddenSignatureTabbedPane.getSelectedIndex());
            }
        });
        this.alternativeLayoutPermittedSignatureTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                alternativeLayoutForbiddenSignatureTabbedPane.setSelectedIndex(
                        alternativeLayoutPermittedSignatureTabbedPane.getSelectedIndex());
            }
        });
    }

//    ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case STANDARD_ADD_BTN_COMMAND:
                this.standardAddAction();
                break;
            case STANDARD_DEL_BTN_COMMAND:
                this.standardDeleteAction();
                break;
            case STANDARD_ADD_ALL_BTN_COMMAND:
                this.standardAddAllAction();
                break;
            case STANDARD_DEL_ALL_BTN_COMMAND:
                this.standardDeleteAllAction();
                break;
            case STANDARD_LOAD_SIGNATURE_COMMAND:
            case ALTERNATIVE_LOAD_SIGNATURE_COMMAND:
                this.loadSignatureAction();
                break;
            case STANDARD_SAVE_SIGNATURE_COMMAND:
            case ALTERNATIVE_SAVE_SIGNATURE_COMMAND:
                this.saveSignatureAction();
                break;
            case STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND:
                this.standardAddMissingEntailmentSignatureAction();
                break;
            case ALTERNATIVE_ADD_BTN_COMMAND:
                this.alternativeAddAction();
                break;
            case ALTERNATIVE_DEL_BTN_COMMAND:
                this.alternativeDeleteAction();
                break;
            case ALTERNATIVE_ADD_ALL_BTN_COMMAND:
                this.alternativeAddAllAction();
                break;
            case ALTERNATIVE_DEL_ALL_BTN_COMMAND:
                this.alternativeDeleteAllAction();
                break;
            case ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND:
                this.alternativeAddMissingEntailmentSignatureAction();
                break;
        }
    }

//    general button actions
private void loadSignatureAction(){
    SwingUtilities.invokeLater(() -> {
        try{
            SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.owlEditorKit);
            signatureFileHandler.loadFile();
            Set<OWLEntity> knownEntitySet = new HashSet<>(signatureFileHandler.getSignature());
            this.removeTopAndBottomEntities(knownEntitySet);
            if (this.preferencesManager.loadSignatureComponentLayout().equals(
                    NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
                //                deleting from one list = adding to other list
                this.addAll2VocabularyList(VocabularyTab.Forbidden);
                this.moveEntities2VocabularyList(knownEntitySet, VocabularyTab.Permitted);
                this.clearVocabularySelection();
//                this.vocabularyTabbedPane.setSelectedIndex(0);
            } else {
                for (OWLEntity entity : knownEntitySet){
                    if (entity instanceof OWLClass){
                        OWLClass owlClass = (OWLClass) entity;
                        this.alternativeLayoutPermittedClassesListModel.checkAndAddElement(owlClass);
                        this.alternativeLayoutForbiddenClassesListModel.removeElement(owlClass);
                    } else if (entity instanceof OWLObjectProperty){
                        OWLObjectProperty property = (OWLObjectProperty) entity;
                        this.alternativeLayoutPermittedPropertiesListModel.checkAndAddElement(property);
                        this.alternativeLayoutForbiddenPropertiesListModel.removeElement(property);
                    } else if (entity instanceof OWLNamedIndividual){
                        OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                        this.alternativeLayoutPermittedIndividualsListModel.checkAndAddElement(individual);
                        this.alternativeLayoutForbiddenIndividualsListModel.removeElement(individual);
                    }
                }
            }
        } catch (IOException ignored){
//                error-message already shown and logged in SignatureFileHandler
        } catch (LoadingAbortedException ignored){
//                no handling necessary, logging-message created in SignatureFileHandler
        }
    });
}

    private void saveSignatureAction(){
        SwingUtilities.invokeLater( () -> {
            try{
                SignatureFileHandler signatureFileHandler = new SignatureFileHandler(this.owlEditorKit);
                if (this.preferencesManager.loadSignatureComponentLayout().equals(
                        NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
                    signatureFileHandler.setSignature(this.standardLayoutPermittedVocabularyListModel.getOwlObjects());
                } else{
                    Set<OWLEntity> signature = new HashSet<>();
                    signature.addAll(this.alternativeLayoutPermittedClassesListModel.getOwlObjects());
                    signature.addAll(this.alternativeLayoutPermittedPropertiesListModel.getOwlObjects());
                    signature.addAll(this.alternativeLayoutPermittedIndividualsListModel.getOwlObjects());
                    signatureFileHandler.setSignature(signature);
                }
                signatureFileHandler.setUseSignature(true);
                signatureFileHandler.saveSignature();
                this.clearVocabularySelection();
            } catch (IOException ignored){
//                error-message already shown and logged in SignatureFileHandler
            }
        });
    }

//    standard layout button actions
    private void standardAddAction(){
        SwingUtilities.invokeLater(() -> {
            int ontologySignatureTabIndex = this.standardLayoutOntologySignatureTabbedPane.getSelectedIndex();
            List<? extends OWLEntity> entitiesToAdd;
            if (ontologySignatureTabIndex == 0){
                entitiesToAdd = this.standardLayoutClassesTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else if (ontologySignatureTabIndex == 1){
                entitiesToAdd = this.standardLayoutPropertyTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else{
                entitiesToAdd = this.standardLayoutOntologyIndividualsJList.getSelectedValuesList();
            }
            int selectedVocabularyTabIndex = this.standardLayoutVocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(entitiesToAdd,
                    this.standardLayoutTabIndex2Name(selectedVocabularyTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void standardAddAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.standardLayoutTabIndex2Name(this.standardLayoutVocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void standardDeleteAction(){
        SwingUtilities.invokeLater(() -> {
            int deleteFromTabIndex = this.standardLayoutVocabularyTabbedPane.getSelectedIndex();
            List<OWLEntity> entitiesToDelete;
            if (deleteFromTabIndex == 0){
                entitiesToDelete = this.standardLayoutPermittedVocabularyList.getSelectedValuesList();
            } else{
                entitiesToDelete = this.standardLayoutForbiddenVocabularyList.getSelectedValuesList();
            }
//            deleting entities from one list = adding entities to other list
            this.moveEntities2VocabularyList(entitiesToDelete,
                    this.tabIndex2ComplementName(deleteFromTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void standardDeleteAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.tabIndex2ComplementName(this.standardLayoutVocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void standardAddMissingEntailmentSignatureAction(){
        SwingUtilities.invokeLater(() -> {
            ArrayList<OWLObject> missingEntailmentList = this.nonEntailmentViewComponent.getMissingEntailments();
            HashSet<OWLEntity> missingEntailmentSet = new HashSet<>();
            missingEntailmentList.forEach(missingEntailment -> missingEntailmentSet.addAll(missingEntailment.getSignature()));
            this.removeTopAndBottomEntities(missingEntailmentSet);
//            deleting entities from one list = adding entities to other list
            int tabIndex = this.standardLayoutVocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(missingEntailmentSet,
                    this.standardLayoutTabIndex2Name(tabIndex));
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void moveEntities2VocabularyList(Collection<? extends OWLEntity> entitiesToAdd, VocabularyTab addToTab){
        OWLObjectListModel<OWLEntity> listToAdd;
        OWLObjectListModel<OWLEntity> listToDelete;
        if (addToTab == VocabularyTab.Permitted){
            listToAdd = this.standardLayoutPermittedVocabularyListModel;
            listToDelete = this.standardLayoutForbiddenVocabularyListModel;
        } else{
            listToAdd = this.standardLayoutForbiddenVocabularyListModel;
            listToDelete = this.standardLayoutPermittedVocabularyListModel;
        }
        listToAdd.checkAndAddElements(entitiesToAdd);
        listToDelete.removeElements(entitiesToAdd);
    }

    private void clearVocabularySelection(){
        this.standardLayoutClassesTree.clearSelection();
        this.standardLayoutPropertyTree.clearSelection();
        this.standardLayoutOntologyIndividualsJList.clearSelection();
        this.standardLayoutPermittedVocabularyList.clearSelection();
        this.standardLayoutForbiddenVocabularyList.clearSelection();
        this.alternativeLayoutPermittedClassesList.clearSelection();
        this.alternativeLayoutForbiddenClassesList.clearSelection();
        this.alternativeLayoutPermittedPropertiesList.clearSelection();
        this.alternativeLayoutForbiddenPropertiesList.clearSelection();
        this.alternativeLayoutPermittedIndividualsList.clearSelection();
        this.alternativeLayoutForbiddenIndividualsList.clearSelection();
    }

    private void addAll2VocabularyList(VocabularyTab addToTab){
        this.moveEntities2VocabularyList(this.getCompleteOntologySignature(), addToTab);
    }

//    alternative layout button actions
    private void alternativeAddAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.alternativeLayoutPermittedSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.alternativeLayoutPermittedClassesList.getSelectedValuesList());
                    this.alternativeLayoutPermittedClassesListModel.removeElements(classes);
                    this.alternativeLayoutForbiddenClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.alternativeLayoutPermittedPropertiesList.getSelectedValuesList());
                    this.alternativeLayoutPermittedPropertiesListModel.removeElements(properties);
                    this.alternativeLayoutForbiddenPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.alternativeLayoutPermittedIndividualsList.getSelectedValuesList());
                    this.alternativeLayoutPermittedIndividualsListModel.removeElements(individuals);
                    this.alternativeLayoutForbiddenIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void alternativeDeleteAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.alternativeLayoutForbiddenSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.alternativeLayoutForbiddenClassesList.getSelectedValuesList());
                    this.alternativeLayoutForbiddenClassesListModel.removeElements(classes);
                    this.alternativeLayoutPermittedClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.alternativeLayoutForbiddenPropertiesList.getSelectedValuesList());
                    this.alternativeLayoutForbiddenPropertiesListModel.removeElements(properties);
                    this.alternativeLayoutPermittedPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.alternativeLayoutForbiddenIndividualsList.getSelectedValuesList());
                    this.alternativeLayoutForbiddenIndividualsListModel.removeElements(individuals);
                    this.alternativeLayoutPermittedIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void alternativeAddAllAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.alternativeLayoutPermittedSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.alternativeLayoutPermittedClassesListModel.getOwlObjects());
                    this.alternativeLayoutPermittedClassesListModel.removeElements(classes);
                    this.alternativeLayoutForbiddenClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.alternativeLayoutPermittedPropertiesListModel.getOwlObjects());
                    this.alternativeLayoutPermittedPropertiesListModel.removeElements(properties);
                    this.alternativeLayoutForbiddenPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.alternativeLayoutPermittedIndividualsListModel.getOwlObjects());
                    this.alternativeLayoutPermittedIndividualsListModel.removeElements(individuals);
                    this.alternativeLayoutForbiddenIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void alternativeDeleteAllAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.alternativeLayoutForbiddenSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.alternativeLayoutForbiddenClassesListModel.getOwlObjects());
                    this.alternativeLayoutForbiddenClassesListModel.removeElements(classes);
                    this.alternativeLayoutPermittedClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.alternativeLayoutForbiddenPropertiesListModel.getOwlObjects());
                    this.alternativeLayoutForbiddenPropertiesListModel.removeElements(properties);
                    this.alternativeLayoutPermittedPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.alternativeLayoutForbiddenIndividualsListModel.getOwlObjects());
                    this.alternativeLayoutForbiddenIndividualsListModel.removeElements(individuals);
                    this.alternativeLayoutPermittedIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void alternativeAddMissingEntailmentSignatureAction() {
        SwingUtilities.invokeLater(() -> {
            ArrayList<OWLObject> missingEntailmentList = this.nonEntailmentViewComponent.getMissingEntailments();
            HashSet<OWLEntity> missingEntailmentSet = new HashSet<>();
            this.removeTopAndBottomEntities(missingEntailmentSet);
            missingEntailmentList.forEach(
                    missingEntailment -> missingEntailmentSet.addAll(missingEntailment.getSignature()));
            missingEntailmentSet.forEach(entity -> logger.debug(entity.toString()));
            missingEntailmentSet.forEach(entity -> {
                if (entity instanceof OWLClass){
                    OWLClass owlClass = (OWLClass) entity;
                    alternativeLayoutPermittedClassesListModel.removeElement(owlClass);
                    alternativeLayoutForbiddenClassesListModel.checkAndAddElement(owlClass);
                } else if (entity instanceof OWLObjectProperty){
                    OWLObjectProperty property = (OWLObjectProperty) entity;
                    alternativeLayoutPermittedPropertiesListModel.removeElement(property);
                    alternativeLayoutForbiddenPropertiesListModel.checkAndAddElement(property);
                } else if (entity instanceof OWLNamedIndividual){
                    OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                    alternativeLayoutPermittedIndividualsListModel.removeElement(individual);
                    alternativeLayoutForbiddenIndividualsListModel.checkAndAddElement(individual);
                }
            });
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    public void addNamesToSignature(Set<OWLEntity> newNames){
        newNames.forEach(name -> {
            if (name instanceof OWLClass){
                OWLClass newClassName = (OWLClass) name;
                if (! newClassName.isBottomEntity() && ! newClassName.isTopEntity()){
                    this.standardLayoutForbiddenVocabularyListModel.checkAndAddElement(newClassName);
                    this.standardLayoutPermittedVocabularyListModel.removeElement(newClassName);
                    this.alternativeLayoutForbiddenClassesListModel.checkAndAddElement(newClassName);
                    this.alternativeLayoutPermittedClassesListModel.removeElement(newClassName);
                }
            } else if (name instanceof OWLObjectProperty){
                OWLObjectProperty newPropertyName = (OWLObjectProperty) name;
                if (! newPropertyName.isBottomEntity() && ! newPropertyName.isTopEntity()){
                    this.standardLayoutForbiddenVocabularyListModel.checkAndAddElement(newPropertyName);
                    this.standardLayoutPermittedVocabularyListModel.removeElement(newPropertyName);
                    this.alternativeLayoutForbiddenPropertiesListModel.checkAndAddElement(newPropertyName);
                    this.alternativeLayoutPermittedPropertiesListModel.removeElement(newPropertyName);
                }
            } else if (name instanceof OWLNamedIndividual){
                OWLNamedIndividual newIndividualName = (OWLNamedIndividual) name;
                this.standardLayoutForbiddenVocabularyListModel.checkAndAddElement(newIndividualName);
                this.standardLayoutPermittedVocabularyListModel.removeElement(newIndividualName);
                this.alternativeLayoutForbiddenIndividualsListModel.checkAndAddElement(newIndividualName);
                this.alternativeLayoutPermittedIndividualsListModel.removeElement(newIndividualName);
            }
        });
        if (this.preferencesManager.loadSignatureComponentLayout().equals(
                NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
            this.standardLayoutVocabularyTabbedPane.setSelectedIndex(1);
        }
    }

    public void resetVocabularyManagementPanel() {
        this.alternativeLayoutWideComponentDimensionList.clear();
        this.createStandardLayoutComponents();
        this.standardLayoutWideComponentDimensionList.clear();
        this.createAlternativeLayoutComponents();
    }

    public void resetSelectedSignature(){
        this.resetVocabularyListModels();
    }

    private class SignatureOWLModelChangeListener implements OWLModelManagerListener {

        private SignatureOWLModelChangeListener(){

        }

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                        changeEvent.isType(EventType.ONTOLOGY_RELOADED)){
//                    ontology signature component:
                    standardLayoutOntologyIndividualsListModel.removeAll();
                    standardLayoutOntologyIndividualsListModel.addElements(
                            owlEditorKit.getOWLModelManager().getActiveOntology()
                                    .getIndividualsInSignature(Imports.INCLUDED));
                    standardLayoutOntologyIndividualsJList.clearSelection();
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
                Set<OWLEntity> possiblyDeletedEntities = new HashSet<>();
                Set<OWLEntity> entitiesToAdd = new HashSet<>();
                Set<OWLEntity> entitiesToDelete = new HashSet<>();
                OWLEntityCollector deletedEntitiesCollector = new OWLEntityCollector(possiblyDeletedEntities);
                OWLEntityCollector addedEntitiesCollector = new OWLEntityCollector(entitiesToAdd);
                ontologyChanges.forEach(owlOntologyChange -> {
                    if (owlOntologyChange instanceof RemoveAxiom){
                        owlOntologyChange.getAxiom().accept(deletedEntitiesCollector);
                    } else if (owlOntologyChange instanceof AddAxiom){
                        owlOntologyChange.getAxiom().accept(addedEntitiesCollector);
                    }
                });
                for (OWLEntity deletedEntity : possiblyDeletedEntities){
                    if (! owlEditorKit.getOWLModelManager().getActiveOntology()
                            .containsEntityInSignature(deletedEntity)){
                        entitiesToDelete.add(deletedEntity);
                    }
                }
                updateStandardLayoutListModels(entitiesToDelete);
                updateAlternativeLayoutListModels(entitiesToAdd, entitiesToDelete);
            });
        }

        private void updateStandardLayoutListModels(Set<OWLEntity> entitiesToDelete){
//               ontology signature component:
            standardLayoutOntologyIndividualsListModel.removeAll();
            standardLayoutOntologyIndividualsListModel.addElements(
                    owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature(
                            Imports.INCLUDED));
            standardLayoutOntologyIndividualsJList.clearSelection();
//               selected vocabulary component:
//                deleting individuals removed from vocabulary (permitted and forbidden):
            standardLayoutPermittedVocabularyListModel.removeElements(entitiesToDelete);
            standardLayoutForbiddenVocabularyListModel.removeElements(entitiesToDelete);
//                adding new entities to default vocabulary list:
            OWLObjectListModel<OWLEntity> listToAdd;
            OWLObjectListModel<OWLEntity> listToCheck;
            if (preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
                listToAdd = standardLayoutPermittedVocabularyListModel;
                listToCheck = standardLayoutForbiddenVocabularyListModel;
            } else{
                listToAdd = standardLayoutForbiddenVocabularyListModel;
                listToCheck = standardLayoutPermittedVocabularyListModel;
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
        }
    }
    private void updateAlternativeLayoutListModels(Set<OWLEntity> entitiesToAdd, Set<OWLEntity> entitiesToDelete){
//        delete classes, properties, individuals that were removed from signature
        entitiesToDelete.forEach(entity ->{
            if (entity instanceof OWLClass){
                OWLClass owlClass = (OWLClass) entity;
                alternativeLayoutPermittedClassesListModel.removeElement(owlClass);
                alternativeLayoutForbiddenClassesListModel.removeElement(owlClass);
            } else if (entity instanceof OWLObjectProperty){
                OWLObjectProperty property = (OWLObjectProperty) entity;
                alternativeLayoutPermittedPropertiesListModel.removeElement(property);
                alternativeLayoutForbiddenPropertiesListModel.removeElement(property);
            } else if (entity instanceof OWLNamedIndividual){
                OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                alternativeLayoutPermittedIndividualsListModel.removeElement(individual);
                alternativeLayoutForbiddenIndividualsListModel.removeElement(individual);
            }
        });
//        add classes, propertis, individuals that were added to the signature
        boolean addToPermitted = preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted);
        for (OWLEntity entity : entitiesToAdd){
            if (entity instanceof OWLClass){
                OWLClass owlClass = (OWLClass) entity;
                if (addToPermitted){
                    if (! alternativeLayoutForbiddenClassesListModel.getOwlObjects().contains(owlClass)){
                        alternativeLayoutPermittedClassesListModel.checkAndAddElement(owlClass);
                    }
                } else{
                    if (! alternativeLayoutPermittedClassesListModel.getOwlObjects().contains(owlClass)){
                        alternativeLayoutForbiddenClassesListModel.checkAndAddElement(owlClass);
                    }
                }
            } else if (entity instanceof OWLObjectProperty){
                OWLObjectProperty property = (OWLObjectProperty) entity;
                if (addToPermitted){
                    if (! alternativeLayoutForbiddenPropertiesListModel.getOwlObjects().contains(property)){
                        alternativeLayoutPermittedPropertiesListModel.checkAndAddElement(property);
                    }
                } else {
                    if (! alternativeLayoutPermittedPropertiesListModel.getOwlObjects().contains(property)){
                        alternativeLayoutForbiddenPropertiesListModel.checkAndAddElement(property);
                    }
                }
            } else if (entity instanceof OWLNamedIndividual){
                OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                if (addToPermitted){
                    if (! alternativeLayoutForbiddenIndividualsListModel.getOwlObjects().contains(individual)){
                        alternativeLayoutPermittedIndividualsListModel.checkAndAddElement(individual);
                    }
                } else{
                    if (! alternativeLayoutPermittedIndividualsListModel.getOwlObjects().contains(individual)){
                        alternativeLayoutForbiddenIndividualsListModel.checkAndAddElement(individual);
                    }
                }
            }
        }
    }

}
