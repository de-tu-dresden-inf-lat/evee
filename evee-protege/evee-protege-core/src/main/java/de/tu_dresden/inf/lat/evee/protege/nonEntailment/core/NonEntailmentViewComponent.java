package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.NonEntailmentExplanationLoadingUIManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.NonEntailmentExplanationProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPlugin;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPluginLoader;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEventType;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.apache.commons.io.FilenameUtils;
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
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

import static org.junit.Assert.assertNotNull;

public class NonEntailmentViewComponent extends AbstractOWLViewComponent
        implements ActionListener,
        IExplanationLoadingUIListener,
        IExplanationGenerationListener<
                ExplanationEvent<
                        INonEntailmentExplanationService<?>>> {

    private final NonEntailmentExplainerManager nonEntailmentExplainerManager;
    private final ViewComponentOntologyChangeListener changeListener;

    private NonEntailmentVocabularySelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> missingEntailmentTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedMissingEntailmentListModel;
    private JList<OWLAxiom> selectedmissingEntailmentList;
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
//    private JPanel splitPaneHolderComponent;
    private JComboBox<String> serviceNamesComboBox;
    private JLabel computeMessageLabel;
    protected NonEntailmentExplanationLoadingUIManager loadingUI;
    private static final String COMPUTE_COMMAND = "COMPUTE_NON_ENTAILMENT";
    private static final String COMPUTE_NAME = "Generate Explanation";
    private static final String COMPUTE_TOOLTIP = "Generate non-entailment explanation using selected vocabulary and missing entailment";
    private static final String ADD_MISSING_ENTAILMENT_COMMAND = "ADD_MISSING_ENTAILMENT";
    private static final String ADD_MISSING_ENTAILMLENT_NAME = "Add";
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

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentViewComponent.class);

    public NonEntailmentViewComponent(){
        this.nonEntailmentExplainerManager = new NonEntailmentExplainerManager();
        this.changeListener = new ViewComponentOntologyChangeListener();
        this.loadingUI = new NonEntailmentExplanationLoadingUIManager(DEFAULT_UI_TITLE);
        this.loadingUI.registerLoadingUIListener(this);
        this.logger.debug("Object NonEntailmentViewComponent created");
    }

    protected ArrayList<OWLObject> getMissingEntailments(){
        return new ArrayList<>(this.selectedMissingEntailmentListModel.getOwlObjects());
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
                this.nonEntailmentExplainerManager.registerNonEntailmentExplanationService(service, plugin.getName());
            }
            catch (Exception e){
                this.logger.error("Error while loading non-entailment explanation plugin:\n", e);
            }
        }
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.createGeneralSettingsComponent();
        this.nonEntailmentExplainerManager.setExplanationService((String) this.serviceNamesComboBox.getSelectedItem());
        this.createSignatureManagementComponent();
        this.createMissingEntailmentManagementComponent();
        this.resetMainComponent();
        this.getOWLEditorKit().getOWLModelManager().addListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.changeComputeButtonStatus();
        this.logger.debug("initialisation completed");
    }

    private void resetMainComponent(){
        this.logger.debug("Resetting entire view component");
        this.resetSignatureAndMissingEntailmentComponent();
        this.resetExplanationServiceComponent();
        this.resetHorizontalSplitPane();
        this.resetHolderPanel();
        this.addHolderPanel();
    }

