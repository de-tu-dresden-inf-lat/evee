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

//    alternative layout UI-elements
    private JTabbedPane alternativeLayoutOntologySignatureTabbedPane;
    private OWLObjectTree<OWLClass> alternativeLayoutClassesTree;
    private OWLObjectTree<OWLObjectProperty> alternativeLayoutPropertyTree;
    private OWLObjectListModel<OWLNamedIndividual> alternativeLayoutOntologyIndividualsListModel;
    private JList<OWLNamedIndividual> alternativeLayoutOntologyIndividualsJList;
    private JTabbedPane alternativeLayoutVocabularyTabbedPane;
    private OWLObjectListModel<OWLEntity> alternativeLayoutPermittedVocabularyListModel;
    private JList<OWLEntity> alternativeLayoutPermittedVocabularyList;
    private OWLObjectListModel<OWLEntity> alternativeLayoutForbiddenVocabularyListModel;
    private JList<OWLEntity> alternativeLayoutForbiddenVocabularyList;
    private JPanel alternativeLayoutButtonHolderPanel;
    private JPanel alternativeVocabularyManagementPanel;
    private final List<Dimension> alternativeLayoutWideComponentDimensionList;

//   alternative layout button elements
    private static final String ALTERNATIVE_ADD_BTN_COMMAND = "ALTERNATIVE_ADD";
    private static final String ALTERNATIVE_ADD_BTN_TOOLTIP = "Add selected OWLObjects to the vocabulary";
    private static final String ALTERNATIVE_ADD_ALL_BTN_COMMAND = "ALTERNATIVE_ADD_ALL";
    private static final String ALTERNATIVE_ADD_ALL_BTN_TOOLTIP = "Add all entities to the vocabulary";
    private static final String ALTERNATIVE_DEL_BTN_COMMAND = "ALTERNATIVE_DELETE";
    private static final String ALTERNATIVE_DEL_BTN_TOOLTIP = "Delete selected OWLObjects from the vocabulary";
    private static final String ALTERNATIVE_DEL_ALL_BTN_COMMAND = "ALTERNATIVE_DELETE_ALL";
    private static final String ALTERNATIVE_DEL_ALL_BTN_TOOLTIP = "Delete all entities from the vocabulary";
    private static final String ALTERNATIVE_LOAD_SIGNATURE_COMMAND = "ALTERNATIVE_LOAD_SIGNATURE";
    private static final String ALTERNATIVE_SAVE_SIGNATURE_COMMAND = "ALTERNATIVE_SAVE_SIGNATURE";
    private static final String ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND =
            "ALTERNATIVE_ADD_MISSING_ENTAILMENT";
    private static final String ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME =
            "Add missing entailment vocabulary";
    private static final String ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP =
            "Adds vocabulary of missing entailment to the selected vocabulary tab";

//    standard layout UI-elements
    private JPanel standardLayoutMiddleButtonHolderPanel;
    private JPanel standardLayoutBottomButtonHolderPanel;
    private OWLObjectListModel<OWLClass> standardLayoutPermittedClassesListModel;
    private OWLObjectListModel<OWLObjectProperty> standardLayoutPermittedPropertiesListModel;
    private OWLObjectListModel<OWLNamedIndividual> standardLayoutPermittedIndividualsListModel;
    private OWLObjectListModel<OWLClass> standardLayoutForbiddenClassesListModel;
    private OWLObjectListModel<OWLObjectProperty> standardLayoutForbiddenPropertiesListModel;
    private OWLObjectListModel<OWLNamedIndividual> standardLayoutForbiddenIndividualsListModel;
    private JList<OWLClass> standardLayoutPermittedClassesList;
    private JList<OWLClass> standardLayoutForbiddenClassesList;
    private JList<OWLObjectProperty> standardLayoutPermittedPropertiesList;
    private JList<OWLObjectProperty> standardLayoutForbiddenPropertiesList;
    private JList<OWLNamedIndividual> standardLayoutPermittedIndividualsList;
    private JList<OWLNamedIndividual> standardLayoutForbiddenIndividualsList;
    private JTabbedPane standardLayoutPermittedSignatureTabbedPane;
    private JTabbedPane standardLayoutForbiddenSignatureTabbedPane;
    private JPanel standardVocabularyManagementPanel;
    private final List<Dimension> standardLayoutWideComponentDimensionList;

