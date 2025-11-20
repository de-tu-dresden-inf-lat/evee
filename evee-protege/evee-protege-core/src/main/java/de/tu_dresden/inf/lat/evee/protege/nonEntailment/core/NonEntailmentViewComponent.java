package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences.NonEntailmentGeneralPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPlugin;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPluginLoader;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingScreenEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IPreferencesChangeListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.*;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

import static java.lang.Math.ceil;
import static java.util.Collections.max;
import static org.junit.Assert.assertNotNull;

public class NonEntailmentViewComponent extends AbstractOWLViewComponent
        implements ActionListener,
        IPreferencesChangeListener,
        IExplanationLoadingScreenEventListener,
        ISignatureModificationEventListener,
        IExplanationGenerationListener<
                ExplanationEvent<
                        INonEntailmentExplanationService<?>>> {

//    non-UI-related
    private final NonEntailmentExplainerManager nonEntailmentExplainerManager;
    private final NonEntailmentGeneralPreferencesManager preferencesManager;
    private final ViewComponentOntologyChangeListener changeListener;
    private boolean ignoreOntologyChangeEvent;

//    UI-elements
    private NonEntailmentVocabularySelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> missingEntailmentTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedMissingEntailmentListModel;
    private JList<OWLAxiom> selectedMissingEntailmentList;
    private JButton computeButton;
    private JPanel resultHolderComponent;
    private JPanel holderPanel;
    private JPanel serviceSelectionComponent;
    private JPanel signatureAndMissingEntailmentComponent;
    private JTabbedPane signatureAndMissingEntailmentTabbedPane;
    private JPanel signatureManagementPanel;
    private JPanel missingEntailmentManagementPanel;
    private JPanel nonEntailmentExplanationServiceComponent;
    private JSplitPane horizontalSplitPane;
    private JComboBox<String> serviceNamesComboBox;
    private JLabel computeMessageLabel;
    private JLabel filterWarningLabel;
    protected NonEntailmentExplanationLoadingScreenManager loadingUI;
    private final List<Dimension> wideComponentDimensionList;

//    Action-Commands, button-labels, button-tooltips
    private static final String COMPUTE_COMMAND = "COMPUTE_NON_ENTAILMENT";
    private static final String COMPUTE_NAME = "Generate explanation";
    private static final String COMPUTE_TOOLTIP = "Generate non-entailment explanation using selected vocabulary and missing entailment";
    private static final String ADD_MISSING_ENTAILMENT_COMMAND = "ADD_MISSING_ENTAILMENT";
    private static final String ADD_MISSING_ENTAILMENT_NAME = "Add";
    private static final String ADD_MISSING_ENTAILMENT_TOOLTIP = "Add axioms to missing entailment";
    private static final String DELETE_MISSING_ENTAILMENT_COMMAND = "DELETE_MISSING_ENTAILMENT";
    private static final String DELETE_MISSING_ENTAILMENT_NAME = "Delete";
    private static final String DELETE_MISSING_ENTAILMENT_TOOLTIP = "Delete selected axioms from missing entailment";
    private static final String RESET_MISSING_ENTAILMENT_COMMAND = "RESET_MISSING_ENTAILMENT";
    private static final String RESET_MISSING_ENTAILMENT_NAME = "Reset";
    private static final String RESET_MISSING_ENTAILMENT_TOOLTIP = "Delete all axioms from missing entailment";
    private static final String LOAD_MISSING_ENTAILMENT_COMMAND = "LOAD_MISSING_ENTAILMENT";
    private static final String LOAD_MISSING_ENTAILMENT_BUTTON_NAME = "Load from file";
    private static final String LOAD_MISSING_ENTAILMENT_TOOLTIP = "Load missing entailment from a file";
    private static final String SAVE_MISSING_ENTAILMENT_COMMAND = "SAVE_MISSING_ENTAILMENT";
    private static final String SAVE_MISSING_ENTAILMENT_BUTTON_NAME = "Save to file";
    private static final String SAVE_MISSING_ENTAILMENT_TOOLTIP = "Save missing entailment to a file";
    protected static final String DEFAULT_UI_TITLE = "LOADING";
    private static final String MISSING_ENTAILMENT_TAB_NAME = "Missing entailment";
    private static final String MISSING_ENTAILMENT_TAB_TOOLTIP = "Enter missing entailment that should be explained";
    private static final String SIGNATURE_MANAGEMENT_TAB_NAME = "Vocabulary";
    private static final String SIGNATURE_MANAGEMENT_TAB_TOOLTIP =
            "Enter forbidden/permitted vocabulary that should be used for the explanation";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentViewComponent.class);