//    private void resetServiceSelectionComponent(){
//        SwingUtilities.invokeLater(() -> {
//            String currentExplainer = null;
//            if (this.serviceNamesComboBox != null){
//                currentExplainer = (String) this.serviceNamesComboBox.getSelectedItem();
//            }
//            this.createGeneralSettingsComponent();
//            if (currentExplainer != null){
//                this.serviceNamesComboBox.setSelectedItem(currentExplainer);
//            }
//            this.changeComputeButtonStatus();
//        });
//    }
//
    private void resetSignatureAndMissingEntailmentComponent(){
        this.logger.debug("Resetting vocabulary and missing entailment component");
        this.signatureAndMissingEntailmentComponent = new JPanel();
        this.signatureAndMissingEntailmentComponent.setLayout(new BoxLayout(this.signatureAndMissingEntailmentComponent, BoxLayout.PAGE_AXIS));
        int idx = 0;
        if (this.signatureAndMissingEntailmentTabbedPane != null){
            idx = this.signatureAndMissingEntailmentTabbedPane.getSelectedIndex();
        }
        this.signatureAndMissingEntailmentTabbedPane = new JTabbedPane();
        this.signatureAndMissingEntailmentTabbedPane.addTab("Missing Entailment", this.missingEntailmentManagementPanel);
        this.signatureAndMissingEntailmentTabbedPane.addTab("Vocabulary", this.signatureManagementPanel);
        this.signatureAndMissingEntailmentTabbedPane.setSelectedIndex(idx);
        this.signatureAndMissingEntailmentComponent.add(this.signatureAndMissingEntailmentTabbedPane);
    }