//   standard layout button elements
    private static final String STANDARD_ADD_BTN_COMMAND = "STANDARD_ADD";
    private static final String STANDARD_ADD_BTN_TOOLTIP = "Add selected OWLObjects to the forbidden vocabulary";
    private static final String STANDARD_ADD_ALL_BTN_COMMAND = "STANDARD_ADD_ALL";
    private static final String STANDARD_ADD_ALL_BTN_TOOLTIP = "Add all entities to the forbidden vocabulary";
    private static final String STANDARD_DEL_BTN_COMMAND = "STANDARD_DELETE";
    private static final String STANDARD_DEL_BTN_TOOLTIP = "Delete selected OWLObjects from the forbidden vocabulary";
    private static final String STANDARD_DEL_ALL_BTN_COMMAND = "STANDARD_DELETE_ALL";
    private static final String STANDARD_DEL_ALL_BTN_TOOLTIP = "Delete all entities from the forbidden vocabulary";
    private static final String STANDARD_LOAD_SIGNATURE_COMMAND = "STANDARD_LOAD_SIGNATURE";
    private static final String STANDARD_SAVE_SIGNATURE_COMMAND = "STANDARD_SAVE_SIGNATURE";
    private static final String STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND =
            "Standard_ADD_MISSING_ENTAILMENT";
    private static final String STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME =
            "Forbid missing entailment vocabulary";
    private static final String STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP =
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

    public NonEntailmentVocabularySelectionUI(
            NonEntailmentViewComponent nonEntailmentViewComponent, OWLEditorKit editorKit){
        this.nonEntailmentViewComponent = nonEntailmentViewComponent;
        this.owlEditorKit = editorKit;
        this.SignatureModelManagerListener = new SignatureOWLModelChangeListener();
        this.owlEditorKit.getOWLModelManager().addListener(this.SignatureModelManagerListener);
        this.SignatureOntologyChangeListener = new SignatureOntologyChangeListener();
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(
                this.SignatureOntologyChangeListener);
        this.preferencesManager = NonEntailmentGeneralPreferencesManager.getInstance();
        this.alternativeLayoutWideComponentDimensionList = new ArrayList<>();
        this.standardLayoutWideComponentDimensionList = new ArrayList<>();
//        SwingUtilities.invokeLater(() -> {
            this.createAlternativeLayoutComponents();
            this.createStandardLayoutComponents();
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
        this.logger.debug("Resetting vocabulary list models for standard and alternative layout");
//        reset alternative layout models:
        this.alternativeLayoutPermittedVocabularyListModel.removeAll();
        this.alternativeLayoutForbiddenVocabularyListModel.removeAll();
        if (this.preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
            this.alternativeLayoutPermittedVocabularyListModel.addElements(this.getCompleteOntologySignature());
        } else{
            this.alternativeLayoutForbiddenVocabularyListModel.addElements(this.getCompleteOntologySignature());
        }
//        reset standard layout models:
        this.standardLayoutPermittedClassesListModel.removeAll();
        this.standardLayoutForbiddenClassesListModel.removeAll();
        this.standardLayoutPermittedPropertiesListModel.removeAll();
        this.standardLayoutForbiddenPropertiesListModel.removeAll();
        this.standardLayoutPermittedIndividualsListModel.removeAll();
        this.standardLayoutForbiddenIndividualsListModel.removeAll();
        Set<OWLClass> classSignature = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getClassesInSignature();
        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
        classSignature.remove(dataFactory.getOWLThing());
        classSignature.remove(dataFactory.getOWLNothing());
        this.standardLayoutPermittedClassesListModel.checkAndAddElements(classSignature);
        Set<OWLObjectProperty> propertySignature = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getObjectPropertiesInSignature();
        propertySignature.remove(dataFactory.getOWLTopObjectProperty());
        propertySignature.remove(dataFactory.getOWLBottomObjectProperty());
        this.standardLayoutPermittedPropertiesListModel.checkAndAddElements(propertySignature);
        this.standardLayoutPermittedIndividualsListModel.checkAndAddElements(
                this.owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature());
        this.clearVocabularySelection();
        this.logger.debug("Resetting vocabulary list models for standard and alternative layout completed");
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
        if (this.alternativeLayoutClassesTree != null){
            this.alternativeLayoutClassesTree.dispose();
        }
        if (this.alternativeLayoutPropertyTree != null){
            this.alternativeLayoutPropertyTree.dispose();
        }
        if (this.alternativeLayoutOntologyIndividualsListModel != null){
            this.alternativeLayoutOntologyIndividualsListModel.dispose();
        }
        this.alternativeLayoutPermittedVocabularyListModel.dispose();
        this.alternativeLayoutForbiddenVocabularyListModel.dispose();
        this.standardLayoutPermittedClassesListModel.dispose();
        this.standardLayoutForbiddenClassesListModel.dispose();
        this.standardLayoutPermittedPropertiesListModel.dispose();
        this.standardLayoutForbiddenPropertiesListModel.dispose();
        this.standardLayoutPermittedIndividualsListModel.dispose();
        this.standardLayoutForbiddenIndividualsListModel.dispose();
        modelManager.removeListener(this.SignatureModelManagerListener);
        modelManager.removeOntologyChangeListener(this.SignatureOntologyChangeListener);

    }

    public Collection<OWLEntity> getPermittedVocabulary(){
        if (this.preferencesManager.loadSignatureComponentLayout().equals(
                NonEntailmentGeneralPreferencesManager.STANDARD_LAYOUT)){
            ArrayList<OWLEntity> result = new ArrayList<>();
            result.addAll(this.standardLayoutPermittedClassesListModel.getOwlObjects());
            result.addAll(this.standardLayoutPermittedPropertiesListModel.getOwlObjects());
            result.addAll(this.standardLayoutPermittedIndividualsListModel.getOwlObjects());
            return result;
        } else {
            return this.alternativeLayoutPermittedVocabularyListModel.getOwlObjects();
        }
    }