//****************************************************************************
//    Constructor, Init, Dispose:
//****************************************************************************
    public NonEntailmentViewComponent(){
        this.nonEntailmentExplainerManager = new NonEntailmentExplainerManager();
        this.preferencesManager = NonEntailmentGeneralPreferencesManager.getInstance();
        this.preferencesManager.registerPreferencesChangeEventListener(this);
        this.changeListener = new ViewComponentOntologyChangeListener();
        this.loadingUI = new NonEntailmentExplanationLoadingScreenManager(DEFAULT_UI_TITLE);
        this.loadingUI.registerLoadingUIListener(this);
        this.wideComponentDimensionList = new ArrayList<>();
        this.ignoreOntologyChangeEvent = false;
        this.logger.debug("Object NonEntailmentViewComponent created");
    }

    @Override
    protected void initialiseOWLView() {
        this.logger.debug("initialisation started");
        this.loadingUI.setup(this.getOWLEditorKit());
        this.signatureSelectionUI = new NonEntailmentVocabularySelectionUI(
                this, this.getOWLEditorKit());
        this.loadingUI.initialise();
        NonEntailmentExplanationPluginLoader loader = new NonEntailmentExplanationPluginLoader(this.getOWLEditorKit());
        for (NonEntailmentExplanationPlugin plugin : loader.getPlugins()){
            try{
                INonEntailmentExplanationService<?> service = plugin.newInstance();
                service.setup(this.getOWLEditorKit());
                service.initialise();
                service.registerListener(this);
                service.registerSignatureModificationEventListener(this);
                this.nonEntailmentExplainerManager.registerNonEntailmentExplanationService(service, plugin.getName());
            }
            catch (Exception e){
                this.logger.error("Error while loading non-entailment explanation plugin: ", e);
            }
        }
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.createGeneralSettingsComponent();
        this.resetSignatureSelectionComponent();
        this.createMissingEntailmentManagementComponent();
        this.resetMainComponent();
        this.nonEntailmentExplainerManager.setExplanationService(
                (String) this.serviceNamesComboBox.getSelectedItem());
        this.getOWLEditorKit().getOWLModelManager().addListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.checkComputeButtonAndWarningLabelStatus();
        this.logger.debug("initialisation completed");
    }

    @Override
    protected void disposeOWLView() {
        this.logger.debug("Disposing Missing Entailment View Component");
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
        this.loadingUI.dispose();
        this.getOWLEditorKit().getOWLModelManager().removeListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().removeOntologyChangeListener(this.changeListener);
        this.nonEntailmentExplainerManager.dispose();
        this.selectedMissingEntailmentListModel.dispose();
        this.logger.debug("Missing Entailment View Component disposed");
    }

    protected ArrayList<OWLObject> getMissingEntailments(){
        return new ArrayList<>(this.selectedMissingEntailmentListModel.getOwlObjects());
    }

//****************************************************************************
//        Methods to create and reset main UI:
//****************************************************************************
    private void resetMainComponent(){
        this.logger.debug("Resetting entire view component");
        this.resetSignatureSelectionComponent();
        this.resetSignatureAndMissingEntailmentComponent();
        this.resetExplanationServiceComponent();
        this.resetHorizontalSplitPane();
        this.resetHolderPanel();
        this.addHolderPanel();
    }


    private void resetSignatureAndMissingEntailmentComponent(){
        this.logger.debug("Resetting vocabulary and missing entailment component");
        this.signatureAndMissingEntailmentComponent = new JPanel();
        this.signatureAndMissingEntailmentComponent.setLayout(
                new BoxLayout(this.signatureAndMissingEntailmentComponent, BoxLayout.PAGE_AXIS));
        int idx = 0;
        if (this.signatureAndMissingEntailmentTabbedPane != null){
            idx = this.signatureAndMissingEntailmentTabbedPane.getSelectedIndex();
        }
        this.signatureAndMissingEntailmentTabbedPane = new JTabbedPane();
        this.signatureAndMissingEntailmentTabbedPane.addTab(MISSING_ENTAILMENT_TAB_NAME, null,
                this.missingEntailmentManagementPanel, MISSING_ENTAILMENT_TAB_TOOLTIP);
        this.signatureAndMissingEntailmentTabbedPane.addTab(SIGNATURE_MANAGEMENT_TAB_NAME, null,
                this.signatureManagementPanel, SIGNATURE_MANAGEMENT_TAB_TOOLTIP);
        this.signatureAndMissingEntailmentTabbedPane.setSelectedIndex(idx);
        JPanel signatureAndMissingEntailmentTabbedPaneHolderPanel = new JPanel();
        signatureAndMissingEntailmentTabbedPaneHolderPanel.setLayout(
                new BoxLayout(signatureAndMissingEntailmentTabbedPaneHolderPanel, BoxLayout.PAGE_AXIS));
        signatureAndMissingEntailmentTabbedPaneHolderPanel.add(this.signatureAndMissingEntailmentTabbedPane);
        this.signatureAndMissingEntailmentComponent.add(signatureAndMissingEntailmentTabbedPaneHolderPanel);
        if (this.signatureManagementPanel.getMinimumSize().getWidth() >
                this.missingEntailmentManagementPanel.getMinimumSize().getWidth()){
            this.signatureAndMissingEntailmentComponent.setMinimumSize(new Dimension(
//                    minimum width of component
                    (int) this.signatureManagementPanel.getMinimumSize().getWidth()
//                            component embedded in gridBagLayout of holderPanel
                            + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                    minimum height of component
                    (int) this.signatureManagementPanel.getMinimumSize().getHeight()
//                            component embedded in gridBagLayout of holderPanel
                            + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom
            ));
        } else {
            this.signatureAndMissingEntailmentComponent.setMinimumSize(new Dimension(
//                    minimum width of component
                    (int) this.missingEntailmentManagementPanel.getMinimumSize().getWidth()
//                            component embedded in gridBagLayout of holderPanel
                            + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                    minimum height of component
                    (int) this.missingEntailmentManagementPanel.getMinimumSize().getHeight()
//                            component embedded in gridBagLayout of holderPanel
                            + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom
            ));

        }
    }

    private void resetExplanationServiceComponent(){
        this.logger.debug("Resetting explanation service component");
            this.nonEntailmentExplanationServiceComponent = new JPanel();
            this.nonEntailmentExplanationServiceComponent.setLayout(new BoxLayout(
                    this.nonEntailmentExplanationServiceComponent, BoxLayout.PAGE_AXIS));
            this.resultHolderComponent = new JPanel();
            this.resultHolderComponent.setLayout(new BoxLayout(this.resultHolderComponent, BoxLayout.PAGE_AXIS));
            INonEntailmentExplanationService<?> explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
            if (explainer != null){
                this.logger.debug("Explainer available");
                this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
            }
            else {
                this.logger.debug("No explainer available");
                this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
            }
    }

    private void resetHorizontalSplitPane(){
        this.logger.debug("Resetting horizontal split-pane");
        this.horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                this.signatureAndMissingEntailmentComponent,
                this.nonEntailmentExplanationServiceComponent);
        this.horizontalSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                propertyChangeEvent -> {
                    if (nonEntailmentExplainerManager.getCurrentExplainer() != null &&
                            nonEntailmentExplanationServiceComponent != null &&
                    resultHolderComponent.getComponentCount() != 0){
                        logger.debug("Movement of horizontal divider detected");
                        nonEntailmentExplainerManager.getCurrentExplainer().repaintResultComponent();
                    }
                });
        this.horizontalSplitPane.setDividerLocation(0.3);
    }

    private void resetHolderPanel(){
        this.logger.debug("Resetting main holder panel");
        this.holderPanel = new JPanel(new GridBagLayout());
//        for resizing of arrow-buttons on change of font via Protégé preferences
        this.holderPanel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                UIUtilities.revalidateAndRepaintComponent(holderPanel);
            }

            @Override
            public void componentMoved(ComponentEvent ignored) {

            }

            @Override
            public void componentShown(ComponentEvent ignored) {

            }

            @Override
            public void componentHidden(ComponentEvent ignored) {

            }
        });
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        upper panel constraints:
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        constraints.weightx = 0.1;
        constraints.weighty = 0.001;
        this.holderPanel.add(this.serviceSelectionComponent, constraints);