//
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
                if (explainer.getSettingsComponent() != null){
                    this.logger.debug("Adding settings to explanationServiceComponent");
                    JSplitPane serviceSettingsAndResultSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                            explainer.getSettingsComponent(), this.resultHolderComponent);
                    serviceSettingsAndResultSplitPane.setDividerLocation(0.3);
                    this.nonEntailmentExplanationServiceComponent.add(serviceSettingsAndResultSplitPane);
                }
                else {
                    this.logger.debug("No settings available");
                    this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
                }
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
        this.horizontalSplitPane.setDividerLocation(0.3);
    }

    private void resetHolderPanel(){
        this.logger.debug("Resetting main holder panel");
        this.holderPanel = new JPanel(new GridBagLayout());
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
        constraints.weighty = 0.1;
        this.holderPanel.add(this.serviceSelectionComponent, constraints);
//        lower panel constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.9;
        this.holderPanel.add(this.horizontalSplitPane, constraints);
    }

    private void addHolderPanel(){
        this.removeAll();
        this.add(holderPanel);
    }

    private void repaintComponents(){
        this.repaint();
        this.revalidate();
    }

    @Override
    protected void disposeOWLView() {
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
        this.loadingUI.dispose();
        this.getOWLEditorKit().getOWLModelManager().removeListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().removeOntologyChangeListener(this.changeListener);
        this.nonEntailmentExplainerManager.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox){
            JComboBox comboBox = (JComboBox) e.getSource();
            String serviceName = (String) comboBox.getSelectedItem();
            this.nonEntailmentExplainerManager.setExplanationService(serviceName);
            this.resetResultComponent();
            this.changeComputeButtonStatus();
        }
        else{
            switch (e.getActionCommand()){
                case COMPUTE_COMMAND:
                    this.computeExplanation();
                    break;
                case ADD_MISSING_ENTAILMENT_COMMAND:
                    this.addMissingEntailment();
                    break;
                case DELETE_MISSING_ENTAILMENT_COMMAND:
                    this.deleteMissingEntailment();
                    break;
                case RESET_MISSING_ENTAILMENT_COMMAND:
                    this.resetMissingEntailment();
                    break;
                case LOAD_MISSING_ENTAILMENT_COMMAND:
                    this.loadMissingEntailment();
                    break;
                case SAVE_MISSING_ENTAILMENT_COMMAND:
                    this.saveMissingEntailment();
                    break;
            }
        }
    }

    @Override
    public void handleEvent(ExplanationEvent<INonEntailmentExplanationService<?>> event){
        this.logger.debug("Handling explanationEvent: {} of source: {}", event.getType(), event.getSource());
        if (event.getSource().equals(
                this.nonEntailmentExplainerManager.getCurrentExplainer())){
            switch (event.getType()){
                case COMPUTATION_COMPLETE :
                    SwingUtilities.invokeLater(() ->{
                        this.disposeLoadingScreen();
                        this.showResult(event.getSource().getResult());
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
            }
        } else{
            this.logger.debug("EventSource is NOT the current explainer, event ignored");
        }
    }

    private void disposeLoadingScreen(){
        if (this.loadingUI != null) {
            this.loadingUI.resetLoadingUI();
        }
    }

    private void resetResultComponent(){
        resetExplanationServiceComponent();
        resetHorizontalSplitPane();
        resetHolderPanel();
        addHolderPanel();
        repaintComponents();
    }

    private void createSignatureManagementComponent(){
        JComponent ontologySignatureTabbedComponent = this.signatureSelectionUI.getOntologySignatureTabbedComponent();
        this.signatureManagementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
//        general:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for given signature tabbed pane:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.4;
        this.signatureManagementPanel.add(ontologySignatureTabbedComponent, constraints);
        JComponent signatureSelectionToolPanel = this.signatureSelectionUI.getSignatureSelectionButtonPanel();
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        this.signatureManagementPanel.add(signatureSelectionToolPanel, constraints);
        JComponent selectedVocabularyComponent = this.signatureSelectionUI.getSelectedVocabularyPanel();
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.signatureManagementPanel.add(selectedVocabularyComponent, constraints);
    }

    private void createMissingEntailmentManagementComponent(){
        this.missingEntailmentManagementPanel = new JPanel();
        this.missingEntailmentManagementPanel.setLayout(new GridBagLayout());
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
        constraints.weighty = 0.4;
        this.missingEntailmentManagementPanel.add(missingEntailmentTextPanel, constraints);
        JPanel buttonPanel = this.createMissingEntailmentButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        this.missingEntailmentManagementPanel.add(buttonPanel, constraints);
        JPanel selectedMissingEntailmentPanel = this.createSelectedMissingEntailmentPanel();
//        specific for selected missing entailment:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.missingEntailmentManagementPanel.add(selectedMissingEntailmentPanel, constraints);
    }

    private JPanel createMissingEntailmentTextPanel(){
        JPanel missingEntailmentEditorPanel = new JPanel();
        missingEntailmentEditorPanel.setLayout(new BoxLayout(missingEntailmentEditorPanel, BoxLayout.PAGE_AXIS));
        OWLExpressionChecker<OWLAxiom> logicalAxiomChecker =
                new OWLLogicalAxiomChecker(this.getOWLModelManager());
        this.missingEntailmentTextEditor = new ExpressionEditor<>(this.getOWLEditorKit(), logicalAxiomChecker);
        JScrollPane editorScrollPane = ComponentFactory.createScrollPane(this.missingEntailmentTextEditor);
        editorScrollPane.setPreferredSize(new Dimension(400, 400));
        missingEntailmentEditorPanel.add(editorScrollPane);
        missingEntailmentEditorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Enter Missing Entailment:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return missingEntailmentEditorPanel;
    }

    private JPanel createMissingEntailmentButtonPanel(){
        JPanel buttonHolderPanel = new JPanel();
        buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.PAGE_AXIS));
        JToolBar firstRowToolbar = new JToolBar();
        firstRowToolbar.setOrientation(JToolBar.HORIZONTAL);
        firstRowToolbar.setFloatable(false);
        firstRowToolbar.setLayout(new BoxLayout(firstRowToolbar, BoxLayout.LINE_AXIS));
        JButton addMissingEntailmentButton = UIUtilities.createNamedButton(ADD_MISSING_ENTAILMENT_COMMAND,
                ADD_MISSING_ENTAILMLENT_NAME, ADD_MISSING_ENTAILMENT_TOOLTIP, this);
        firstRowToolbar.add(addMissingEntailmentButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton deleteMissingEntailmentButton = UIUtilities.createNamedButton(DELETE_MISSING_ENTAILMENT_COMMAND,
                DELETE_MISSING_ENTAILMENT_NAME, DELETE_MISSING_ENTAILMENT_TOOLTIP, this);
        firstRowToolbar.add(deleteMissingEntailmentButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton resetMissingEntailmentButton = UIUtilities.createNamedButton(RESET_MISSING_ENTAILMENT_COMMAND,
                RESET_MISSING_ENTAILMENT_NAME, RESET_MISSING_ENTAILMENT_TOOLTIP, this);
        firstRowToolbar.add(resetMissingEntailmentButton);
        buttonHolderPanel.add(firstRowToolbar);
        buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JToolBar secondRowToolBar = new JToolBar();
        secondRowToolBar.setOrientation(JToolBar.HORIZONTAL);
        secondRowToolBar.setFloatable(false);
        secondRowToolBar.setLayout(new BoxLayout(secondRowToolBar, BoxLayout.LINE_AXIS));
        JButton loadMissingEntailmentButton = UIUtilities.createNamedButton(LOAD_MISSING_ENTAILMENT_COMMAND,
                LOAD_MISSING_ENTAILMENT_BUTTON_NAME, LOAD_MISSING_ENTAILMENT_TOOLTIP, this);
        secondRowToolBar.add(loadMissingEntailmentButton);
        secondRowToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton saveMissingEntailmentButton = UIUtilities.createNamedButton(SAVE_MISSING_ENTAILMENT_COMMAND,
                SAVE_MISSING_ENTAILMENT_BUTTON_NAME, SAVE_MISSING_ENTAILMENT_TOOLTIP, this);
        secondRowToolBar.add(saveMissingEntailmentButton);
        buttonHolderPanel.add(secondRowToolBar);
        buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonHolderPanel;
    }

    private JPanel createSelectedMissingEntailmentPanel(){
        JPanel missingEntailmentPanel = new JPanel();
        missingEntailmentPanel.setLayout(new BoxLayout(missingEntailmentPanel, BoxLayout.PAGE_AXIS));
        this.selectedMissingEntailmentListModel = new OWLObjectListModel<>();
        this.selectedmissingEntailmentList = new JList<>(this.selectedMissingEntailmentListModel);
        this.selectedmissingEntailmentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    JList list = (JList) e.getSource();
//                    todo: start here for bugfix on issue #73
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
        this.selectedmissingEntailmentList.setCellRenderer(renderer);
        JScrollPane scrollPane = new JScrollPane(this.selectedmissingEntailmentList);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        missingEntailmentPanel.add(scrollPane);
        missingEntailmentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Selected missing entailment:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return missingEntailmentPanel;
    }

    private void createGeneralSettingsComponent(){
        this.serviceSelectionComponent = new JPanel();
        this.serviceSelectionComponent.setLayout(new BoxLayout(this.serviceSelectionComponent, BoxLayout.PAGE_AXIS));
        Vector<String> serviceNames = this.nonEntailmentExplainerManager.getExplanationServiceNames();
        this.serviceNamesComboBox = new JComboBox<>(serviceNames);
        this.serviceNamesComboBox.addActionListener(this);
        this.serviceSelectionComponent.add(this.serviceNamesComboBox);
        this.serviceSelectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = UIUtilities.createNamedButton(COMPUTE_COMMAND,
                COMPUTE_NAME, COMPUTE_TOOLTIP, this);
        this.computeButton.setEnabled(false);
        JPanel buttonHelperPanel = new JPanel();
        buttonHelperPanel.setLayout(new BoxLayout(buttonHelperPanel, BoxLayout.LINE_AXIS));
        buttonHelperPanel.add(this.computeButton);
        buttonHelperPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        this.computeMessageLabel = UIUtilities.createLabel("");
        buttonHelperPanel.add(this.computeMessageLabel);
        buttonHelperPanel.add(Box.createGlue());
        this.serviceSelectionComponent.add(buttonHelperPanel);
        this.serviceSelectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
        this.serviceSelectionComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Missing Entailment Explanation Service:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void computeExplanation(){
        this.logger.debug("Computation of explanation requested");
        this.loadingUI.resetLoadingUI();
        this.loadingUI.activeLoadingUI();
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
        this.resetMainComponent();
        this.resultHolderComponent.removeAll();
        this.resultHolderComponent.add(resultComponent);
        this.repaintComponents();
    }

    private void addMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
            try{
                OWLAxiom axiomToAdd = this.missingEntailmentTextEditor.createObject();
                this.selectedMissingEntailmentListModel.checkAndAddElement(axiomToAdd);
            }
            catch (OWLException e) {
                this.logger.debug("Exception caught when trying to add missing entailment: " + e);
            }
            finally {
                this.selectedmissingEntailmentList.clearSelection();
                this.missingEntailmentTextEditor.setText("");
                this.changeComputeButtonStatus();
            }
//        });
    }

    private void deleteMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
            List<OWLAxiom> toDelete = this.selectedmissingEntailmentList.getSelectedValuesList();
            this.selectedMissingEntailmentListModel.removeElements(toDelete);
            this.selectedmissingEntailmentList.clearSelection();
            this.missingEntailmentTextEditor.setText("");
            this.changeComputeButtonStatus();
//        });
    }

    private void resetMissingEntailment(){
//        SwingUtilities.invokeLater(() -> {
            this.selectedMissingEntailmentListModel.removeAll();
            this.selectedmissingEntailmentList.clearSelection();
            this.missingEntailmentTextEditor.setText("");
            this.changeComputeButtonStatus();
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
        this.selectedmissingEntailmentList.clearSelection();
        this.changeComputeButtonStatus();
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
                String missingEntailmentOntologyName = this.getOWLEditorKit().getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI() + "missingEntailmentOntology";
                OWLOntology missingEntailmentOntology = ontologyManager.createOntology(IRI.create(missingEntailmentOntologyName));
                ontologyManager.addAxioms(missingEntailmentOntology, new HashSet<>(this.selectedMissingEntailmentListModel.getOwlObjects()));
                ontologyManager.saveOntology(missingEntailmentOntology, new RDFXMLDocumentFormat(), new FileOutputStream(file));
            } catch (OWLOntologyCreationException | OWLOntologyStorageException | FileNotFoundException exception) {
                this.logger.error("Error when saving missing entailment ontology to file: ", exception);
                UIUtilities.showError(exception.getMessage(), this.getOWLEditorKit());
            }
        }
        this.selectedmissingEntailmentList.clearSelection();
    }

    private String reverseParseOWLObject(OWLObject owlObject){
        if (owlObject instanceof OWLClassAssertionAxiom){
            OWLClassAssertionAxiom assertion = ((OWLClassAssertionAxiom) owlObject);
            return assertion.getIndividual() + " Type: " + assertion.getClassExpression();
        }
        else{
            return owlObject.toString();
        }
    }

    protected void changeComputeButtonStatus(){
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

    @Override
    public void handleUIEvent(ExplanationLoadingUIEvent event) {
        if (event.getType().equals(
                ExplanationLoadingUIEventType
                        .EXPLANATION_GENERATION_CANCELLED)){
            INonEntailmentExplanationService<?> service =
                    this.nonEntailmentExplainerManager.getCurrentExplainer();
            this.logger.debug("Cancelling non entailment explanation generation of service {}", service);
            service.cancel();
        }
    }

    private class ViewComponentOntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                    changeEvent.isType(EventType.ONTOLOGY_RELOADED)) {
                logger.debug("Change or reload of active ontology detected");
                selectedMissingEntailmentListModel.removeAll();
            }
            change();
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            logger.debug("Change to ontology detected");
            change();
        }

        private void change(){
            INonEntailmentExplanationService<?> explainer = nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(getOWLModelManager().getActiveOntology());
//            resetExplanationServiceComponent();
//            resetHorizontalSplitPane();
//            resetHolderPanel();
//            addHolderPanel();
//            repaintComponents();
            changeComputeButtonStatus();
        }
    }

    private static class OWLLogicalAxiomChecker implements OWLExpressionChecker<OWLAxiom>{

        private final OWLModelManager manager;

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
                throw ParserUtil.convertException(e);
            }
        }
    }

}