//    alternative layout element creation
    private void createAlternativeLayoutComponents(){
        this.createAlternativeLayoutOntologySignatureTabbedPane();
        this.createAlternativeLayoutButtonHolderPanel();
        this.createAlternativeLayoutSelectedVocabularyListPane();
        this.createAlternativeVocabularyManagementPanel();
    }
    private void createAlternativeLayoutOntologySignatureTabbedPane(){
        this.logger.debug("Creating alternative layout ontology signature tabbed pane");
        JTabbedPane tabbedPane = new JTabbedPane();
//        todo: highlighting keywords for classes + properties? see method "initialiseView" in Protege's "AbstractOWLEntityHierarchyViewComponent"
//        classes
        this.alternativeLayoutClassesTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        JScrollPane classesPane = new JScrollPane(this.alternativeLayoutClassesTree);
        classesPane.getViewport().setBackground(Color.WHITE);
        this.alternativeLayoutClassesTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.alternativeLayoutClassesTree.setOWLObjectComparator(
                this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Classes", classesPane);
//        object properties
        this.alternativeLayoutPropertyTree = new OWLModelManagerTree<>(
                this.owlEditorKit,
                this.owlEditorKit.getOWLModelManager().getOWLHierarchyManager()
                        .getOWLObjectPropertyHierarchyProvider());
        JScrollPane propertyPane = new JScrollPane(this.alternativeLayoutPropertyTree);
        propertyPane.getViewport().setBackground(Color.WHITE);
        this.alternativeLayoutPropertyTree.setCellRenderer(new ProtegeTreeNodeRenderer(this.owlEditorKit));
        this.alternativeLayoutPropertyTree.setOWLObjectComparator(
                this.owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Object properties", propertyPane);
//        individuals
        this.alternativeLayoutOntologyIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutOntologyIndividualsJList = new JList<>(this.alternativeLayoutOntologyIndividualsListModel);
        this.alternativeLayoutOntologyIndividualsJList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        Set<OWLNamedIndividual> individuals = this.owlEditorKit.getOWLModelManager().
                getActiveOntology().getIndividualsInSignature(Imports.INCLUDED);
        this.alternativeLayoutOntologyIndividualsListModel.addElements(individuals);
        tabbedPane.addTab("Individuals", this.alternativeLayoutOntologyIndividualsJList);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),"Ontology vocabulary:");
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(titledBorder,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int upperComponentTitleWidth = (int) titledBorder.getMinimumSize(tabbedPane).getWidth() + 5;
        int upperComponentTitleHeight = (int) titledBorder.getMinimumSize(tabbedPane).getHeight() + 5;
        this.alternativeLayoutWideComponentDimensionList.add(new Dimension(
                upperComponentTitleWidth, upperComponentTitleHeight));
        this.alternativeLayoutOntologySignatureTabbedPane = tabbedPane;
        this.logger.debug("Alternative layout ontology signature tabbed pane created");
    }

    private void removeTopAndBottomEntities(Collection<? extends OWLEntity> entities){
        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
        entities.remove(dataFactory.getOWLThing());
        entities.remove(dataFactory.getOWLNothing());
        entities.remove(dataFactory.getOWLTopObjectProperty());
        entities.remove(dataFactory.getOWLBottomObjectProperty());
    }

    private void createAlternativeLayoutButtonHolderPanel(){
        this.alternativeLayoutButtonHolderPanel = new JPanel();
        this.alternativeLayoutButtonHolderPanel.setLayout(
                new BoxLayout(this.alternativeLayoutButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.alternativeLayoutButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
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
        this.alternativeLayoutButtonHolderPanel.add(firstButtonRowPanel);
        this.alternativeLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
        this.alternativeLayoutButtonHolderPanel.add(secondButtonRowPanel);
        buttonList.clear();
        if (! this.preferencesManager.loadUseSimpleMode()){
            this.alternativeLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JButton addMissingEntailmentSignatureButton = UIUtilities.createNamedButton(
                    ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND,
                    ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME,
                    ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP, this);
            buttonList.add(addMissingEntailmentSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(addMissingEntailmentSignatureButton.getMinimumSize());
            JPanel thirdButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
            this.alternativeLayoutButtonHolderPanel.add(thirdButtonRowPanel);
            this.alternativeLayoutButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            buttonList.clear();
            JButton loadSignatureButton = UIUtilities.createNamedButton(ALTERNATIVE_LOAD_SIGNATURE_COMMAND,
                    LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(loadSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(loadSignatureButton.getMinimumSize());
            JButton saveSignatureButton = UIUtilities.createNamedButton(ALTERNATIVE_SAVE_SIGNATURE_COMMAND,
                    SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(saveSignatureButton);
            this.alternativeLayoutWideComponentDimensionList.add(saveSignatureButton.getMinimumSize());
            JPanel fourthButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.PAGE_AXIS);
            this.alternativeLayoutButtonHolderPanel.add(fourthButtonRowPanel);
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

    private void createAlternativeLayoutSelectedVocabularyListPane(){
        this.logger.debug("Creating alternative layout selected vocabulary tabbed pane");
        this.alternativeLayoutPermittedVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutPermittedVocabularyList = new JList<>(this.alternativeLayoutPermittedVocabularyListModel);
        this.alternativeLayoutPermittedVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutForbiddenVocabularyListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.alternativeLayoutForbiddenVocabularyList = new JList<>(this.alternativeLayoutForbiddenVocabularyListModel);
        this.alternativeLayoutForbiddenVocabularyList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.alternativeLayoutVocabularyTabbedPane = new JTabbedPane();
        this.alternativeLayoutVocabularyTabbedPane.addTab("Permitted vocabulary",
                ComponentFactory.createScrollPane(this.alternativeLayoutPermittedVocabularyList));
        this.alternativeLayoutVocabularyTabbedPane.addTab("Forbidden vocabulary",
                ComponentFactory.createScrollPane(this.alternativeLayoutForbiddenVocabularyList));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "Vocabulary:");
        int titleWidth = (int) titledBorder.getMinimumSize(this.alternativeLayoutVocabularyTabbedPane).
                getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(this.alternativeLayoutVocabularyTabbedPane).
                getHeight() + 5;
        this.alternativeLayoutWideComponentDimensionList.add(new Dimension(titleWidth, titleHeight));
        this.alternativeLayoutVocabularyTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.logger.debug("Alternative layout selected vocabulary tabbed pane created");
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
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutOntologySignatureTabbedPane, constraints);
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutButtonHolderPanel, constraints);
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.alternativeVocabularyManagementPanel.add(this.alternativeLayoutVocabularyTabbedPane, constraints);
        this.alternativeVocabularyManagementPanel.setMinimumSize(new Dimension(
//                width of each component
                (int) ceil(max(this.alternativeLayoutWideComponentDimensionList.stream().map(Dimension::getWidth).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of alternativeVocabularyManagementPanel
                        + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                height of each component
                (int) ceil(max(this.alternativeLayoutWideComponentDimensionList.stream().map(Dimension::getHeight).
                        collect(Collectors.toList())))
//                        component embedded in gridBagLayout of alternativeVocabularyManagementPanel
                        + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom

        ));
    }

//    standard layout element creation
    private void createStandardLayoutComponents(){
        this.createStandardLayoutUpperPanel();
        this.createStandardLayoutMiddleButtonPanel();
        this.createStandardLayoutLowerPanel();
        this.createStandardLayoutBottomButtonPanel();
        this.createStandardVocabularyManagementPanel();
        this.addStandardLayoutTabbedPaneStateChangeListeners();
    }
    private void createStandardLayoutUpperPanel() {
        this.logger.debug("Creating standard layout permitted vocabulary tabbed pane");
//        general
        this.standardLayoutPermittedSignatureTabbedPane = new JTabbedPane();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Permitted vocabulary:");
        this.standardLayoutPermittedSignatureTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int titleWidth = (int) titledBorder.getMinimumSize(
                this.standardLayoutPermittedSignatureTabbedPane).getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(
                this.standardLayoutPermittedSignatureTabbedPane).getHeight() + 5;
        this.standardLayoutWideComponentDimensionList.add(new Dimension(titleWidth,
                titleHeight));
//        classes
        this.standardLayoutPermittedClassesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutPermittedClassesList =
                new JList<>(this.standardLayoutPermittedClassesListModel);
        this.standardLayoutPermittedClassesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutPermittedSignatureTabbedPane.addTab("Classes",
                ComponentFactory.createScrollPane(this.standardLayoutPermittedClassesList));
//        properties
        this.standardLayoutPermittedPropertiesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutPermittedPropertiesList =
                new JList<>(this.standardLayoutPermittedPropertiesListModel);
        this.standardLayoutPermittedPropertiesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutPermittedSignatureTabbedPane.addTab("Object properties",
                ComponentFactory.createScrollPane(this.standardLayoutPermittedPropertiesList));
//        individuals
        this.standardLayoutPermittedIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutPermittedIndividualsList =
                new JList<>(this.standardLayoutPermittedIndividualsListModel);
        this.standardLayoutPermittedIndividualsList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutPermittedSignatureTabbedPane.addTab("Individuals",
                ComponentFactory.createScrollPane(this.standardLayoutPermittedIndividualsList));
        this.logger.debug("Standard layout permitted vocabulary tabbed pane created");
    }

    private void createStandardLayoutLowerPanel() {
        this.logger.debug("Creating standard layout forbidden vocabulary tabbed pane");
//        general
        this.standardLayoutForbiddenSignatureTabbedPane = new JTabbedPane();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Forbidden vocabulary:");
        this.standardLayoutForbiddenSignatureTabbedPane.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int lowerComponentTitleWidth = (int) titledBorder.getMinimumSize(
                this.standardLayoutForbiddenSignatureTabbedPane).getWidth();
        int lowerComponentTitleHeight = (int) titledBorder.getMinimumSize(
                this.standardLayoutForbiddenSignatureTabbedPane).getHeight();
        this.standardLayoutWideComponentDimensionList.add(new Dimension(lowerComponentTitleWidth,
                lowerComponentTitleHeight));
//        classes
        this.standardLayoutForbiddenClassesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutForbiddenClassesList = new JList<>(this.standardLayoutForbiddenClassesListModel);
        this.standardLayoutForbiddenClassesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutForbiddenSignatureTabbedPane.addTab("Classes:",
                ComponentFactory.createScrollPane(this.standardLayoutForbiddenClassesList));
//        properties
        this.standardLayoutForbiddenPropertiesListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutForbiddenPropertiesList =
                new JList<>(this.standardLayoutForbiddenPropertiesListModel);
        this.standardLayoutForbiddenPropertiesList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutForbiddenSignatureTabbedPane.addTab("Object properties",
                ComponentFactory.createScrollPane(this.standardLayoutForbiddenPropertiesList));
//        individuals
        this.standardLayoutForbiddenIndividualsListModel = new OWLObjectListModel<>(this.owlEditorKit);
        this.standardLayoutForbiddenIndividualsList =
                new JList<>(this.standardLayoutForbiddenIndividualsListModel);
        this.standardLayoutForbiddenIndividualsList.setCellRenderer(new OWLCellRendererSimple(this.owlEditorKit));
        this.standardLayoutForbiddenSignatureTabbedPane.addTab("Individuals",
                ComponentFactory.createScrollPane(this.standardLayoutForbiddenIndividualsList));
        this.logger.debug("Alternative layout forbidden vocabulary tabbed pane created");
    }

    private void createStandardLayoutMiddleButtonPanel(){
        this.standardLayoutMiddleButtonHolderPanel = new JPanel();
        this.standardLayoutMiddleButtonHolderPanel.setLayout(
                new BoxLayout(this.standardLayoutMiddleButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.standardLayoutMiddleButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
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
        this.standardLayoutMiddleButtonHolderPanel.add(firstButtonRowPanel);
        this.standardLayoutMiddleButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
        this.standardLayoutMiddleButtonHolderPanel.add(secondButtonRowPanel);
    }

    private void createStandardLayoutBottomButtonPanel(){
        this.standardLayoutBottomButtonHolderPanel = new JPanel();
        this.standardLayoutBottomButtonHolderPanel.setLayout(
                new BoxLayout(this.standardLayoutBottomButtonHolderPanel, BoxLayout.PAGE_AXIS));
        this.standardLayoutBottomButtonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        if (! this.preferencesManager.loadUseSimpleMode()){
            ArrayList<JButton> buttonList = new ArrayList<>();
            JButton addMissingEntailmentSignatureButton =
                    UIUtilities.createNamedButton(
                            STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND,
                            STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_NAME,
                            STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_TOOLTIP, this);
            buttonList.add(addMissingEntailmentSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(addMissingEntailmentSignatureButton.getMinimumSize());
            JPanel firstButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.LINE_AXIS);
            this.standardLayoutBottomButtonHolderPanel.add(firstButtonRowPanel);
            this.standardLayoutBottomButtonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            buttonList.clear();
            JButton loadSignatureButton = UIUtilities.createNamedButton(STANDARD_LOAD_SIGNATURE_COMMAND,
                    LOAD_SIGNATURE_BUTTON_NAME, LOAD_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(loadSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(loadSignatureButton.getMinimumSize());
            JButton saveSignatureButton = UIUtilities.createNamedButton(STANDARD_SAVE_SIGNATURE_COMMAND,
                    SAVE_SIGNATURE_BUTTON_NAME, SAVE_SIGNATURE_BUTTON_TOOLTIP, this);
            buttonList.add(saveSignatureButton);
            this.standardLayoutWideComponentDimensionList.add(saveSignatureButton.getMinimumSize());
            JPanel secondButtonRowPanel = this.createButtonPanelFromList(buttonList, BoxLayout.PAGE_AXIS);
            this.standardLayoutBottomButtonHolderPanel.add(secondButtonRowPanel);
        }
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
        this.standardVocabularyManagementPanel.add(this.standardLayoutPermittedSignatureTabbedPane, constraints);
//        specific for middle buttons:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.standardVocabularyManagementPanel.add(this.standardLayoutMiddleButtonHolderPanel, constraints);
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.99;
        this.standardVocabularyManagementPanel.add(this.standardLayoutForbiddenSignatureTabbedPane, constraints);
//            specific for bottom buttons:
        constraints.gridy = 3;
        constraints.weightx = 0.1;
        constraints.weighty = 0.01;
        this.standardVocabularyManagementPanel.add(this.standardLayoutBottomButtonHolderPanel, constraints);
        this.standardVocabularyManagementPanel.setMinimumSize(new Dimension(
//                width of each component
                (int) ceil(max(this.standardLayoutWideComponentDimensionList.stream().map(Dimension::getWidth).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of standardVocabularyManagementPanel
                        + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                height of each component
                (int) ceil(max(this.standardLayoutWideComponentDimensionList.stream().map(Dimension::getHeight).
                        collect(Collectors.toList())))
//                        components embedded in gridBagLayout of standardVocabularyManagementPanel
                        + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom
        ));
    }

    private void addStandardLayoutTabbedPaneStateChangeListeners(){
        this.standardLayoutForbiddenSignatureTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                standardLayoutPermittedSignatureTabbedPane.setSelectedIndex(
                        standardLayoutForbiddenSignatureTabbedPane.getSelectedIndex());
            }
        });
        this.standardLayoutPermittedSignatureTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                standardLayoutForbiddenSignatureTabbedPane.setSelectedIndex(
                        standardLayoutPermittedSignatureTabbedPane.getSelectedIndex());
            }
        });
    }

//    ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
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
            case ALTERNATIVE_LOAD_SIGNATURE_COMMAND:
            case STANDARD_LOAD_SIGNATURE_COMMAND:
                this.loadSignatureAction();
                break;
            case ALTERNATIVE_SAVE_SIGNATURE_COMMAND:
            case STANDARD_SAVE_SIGNATURE_COMMAND:
                this.saveSignatureAction();
                break;
            case ALTERNATIVE_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND:
                this.alternativeAddMissingEntailmentSignatureAction();
                break;
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
            case STANDARD_ADD_MISSING_ENTAILMENT_SIGNATURE_BTN_COMMAND:
                this.standardAddMissingEntailmentSignatureAction();
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
                    NonEntailmentGeneralPreferencesManager.ALTERNATIVE_LAYOUT_LISTS)){
//                deleting from one list = adding to other list
                this.addAll2VocabularyList(VocabularyTab.Forbidden);
                this.moveEntities2VocabularyList(knownEntitySet, VocabularyTab.Permitted);
                this.clearVocabularySelection();
//                this.vocabularyTabbedPane.setSelectedIndex(0);
            } else {
                this.standardLayoutForbiddenClassesListModel.checkAndAddElements(
                        this.standardLayoutPermittedClassesListModel.getOwlObjects());
                this.standardLayoutPermittedClassesListModel.removeAll();
                this.standardLayoutForbiddenPropertiesListModel.checkAndAddElements(
                        this.standardLayoutPermittedPropertiesListModel.getOwlObjects());
                this.standardLayoutPermittedPropertiesListModel.removeAll();
                this.standardLayoutForbiddenIndividualsListModel.checkAndAddElements(
                        this.standardLayoutPermittedIndividualsListModel.getOwlObjects());
                this.standardLayoutPermittedIndividualsListModel.removeAll();
                for (OWLEntity entity : knownEntitySet){
                    if (entity instanceof OWLClass){
                        OWLClass owlClass = (OWLClass) entity;
                        this.standardLayoutPermittedClassesListModel.checkAndAddElement(owlClass);
                        this.standardLayoutForbiddenClassesListModel.removeElement(owlClass);
                    } else if (entity instanceof OWLObjectProperty){
                        OWLObjectProperty property = (OWLObjectProperty) entity;
                        this.standardLayoutPermittedPropertiesListModel.checkAndAddElement(property);
                        this.standardLayoutForbiddenPropertiesListModel.removeElement(property);
                    } else if (entity instanceof OWLNamedIndividual){
                        OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                        this.standardLayoutPermittedIndividualsListModel.checkAndAddElement(individual);
                        this.standardLayoutForbiddenIndividualsListModel.removeElement(individual);
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
                        NonEntailmentGeneralPreferencesManager.ALTERNATIVE_LAYOUT_LISTS)){
                    signatureFileHandler.setSignature(this.alternativeLayoutPermittedVocabularyListModel.getOwlObjects());
                } else{
                    Set<OWLEntity> signature = new HashSet<>();
                    signature.addAll(this.standardLayoutPermittedClassesListModel.getOwlObjects());
                    signature.addAll(this.standardLayoutPermittedPropertiesListModel.getOwlObjects());
                    signature.addAll(this.standardLayoutPermittedIndividualsListModel.getOwlObjects());
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

//    alternative layout button actions
    private void alternativeAddAction(){
        SwingUtilities.invokeLater(() -> {
            int ontologySignatureTabIndex = this.alternativeLayoutOntologySignatureTabbedPane.getSelectedIndex();
            List<? extends OWLEntity> entitiesToAdd;
            if (ontologySignatureTabIndex == 0){
                entitiesToAdd = this.alternativeLayoutClassesTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else if (ontologySignatureTabIndex == 1){
                entitiesToAdd = this.alternativeLayoutPropertyTree.getSelectedOWLObjects();
                this.removeTopAndBottomEntities(entitiesToAdd);
            }
            else{
                entitiesToAdd = this.alternativeLayoutOntologyIndividualsJList.getSelectedValuesList();
            }
            int selectedVocabularyTabIndex = this.alternativeLayoutVocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(entitiesToAdd,
                    this.standardLayoutTabIndex2Name(selectedVocabularyTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void alternativeAddAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.standardLayoutTabIndex2Name(this.alternativeLayoutVocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void alternativeDeleteAction(){
        SwingUtilities.invokeLater(() -> {
            int deleteFromTabIndex = this.alternativeLayoutVocabularyTabbedPane.getSelectedIndex();
            List<OWLEntity> entitiesToDelete;
            if (deleteFromTabIndex == 0){
                entitiesToDelete = this.alternativeLayoutPermittedVocabularyList.getSelectedValuesList();
            } else{
                entitiesToDelete = this.alternativeLayoutForbiddenVocabularyList.getSelectedValuesList();
            }
//            deleting entities from one list = adding entities to other list
            this.moveEntities2VocabularyList(entitiesToDelete,
                    this.tabIndex2ComplementName(deleteFromTabIndex));
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void alternativeDeleteAllAction(){
        SwingUtilities.invokeLater(() -> {
//            deleting entities from one list = adding entities to other list
            VocabularyTab tabToAdd = this.tabIndex2ComplementName(this.alternativeLayoutVocabularyTabbedPane.getSelectedIndex());
            this.addAll2VocabularyList(tabToAdd);
            this.clearVocabularySelection();
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void alternativeAddMissingEntailmentSignatureAction(){
        SwingUtilities.invokeLater(() -> {
            ArrayList<OWLObject> missingEntailmentList = this.nonEntailmentViewComponent.getMissingEntailments();
            HashSet<OWLEntity> missingEntailmentSet = new HashSet<>();
            missingEntailmentList.forEach(missingEntailment -> missingEntailmentSet.addAll(missingEntailment.getSignature()));
            this.removeTopAndBottomEntities(missingEntailmentSet);
//            deleting entities from one list = adding entities to other list
            int tabIndex = this.alternativeLayoutVocabularyTabbedPane.getSelectedIndex();
            this.moveEntities2VocabularyList(missingEntailmentSet,
                    this.standardLayoutTabIndex2Name(tabIndex));
            this.nonEntailmentViewComponent.checkComputeButtonAndWarningLabelStatus();
        });
    }

    private void moveEntities2VocabularyList(Collection<? extends OWLEntity> entitiesToAdd, VocabularyTab addToTab){
        OWLObjectListModel<OWLEntity> listToAdd;
        OWLObjectListModel<OWLEntity> listToDelete;
        if (addToTab == VocabularyTab.Permitted){
            listToAdd = this.alternativeLayoutPermittedVocabularyListModel;
            listToDelete = this.alternativeLayoutForbiddenVocabularyListModel;
        } else{
            listToAdd = this.alternativeLayoutForbiddenVocabularyListModel;
            listToDelete = this.alternativeLayoutPermittedVocabularyListModel;
        }
        listToAdd.checkAndAddElements(entitiesToAdd);
        listToDelete.removeElements(entitiesToAdd);
    }

    private void clearVocabularySelection(){
        this.alternativeLayoutClassesTree.clearSelection();
        this.alternativeLayoutPropertyTree.clearSelection();
        this.alternativeLayoutOntologyIndividualsJList.clearSelection();
        this.alternativeLayoutPermittedVocabularyList.clearSelection();
        this.alternativeLayoutForbiddenVocabularyList.clearSelection();
        this.standardLayoutPermittedClassesList.clearSelection();
        this.standardLayoutForbiddenClassesList.clearSelection();
        this.standardLayoutPermittedPropertiesList.clearSelection();
        this.standardLayoutForbiddenPropertiesList.clearSelection();
        this.standardLayoutPermittedIndividualsList.clearSelection();
        this.standardLayoutForbiddenIndividualsList.clearSelection();
    }

    private void addAll2VocabularyList(VocabularyTab addToTab){
        this.moveEntities2VocabularyList(this.getCompleteOntologySignature(), addToTab);
    }

//    standard layout button actions
    private void standardAddAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.standardLayoutPermittedSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.standardLayoutPermittedClassesList.getSelectedValuesList());
                    this.standardLayoutPermittedClassesListModel.removeElements(classes);
                    this.standardLayoutForbiddenClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.standardLayoutPermittedPropertiesList.getSelectedValuesList());
                    this.standardLayoutPermittedPropertiesListModel.removeElements(properties);
                    this.standardLayoutForbiddenPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.standardLayoutPermittedIndividualsList.getSelectedValuesList());
                    this.standardLayoutPermittedIndividualsListModel.removeElements(individuals);
                    this.standardLayoutForbiddenIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void standardDeleteAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.standardLayoutForbiddenSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.standardLayoutForbiddenClassesList.getSelectedValuesList());
                    this.standardLayoutForbiddenClassesListModel.removeElements(classes);
                    this.standardLayoutPermittedClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.standardLayoutForbiddenPropertiesList.getSelectedValuesList());
                    this.standardLayoutForbiddenPropertiesListModel.removeElements(properties);
                    this.standardLayoutPermittedPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.standardLayoutForbiddenIndividualsList.getSelectedValuesList());
                    this.standardLayoutForbiddenIndividualsListModel.removeElements(individuals);
                    this.standardLayoutPermittedIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void standardAddAllAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.standardLayoutPermittedSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.standardLayoutPermittedClassesListModel.getOwlObjects());
                    this.standardLayoutPermittedClassesListModel.removeElements(classes);
                    this.standardLayoutForbiddenClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.standardLayoutPermittedPropertiesListModel.getOwlObjects());
                    this.standardLayoutPermittedPropertiesListModel.removeElements(properties);
                    this.standardLayoutForbiddenPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.standardLayoutPermittedIndividualsListModel.getOwlObjects());
                    this.standardLayoutPermittedIndividualsListModel.removeElements(individuals);
                    this.standardLayoutForbiddenIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void standardDeleteAllAction() {
        SwingUtilities.invokeLater(() -> {
            int idx = this.standardLayoutForbiddenSignatureTabbedPane.getSelectedIndex();
            switch (idx){
                case 0:
                    ArrayList<OWLClass> classes = new ArrayList<>(
                            this.standardLayoutForbiddenClassesListModel.getOwlObjects());
                    this.standardLayoutForbiddenClassesListModel.removeElements(classes);
                    this.standardLayoutPermittedClassesListModel.checkAndAddElements(classes);
                    break;
                case 1:
                    ArrayList<OWLObjectProperty> properties = new ArrayList<>(
                            this.standardLayoutForbiddenPropertiesListModel.getOwlObjects());
                    this.standardLayoutForbiddenPropertiesListModel.removeElements(properties);
                    this.standardLayoutPermittedPropertiesListModel.checkAndAddElements(properties);
                    break;
                case 2:
                    ArrayList<OWLNamedIndividual> individuals = new ArrayList<>(
                            this.standardLayoutForbiddenIndividualsListModel.getOwlObjects());
                    this.standardLayoutForbiddenIndividualsListModel.removeElements(individuals);
                    this.standardLayoutPermittedIndividualsListModel.addElements(individuals);
                    break;
            }
            this.clearVocabularySelection();
        });
    }

    private void standardAddMissingEntailmentSignatureAction() {
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
                    standardLayoutPermittedClassesListModel.removeElement(owlClass);
                    standardLayoutForbiddenClassesListModel.checkAndAddElement(owlClass);
                } else if (entity instanceof OWLObjectProperty){
                    OWLObjectProperty property = (OWLObjectProperty) entity;
                    standardLayoutPermittedPropertiesListModel.removeElement(property);
                    standardLayoutForbiddenPropertiesListModel.checkAndAddElement(property);
                } else if (entity instanceof OWLNamedIndividual){
                    OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                    standardLayoutPermittedIndividualsListModel.removeElement(individual);
                    standardLayoutForbiddenIndividualsListModel.checkAndAddElement(individual);
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
                    this.alternativeLayoutForbiddenVocabularyListModel.checkAndAddElement(newClassName);
                    this.alternativeLayoutPermittedVocabularyListModel.removeElement(newClassName);
                    this.standardLayoutForbiddenClassesListModel.checkAndAddElement(newClassName);
                    this.standardLayoutPermittedClassesListModel.removeElement(newClassName);
                }
            } else if (name instanceof OWLObjectProperty){
                OWLObjectProperty newPropertyName = (OWLObjectProperty) name;
                if (! newPropertyName.isBottomEntity() && ! newPropertyName.isTopEntity()){
                    this.alternativeLayoutForbiddenVocabularyListModel.checkAndAddElement(newPropertyName);
                    this.alternativeLayoutPermittedVocabularyListModel.removeElement(newPropertyName);
                    this.standardLayoutForbiddenPropertiesListModel.checkAndAddElement(newPropertyName);
                    this.standardLayoutPermittedPropertiesListModel.removeElement(newPropertyName);
                }
            } else if (name instanceof OWLNamedIndividual){
                OWLNamedIndividual newIndividualName = (OWLNamedIndividual) name;
                this.alternativeLayoutForbiddenVocabularyListModel.checkAndAddElement(newIndividualName);
                this.alternativeLayoutPermittedVocabularyListModel.removeElement(newIndividualName);
                this.standardLayoutForbiddenIndividualsListModel.checkAndAddElement(newIndividualName);
                this.standardLayoutPermittedIndividualsListModel.removeElement(newIndividualName);
            }
        });
        if (this.preferencesManager.loadSignatureComponentLayout().equals(
                NonEntailmentGeneralPreferencesManager.ALTERNATIVE_LAYOUT_LISTS)){
            this.alternativeLayoutVocabularyTabbedPane.setSelectedIndex(1);
        }
    }

    public void resetVocabularyManagementPanel() {
        this.standardLayoutWideComponentDimensionList.clear();
        this.createAlternativeLayoutComponents();
        this.alternativeLayoutWideComponentDimensionList.clear();
        this.createStandardLayoutComponents();
        this.resetVocabularyListModels();
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
                    alternativeLayoutOntologyIndividualsListModel.removeAll();
                    alternativeLayoutOntologyIndividualsListModel.addElements(
                            owlEditorKit.getOWLModelManager().getActiveOntology()
                                    .getIndividualsInSignature(Imports.INCLUDED));
                    alternativeLayoutOntologyIndividualsJList.clearSelection();
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
                updateAlternativeLayoutListModels(entitiesToDelete);
                updateStandardLayoutListModels(entitiesToAdd, entitiesToDelete);
            });
        }

        private void updateAlternativeLayoutListModels(Set<OWLEntity> entitiesToDelete){
//               ontology signature component:
            alternativeLayoutOntologyIndividualsListModel.removeAll();
            alternativeLayoutOntologyIndividualsListModel.addElements(
                    owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature(
                            Imports.INCLUDED));
            alternativeLayoutOntologyIndividualsJList.clearSelection();
//               selected vocabulary component:
//                deleting individuals removed from vocabulary (permitted and forbidden):
            alternativeLayoutPermittedVocabularyListModel.removeElements(entitiesToDelete);
            alternativeLayoutForbiddenVocabularyListModel.removeElements(entitiesToDelete);
//                adding new entities to default vocabulary list:
            OWLObjectListModel<OWLEntity> listToAdd;
            OWLObjectListModel<OWLEntity> listToCheck;
            if (preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted)){
                listToAdd = alternativeLayoutPermittedVocabularyListModel;
                listToCheck = alternativeLayoutForbiddenVocabularyListModel;
            } else{
                listToAdd = alternativeLayoutForbiddenVocabularyListModel;
                listToCheck = alternativeLayoutPermittedVocabularyListModel;
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
    private void updateStandardLayoutListModels(Set<OWLEntity> entitiesToAdd, Set<OWLEntity> entitiesToDelete){
//        delete classes, properties, individuals that were removed from signature
        entitiesToDelete.forEach(entity ->{
            if (entity instanceof OWLClass){
                OWLClass owlClass = (OWLClass) entity;
                standardLayoutPermittedClassesListModel.removeElement(owlClass);
                standardLayoutForbiddenClassesListModel.removeElement(owlClass);
            } else if (entity instanceof OWLObjectProperty){
                OWLObjectProperty property = (OWLObjectProperty) entity;
                standardLayoutPermittedPropertiesListModel.removeElement(property);
                standardLayoutForbiddenPropertiesListModel.removeElement(property);
            } else if (entity instanceof OWLNamedIndividual){
                OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                standardLayoutPermittedIndividualsListModel.removeElement(individual);
                standardLayoutForbiddenIndividualsListModel.removeElement(individual);
            }
        });
//        add classes, properties, individuals that were added to the signature
        boolean addToPermitted = preferencesManager.loadDefaultVocabularyTab().equals(VocabularyTab.Permitted);
        for (OWLEntity entity : entitiesToAdd){
            if (entity instanceof OWLClass){
                OWLClass owlClass = (OWLClass) entity;
                if (addToPermitted){
                    if (! standardLayoutForbiddenClassesListModel.getOwlObjects().contains(owlClass)){
                        standardLayoutPermittedClassesListModel.checkAndAddElement(owlClass);
                    }
                } else{
                    if (! standardLayoutPermittedClassesListModel.getOwlObjects().contains(owlClass)){
                        standardLayoutForbiddenClassesListModel.checkAndAddElement(owlClass);
                    }
                }
            } else if (entity instanceof OWLObjectProperty){
                OWLObjectProperty property = (OWLObjectProperty) entity;
                if (addToPermitted){
                    if (! standardLayoutForbiddenPropertiesListModel.getOwlObjects().contains(property)){
                        standardLayoutPermittedPropertiesListModel.checkAndAddElement(property);
                    }
                } else {
                    if (! standardLayoutPermittedPropertiesListModel.getOwlObjects().contains(property)){
                        standardLayoutForbiddenPropertiesListModel.checkAndAddElement(property);
                    }
                }
            } else if (entity instanceof OWLNamedIndividual){
                OWLNamedIndividual individual = (OWLNamedIndividual) entity;
                if (addToPermitted){
                    if (! standardLayoutForbiddenIndividualsListModel.getOwlObjects().contains(individual)){
                        standardLayoutPermittedIndividualsListModel.checkAndAddElement(individual);
                    }
                } else{
                    if (! standardLayoutPermittedIndividualsListModel.getOwlObjects().contains(individual)){
                        standardLayoutForbiddenIndividualsListModel.checkAndAddElement(individual);
                    }
                }
            }
        }
    }

}