//        lower panel constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.999;
        this.holderPanel.add(this.horizontalSplitPane, constraints);
    }

    private void addHolderPanel(){
        this.removeAll();
        this.add(holderPanel);
    }

    private void repaintComponents(){
        UIUtilities.revalidateAndRepaintComponent(this);
    }

    private void resetResultComponent(){
        JComponent oldExplanationServiceComponent = this.nonEntailmentExplanationServiceComponent;
        this.resetExplanationServiceComponent();
        JComponent newExplanationServiceComponent = this.nonEntailmentExplanationServiceComponent;
        this.horizontalSplitPane.remove(oldExplanationServiceComponent);
        this.horizontalSplitPane.add(newExplanationServiceComponent);
        this.resultHolderComponent.removeAll();
        this.repaintComponents();
    }

    private void resetSignatureSelectionComponent(){
        JPanel nonScrollableSignatureManagementPanel = this.signatureSelectionUI.getSignatureManagementComponent();
        JScrollPane signatureManagementScrollPane =
                ComponentFactory.createScrollPane(nonScrollableSignatureManagementPanel);
        this.signatureManagementPanel = new JPanel(new BorderLayout());
        this.signatureManagementPanel.add(signatureManagementScrollPane);
        this.signatureManagementPanel.setMinimumSize(nonScrollableSignatureManagementPanel.getMinimumSize());
    }

    private void createMissingEntailmentManagementComponent(){
        JPanel missingEntailmentManagementInnerPanel = new JPanel();
        missingEntailmentManagementInnerPanel.setLayout(new GridBagLayout());
        JPanel missingEntailmentTextPanel = this.createMissingEntailmentTextPanel();
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for editor panel:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        missingEntailmentManagementInnerPanel.add(missingEntailmentTextPanel, constraints);
        JPanel buttonPanel = this.createMissingEntailmentButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        missingEntailmentManagementInnerPanel.add(buttonPanel, constraints);
        JPanel selectedMissingEntailmentPanel = this.createSelectedMissingEntailmentPanel();
//        specific for selected missing entailment:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        missingEntailmentManagementInnerPanel.add(selectedMissingEntailmentPanel, constraints);
        missingEntailmentManagementInnerPanel.setMinimumSize(new Dimension(
//                width of each component
                (int) ceil(max(this.wideComponentDimensionList.stream().map(Dimension::getWidth).
                        collect(Collectors.toList())))
//                        component embedded in gridBagLayout of missingEntailmentManagementPanel
                        + this.STANDARD_INSETS.left + this.STANDARD_INSETS.right,
//                height of each component
                (int) ceil(max(this.wideComponentDimensionList.stream().map(Dimension::getHeight).
                        collect(Collectors.toList())))
//                        component embedded in gridBagLayout of missingEntailmentManagementPanel
                        + this.STANDARD_INSETS.top + this.STANDARD_INSETS.bottom
        ));
        JScrollPane signatureManagementScrollPane = ComponentFactory.createScrollPane(
                missingEntailmentManagementInnerPanel);
        this.missingEntailmentManagementPanel = new JPanel(new BorderLayout());
        this.missingEntailmentManagementPanel.add(signatureManagementScrollPane);
        this.missingEntailmentManagementPanel.setMinimumSize(missingEntailmentManagementInnerPanel.getMinimumSize());
    }

    private JPanel createMissingEntailmentTextPanel(){
        JPanel missingEntailmentEditorPanel = new JPanel();
        missingEntailmentEditorPanel.setLayout(new BoxLayout(missingEntailmentEditorPanel, BoxLayout.PAGE_AXIS));
        OWLExpressionChecker<OWLAxiom> logicalAxiomChecker =
                new OWLLogicalAxiomChecker(this.getOWLModelManager());
        this.missingEntailmentTextEditor = new ExpressionEditor<>(this.getOWLEditorKit(), logicalAxiomChecker);
        JScrollPane editorScrollPane = ComponentFactory.createScrollPane(this.missingEntailmentTextEditor);
//        editorScrollPane.setPreferredSize(new Dimension(400, 400));
        missingEntailmentEditorPanel.add(editorScrollPane);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "Enter missing entailment:");
        missingEntailmentEditorPanel.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int titleWidth = (int) titledBorder.getMinimumSize(missingEntailmentEditorPanel).getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(missingEntailmentEditorPanel).getHeight() + 5;
        this.wideComponentDimensionList.add(new Dimension(titleWidth, titleHeight));
        return missingEntailmentEditorPanel;
    }

    private JPanel createMissingEntailmentButtonPanel(){
        JPanel buttonHolderPanel = new JPanel();
        buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.PAGE_AXIS));
        JPanel firstButtonRowPanel = new JPanel();
        firstButtonRowPanel.setLayout(new BoxLayout(firstButtonRowPanel, BoxLayout.LINE_AXIS));
        JButton addMissingEntailmentButton = UIUtilities.createNamedButton(ADD_MISSING_ENTAILMENT_COMMAND,
                ADD_MISSING_ENTAILMENT_NAME, ADD_MISSING_ENTAILMENT_TOOLTIP, this);
        firstButtonRowPanel.add(addMissingEntailmentButton);
        firstButtonRowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton deleteMissingEntailmentButton = UIUtilities.createNamedButton(DELETE_MISSING_ENTAILMENT_COMMAND,
                DELETE_MISSING_ENTAILMENT_NAME, DELETE_MISSING_ENTAILMENT_TOOLTIP, this);
        firstButtonRowPanel.add(deleteMissingEntailmentButton);
        firstButtonRowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton resetMissingEntailmentButton = UIUtilities.createNamedButton(RESET_MISSING_ENTAILMENT_COMMAND,
                RESET_MISSING_ENTAILMENT_NAME, RESET_MISSING_ENTAILMENT_TOOLTIP, this);
        firstButtonRowPanel.add(resetMissingEntailmentButton);
        buttonHolderPanel.add(firstButtonRowPanel);
        if (! this.preferencesManager.loadUseSimpleMode()){
            buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel secondButtonRowPanel = new JPanel();
            secondButtonRowPanel.setLayout(new BoxLayout(secondButtonRowPanel, BoxLayout.LINE_AXIS));
            JButton loadMissingEntailmentButton = UIUtilities.createNamedButton(LOAD_MISSING_ENTAILMENT_COMMAND,
                    LOAD_MISSING_ENTAILMENT_BUTTON_NAME, LOAD_MISSING_ENTAILMENT_TOOLTIP, this);
            secondButtonRowPanel.add(loadMissingEntailmentButton);
            secondButtonRowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            JButton saveMissingEntailmentButton = UIUtilities.createNamedButton(SAVE_MISSING_ENTAILMENT_COMMAND,
                    SAVE_MISSING_ENTAILMENT_BUTTON_NAME, SAVE_MISSING_ENTAILMENT_TOOLTIP, this);
            secondButtonRowPanel.add(saveMissingEntailmentButton);
            buttonHolderPanel.add(secondButtonRowPanel);
            buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        }
        return buttonHolderPanel;
    }

    private JPanel createSelectedMissingEntailmentPanel(){
        JPanel missingEntailmentPanel = new JPanel();
        missingEntailmentPanel.setLayout(new BoxLayout(missingEntailmentPanel, BoxLayout.PAGE_AXIS));
        this.selectedMissingEntailmentListModel = new OWLObjectListModel<>(this.getOWLEditorKit());
        this.selectedMissingEntailmentList = new JList<>(this.selectedMissingEntailmentListModel);
        this.selectedMissingEntailmentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    JList list = (JList) e.getSource();
                    Object selectedValue = list.getSelectedValue();
                    if (selectedValue instanceof OWLObject){
                        missingEntailmentTextEditor.setText(reverseParseOWLObject((OWLObject) selectedValue));
                    }
                }
            }
        });
        OWLCellRenderer renderer = new OWLCellRenderer(this.getOWLEditorKit());
        renderer.setHighlightKeywords(true);
        renderer.setHighlightUnsatisfiableClasses(false);
        renderer.setHighlightUnsatisfiableProperties(false);
        this.selectedMissingEntailmentList.setCellRenderer(renderer);
        JScrollPane scrollPane = ComponentFactory.createScrollPane(this.selectedMissingEntailmentList);
        missingEntailmentPanel.add(scrollPane);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(
                5, 5, 5, 5),
                "Selected missing entailment(s):");
        missingEntailmentPanel.setBorder(BorderFactory.createCompoundBorder(
                titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        int titleWidth = (int) titledBorder.getMinimumSize(missingEntailmentPanel).getWidth() + 5;
        int titleHeight = (int) titledBorder.getMinimumSize(missingEntailmentPanel).getHeight() + 5;
        this.wideComponentDimensionList.add(new Dimension(titleWidth, titleHeight));
        return missingEntailmentPanel;
    }

    private void createGeneralSettingsComponent(){
        this.serviceSelectionComponent = new JPanel();
        this.serviceSelectionComponent.setLayout(new BoxLayout(this.serviceSelectionComponent, BoxLayout.PAGE_AXIS));
        Vector<String> serviceNames = this.nonEntailmentExplainerManager.getExplanationServiceNames();
        Collections.sort(serviceNames);
        this.serviceNamesComboBox = new JComboBox<>(serviceNames);
        this.serviceNamesComboBox.addActionListener(this);
        this.serviceSelectionComponent.add(this.serviceNamesComboBox);
        this.serviceSelectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = UIUtilities.createNamedButton(COMPUTE_COMMAND,
                COMPUTE_NAME, COMPUTE_TOOLTIP, this);
        this.computeButton.setEnabled(false);
        JPanel buttonAndMessagePanel = new JPanel();
        buttonAndMessagePanel.setLayout(new BoxLayout(buttonAndMessagePanel, BoxLayout.LINE_AXIS));
        buttonAndMessagePanel.add(this.computeButton);
        buttonAndMessagePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        this.computeMessageLabel = UIUtilities.createLabel("");
        buttonAndMessagePanel.add(this.computeMessageLabel);
        buttonAndMessagePanel.add(Box.createGlue());
        JPanel generalSettingsHolderPanel = new JPanel();
        generalSettingsHolderPanel.setLayout(new BoxLayout(generalSettingsHolderPanel, BoxLayout.PAGE_AXIS));
        generalSettingsHolderPanel.add(buttonAndMessagePanel);
        generalSettingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.filterWarningLabel = UIUtilities.createLabel("");
        JPanel filterWarningPanel = new JPanel();
        filterWarningPanel.setLayout(new BoxLayout(filterWarningPanel, BoxLayout.LINE_AXIS));
        filterWarningPanel.add(this.filterWarningLabel);
        filterWarningPanel.add(Box.createGlue());
        generalSettingsHolderPanel.add(filterWarningPanel);
        this.serviceSelectionComponent.add(generalSettingsHolderPanel);
        this.serviceSelectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
        this.serviceSelectionComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Missing Entailment Explanation Service:"),
//                BorderFactory.createLineBorder(Color.BLUE)));
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

//    Various Event-Handling methods:
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox){
            this.logger.debug("Selected <Missing Entailment Explanation Service> changed");
            JComboBox comboBox = (JComboBox) e.getSource();
            String serviceName = (String) comboBox.getSelectedItem();
            if (! this.nonEntailmentExplainerManager.isCurrentExplanationService(serviceName)){
                this.nonEntailmentExplainerManager.setExplanationService(serviceName);
                this.filterWarningLabel.setText("");
                this.resetResultComponent();
                this.checkComputeButtonAndWarningLabelStatus();
                this.signatureSelectionUI.resetSelectedSignature();
            }
        }
        else{
            switch (e.getActionCommand()){
                case COMPUTE_COMMAND:
                    this.computeExplanation();
                    break;
                case ADD_MISSING_ENTAILMENT_COMMAND:
                    this.addMissingEntailment();
                    this.signatureSelectionUI.resetSelectedSignature();
                    break;
                case DELETE_MISSING_ENTAILMENT_COMMAND:
                    this.deleteMissingEntailment();
                    this.signatureSelectionUI.resetSelectedSignature();
                    break;
                case RESET_MISSING_ENTAILMENT_COMMAND:
                    this.resetMissingEntailment();
                    this.signatureSelectionUI.resetSelectedSignature();
                    break;
                case LOAD_MISSING_ENTAILMENT_COMMAND:
                    this.loadMissingEntailment();
                    this.signatureSelectionUI.resetSelectedSignature();
                    break;
                case SAVE_MISSING_ENTAILMENT_COMMAND:
                    this.saveMissingEntailment();
                    break;
            }
        }
    }

    @Override
    public void handleEvent(ExplanationEvent<INonEntailmentExplanationService<?>> event){
        this.logger.debug("Handling explanationEvent: {} of source: {}",
                event.getType(), event.getSource().getClass());
        INonEntailmentExplanationService<?> currentExplaier =
                this.nonEntailmentExplainerManager.getCurrentExplainer();
        if (event.getSource().equals(currentExplaier)){
            switch (event.getType()){
                case COMPUTATION_COMPLETE :
                    SwingUtilities.invokeLater(() ->{
                        this.disposeLoadingScreen();
                        this.showResult(event.getSource().getResult());
                        if (currentExplaier.ignoresPartsOfOntology()){
                            this.filterWarningLabel.setText(currentExplaier.getFilterWarningMessage());
                            if (this.preferencesManager.loadShowFilterWarningMessage()){
                                this.showFilterPopupWarning();
                            }
                        }
                    });
                    break;
                case ERROR :
                    SwingUtilities.invokeLater(() -> {
                        this.disposeLoadingScreen();
                        this.resetMainComponent();
                        this.repaintComponents();
                        UIUtilities.showError(event.getSource().getErrorMessage(), this.getOWLEditorKit());
                    });
                    break;
                case WARNING :
                    SwingUtilities.invokeLater(() -> {
                        this.disposeLoadingScreen();
                        this.resetMainComponent();
                        this.repaintComponents();
                    });
                case RESULT_RESET:
                    SwingUtilities.invokeLater(() -> {
                        this.disposeLoadingScreen();
                        this.resetResultComponent();
                    });
                    break;
                case SHOW_LOADING_SCREEN:
                        this.filterWarningLabel.setText("");
                        SwingUtilities.invokeLater(() -> {
                            this.loadingUI.resetLoadingUI();
                            this.loadingUI.activateLoadingUI();
                        });
                        this.logger.debug("loading UI is activated");
                        INonEntailmentExplanationService<?> explainer =
                                this.nonEntailmentExplainerManager.getCurrentExplainer();
                        NonEntailmentExplanationProgressTracker progressTracker =
                                new NonEntailmentExplanationProgressTracker();
                        progressTracker.registerLoadingUIListener(this.loadingUI);
                        explainer.addProgressTracker(progressTracker);
                    break;
                case IGNORE_ONTOLOGY_CHANGE:
                    this.ignoreOntologyChangeEvent = true;
                    break;
            }
        } else{
            this.logger.debug("EventSource is NOT the current explainer, event ignored");
        }
    }

    @Override
    public void handleUIEvent(ExplanationLoadingScreenEvent event) {
        if (event.getType().equals(
                ExplanationLoadingUIEventType
                        .EXPLANATION_GENERATION_CANCELLED)){
            INonEntailmentExplanationService<?> service =
                    this.nonEntailmentExplainerManager.getCurrentExplainer();
            this.logger.debug("Cancelling non entailment explanation generation of service {}", service);
            service.cancel();
        }
    }

    @Override
    public void handlePreferenceChange(GeneralPreferencesChangeEvent event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()){
                case LAYOUT_CHANGE:
                    this.wideComponentDimensionList.clear();
                    this.createGeneralSettingsComponent();
                    this.resetMainComponent();
                    this.repaintComponents();
                    break;
                case SIMPLE_MODE_CHANGE:
                    this.signatureSelectionUI.resetVocabularyManagementPanel();
                    this.wideComponentDimensionList.clear();
                    this.createGeneralSettingsComponent();
                    this.resetSignatureSelectionComponent();
                    this.createMissingEntailmentManagementComponent();
                    this.resetMainComponent();
                    break;
            }
        });
    }

    @Override
    public void handleSignatureModificationEvent(SignatureModificationEvent event) {
        Set<OWLEntity> additionalSignatureNames = event.getAdditionalSignatureNames();
//        if signature is changed by outside event, show signature tab as confirmation
        this.signatureAndMissingEntailmentTabbedPane.setSelectedIndex(1);
        this.signatureSelectionUI.addNamesToSignature(additionalSignatureNames);
    }

//    Event-Handling related methods:
    private void computeExplanation(){
        this.logger.debug("Computation of explanation requested");
        this.filterWarningLabel.setText("");
        SwingUtilities.invokeLater(() -> {
            this.loadingUI.resetLoadingUI();
            this.loadingUI.activateLoadingUI();
        });
        this.logger.debug("loading UI is activated");
        INonEntailmentExplanationService<?> explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        explainer.setOntology(this.getOWLModelManager().getActiveOntology());
        explainer.setSignature(this.signatureSelectionUI.getPermittedVocabulary());
        explainer.setObservation(new HashSet<>(this.selectedMissingEntailmentListModel.getOwlObjects()));
        NonEntailmentExplanationProgressTracker progressTracker = new NonEntailmentExplanationProgressTracker();
        progressTracker.registerLoadingUIListener(this.loadingUI);
        explainer.addProgressTracker(progressTracker);
        explainer.computeExplanation();
    }

    private void showResult(Component resultComponent){
//        this.resetMainComponent();
        this.resultHolderComponent.removeAll();
        this.resultHolderComponent.add(resultComponent);
        this.repaintComponents();
    }

    private void addMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
        try{
            OWLAxiom axiomToAdd = this.missingEntailmentTextEditor.createObject();
            this.missingEntailmentTextEditor.setText("");
            AtomicBoolean signatureContainedInOntology = new AtomicBoolean(true);
            OWLOntology activeOntology = this.getOWLModelManager().getActiveOntology();
            axiomToAdd.getSignature().forEach(entity -> {
                if (! activeOntology.getSignature(Imports.INCLUDED).contains(entity)){
                    signatureContainedInOntology.set(false);
                }
            });
            if (signatureContainedInOntology.get()){
                this.logger.debug("Signature of entered axiom is part of ontology signature, " +
                        "axiom added to missing entailment.");
                this.selectedMissingEntailmentListModel.checkAndAddElement(axiomToAdd);
            } else {
                this.logger.debug("Signature of entered axiom is NOT part of ontology signature, " +
                        "axiom ignored.");
                UIUtilities.showWarning(
                        "Signature of entered axiom is not part of the ontology signature!",
                        this.getOWLEditorKit());
            }
        }
        catch (OWLException e) {
            this.logger.debug("Exception caught when trying to add missing entailment: ", e);
            UIUtilities.showError(e.getMessage(), this.getOWLEditorKit());
        }
        finally {
            this.selectedMissingEntailmentList.clearSelection();
            this.checkComputeButtonAndWarningLabelStatus();
        }
//        });
    }

    private void deleteMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
        List<OWLAxiom> toDelete = this.selectedMissingEntailmentList.getSelectedValuesList();
        this.selectedMissingEntailmentListModel.removeElements(toDelete);
        this.selectedMissingEntailmentList.clearSelection();
        this.missingEntailmentTextEditor.setText("");
        this.checkComputeButtonAndWarningLabelStatus();
//        });
    }

    private void resetMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
        this.selectedMissingEntailmentListModel.removeAll();
        this.selectedMissingEntailmentList.clearSelection();
        this.missingEntailmentTextEditor.setText("");
        this.checkComputeButtonAndWarningLabelStatus();
//        });
    }

    private JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }

    private void loadMissingEntailment() {
        this.logger.debug("Loading missing entailment from file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this);
        Set<OWLLogicalAxiom> missingEntailmentAxioms = new HashSet<>();
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try {
                OWLOntology missingEntailmentOntology = manager.loadOntologyFromOntologyDocument(file);
                OWLOntology activeOntology = this.getOWLEditorKit().getOWLModelManager().getActiveOntology();
                Set<OWLLogicalAxiom> loadedAxioms = missingEntailmentOntology.getLogicalAxioms();
                missingEntailmentAxioms = loadedAxioms.stream().filter(
                        axiom -> activeOntology.getSignature().containsAll(
                                axiom.getSignature())).collect(Collectors.toSet());
            } catch (OWLOntologyCreationException e) {
                this.logger.error("Error when loading missing entailment from file: ", e);
                UIUtilities.showError(e.getMessage(), this.getOWLEditorKit());
            }
        }
        this.selectedMissingEntailmentListModel.removeAll();
        this.selectedMissingEntailmentListModel.addElements(missingEntailmentAxioms);
        this.selectedMissingEntailmentList.clearSelection();
        this.checkComputeButtonAndWarningLabelStatus();
    }

    private void saveMissingEntailment(){
        this.logger.debug("Saving missing entailment to file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            try {
                String missingEntailmentOntologyName =
                        this.getOWLEditorKit().getOWLModelManager().getActiveOntology().
                                getOntologyID().getOntologyIRI() + "missingEntailmentOntology";
                OWLOntology missingEntailmentOntology = ontologyManager.createOntology(
                        IRI.create(missingEntailmentOntologyName));
                ontologyManager.addAxioms(missingEntailmentOntology, new HashSet<>(
                        this.selectedMissingEntailmentListModel.getOwlObjects()));
                ontologyManager.saveOntology(missingEntailmentOntology,
                        new RDFXMLDocumentFormat(), new FileOutputStream(file));
            } catch (OWLOntologyCreationException |
                     OWLOntologyStorageException |
                     FileNotFoundException exception) {
                this.logger.error("Error when saving missing entailment ontology to file: ", exception);
                UIUtilities.showError(exception.getMessage(), this.getOWLEditorKit());
            }
        }
        this.selectedMissingEntailmentList.clearSelection();
    }

    private String reverseParseOWLObject(OWLObject owlObject){
        return this.getOWLEditorKit().getOWLModelManager().getRendering(owlObject);
    }

    protected void checkComputeButtonAndWarningLabelStatus(){
        this.logger.debug("Changing Compute-Button status");
        INonEntailmentExplanationService<?> currentExplainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
//        SwingUtilities.invokeLater(() -> {
        if (currentExplainer == null) {
            this.computeButton.setEnabled(false);
        } else {
            assertNotNull(this.getOWLModelManager().getActiveOntology());
            assertNotNull(this.signatureSelectionUI.getPermittedVocabulary());
            assertNotNull(this.selectedMissingEntailmentListModel.getOwlObjects());
            currentExplainer.setOntology(this.getOWLModelManager().getActiveOntology());
            currentExplainer.setSignature(this.signatureSelectionUI.getPermittedVocabulary());
            currentExplainer.setObservation(new HashSet<>(this.selectedMissingEntailmentListModel.getOwlObjects()));
            boolean enabled = currentExplainer.supportsExplanation();
            if (enabled) {
                this.computeMessageLabel.setText("");
            } else {
                this.computeMessageLabel.setText(currentExplainer.getSupportsExplanationMessage());
            }
            this.computeButton.setEnabled(enabled);
//                this.resetView();
        }
//        });
        this.repaintComponents();
    }

    private void showFilterPopupWarning(){
        JDialog filterWarningDialog = new JDialog(ProtegeManager.getInstance().getFrame(
                this.getOWLEditorKit().getWorkspace()));
        filterWarningDialog.setTitle("Warning");
        filterWarningDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        JPanel filterWarningPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        INonEntailmentExplanationService<?> currentExplainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        JLabel filterWarningLabel = new JLabel(currentExplainer.getFilterWarningMessage(), SwingConstants.CENTER);
        filterWarningLabel.setHorizontalTextPosition(JLabel.CENTER);
        filterWarningLabel.setVerticalTextPosition(JLabel.CENTER);
        JCheckBox filterWarningCheckBox = new JCheckBox("Don't show this message again", false);
        filterWarningCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                preferencesManager.saveShowFilterWarningMessage(false);
            }
        });
        JButton filterWarningButton = new JButton("OK");
        filterWarningButton.addActionListener(e -> filterWarningDialog.dispose());
        filterWarningCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        filterWarningPanel.add(filterWarningLabel);
        filterWarningPanel.add(filterWarningCheckBox);
        filterWarningPanel.add(filterWarningButton);
        filterWarningDialog.getContentPane().add(filterWarningPanel);
        UIUtilities.packAndSetWindow(filterWarningDialog, this.getOWLEditorKit(), true);
    }

    private void disposeLoadingScreen(){
        if (this.loadingUI != null) {
            this.loadingUI.resetLoadingUI();
        }
    }
//****************************************************************************
//    Ontology-Change Listeners:
//****************************************************************************
    private class ViewComponentOntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                    changeEvent.isType(EventType.ONTOLOGY_RELOADED)) {
                logger.debug("Change or reload of active ontology detected");
                selectedMissingEntailmentListModel.removeAll();
                change();
            }
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            logger.debug("Change to ontology detected");
            if (ignoreOntologyChangeEvent){
                logger.debug("Change ignored due to previous IGNORE_ONTOLOGY_CHANGE-event");
                ignoreOntologyChangeEvent = false;
            } else {
                for (OWLOntologyChange change: list){
                    if (change.getOntology().equals(getOWLEditorKit().getOWLModelManager().getActiveOntology())){
                        logger.debug("Change made to active ontology");
                        for (OWLOntologyChange ontoChanges : list){
                            Set<OWLEntity> entities = ontoChanges.getSignature();
                            for (OWLAxiom axiom : selectedMissingEntailmentListModel.getOwlObjects()){
                                if (axiom.getSignature().stream().anyMatch(entities::contains)){
                                    selectedMissingEntailmentListModel.removeElement(axiom);
                                }
                            }
                        }
                        change();
                        break;
                    }
                }
            }
        }

        private void change(){
            INonEntailmentExplanationService<?> explainer = nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(getOWLModelManager().getActiveOntology());
            resetResultComponent();
//            resetExplanationServiceComponent();
//            resetHorizontalSplitPane();
//            resetHolderPanel();
//            addHolderPanel();
//            repaintComponents();
            checkComputeButtonAndWarningLabelStatus();
            computeMessageLabel.setText("");
            filterWarningLabel.setText("");
        }
    }

//****************************************************************************
//    TextEditor Input Checker:
//****************************************************************************
    private static class OWLLogicalAxiomChecker implements OWLExpressionChecker<OWLAxiom>{

        private final OWLModelManager manager;
        private final Logger logger = LoggerFactory.getLogger(OWLLogicalAxiomChecker.class);

        public OWLLogicalAxiomChecker(OWLModelManager manager){
            this.manager = manager;
        }

        @Override
        public void check(String input) throws OWLExpressionParserException {
            if (input.length() != 0){
                this.createObject(input);
            }
        }

        @Override
        public OWLAxiom createObject(String input) throws OWLExpressionParserException {
            ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(
                    OWLOntologyLoaderConfiguration::new,
                    this.manager.getOWLDataFactory());
            parser.setOWLEntityChecker(
                    new ProtegeOWLEntityChecker(
                            this.manager.getOWLEntityFinder()));
            parser.setStringToParse(input);
            try {
                OWLAxiom axiom = parser.parseAxiom();
                if(axiom.isLogicalAxiom()) {
                    return axiom;
                }
                else {
                    throw new OWLExpressionParserException(
                            "Expected a logical axiom"
                            , 0, 0, true, true, true, true, true, false, Collections.emptySet());
                }
            }
            catch (ParserException e) {
//                no logging done as exception is thrown during typing of axiom after each keystroke
                throw ParserUtil.convertException(e);
            }
        }
    }

}
