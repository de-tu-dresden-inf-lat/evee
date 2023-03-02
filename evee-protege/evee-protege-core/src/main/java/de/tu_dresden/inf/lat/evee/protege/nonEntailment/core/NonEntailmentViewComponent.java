package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPlugin;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPluginLoader;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
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

public class NonEntailmentViewComponent extends AbstractOWLViewComponent implements ActionListener, IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> {

    private final NonEntailmentExplainerManager nonEntailmentExplainerManager;
    private final ViewComponentOntologyChangeListener changeListener;

    private NonEntailmentVocabularySelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> observationTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedObservationListModel;
    private JList<OWLAxiom> selectedObservationList;
    private JButton computeButton;
    private JPanel resultHolderComponent;
    private JPanel holderPanel;
    private JPanel serviceSelectionComponent;
    private JPanel signatureAndObservationComponent;
    private JPanel signatureManagementPanel;
    private JPanel observationManagementPanel;
    private JPanel nonEntailmentExplanationServiceComponent;
    private JSplitPane leftRightSplitPane;
//    private JPanel splitPaneHolderComponent;
    private JComboBox<String> serviceNamesComboBox;
    private JLabel computeMessageLabel;
    private static final String COMPUTE_COMMAND = "COMPUTE_NON_ENTAILMENT";
    private static final String COMPUTE_NAME = "Compute";
    private static final String COMPUTE_TOOLTIP = "Compute non-entailment explanation using Selected Signature and Observation";
    private static final String ADD_OBSERVATION_COMMAND = "ADD_OBSERVATION";
    private static final String ADD_OBSERVATION_NAME = "Add";
    private static final String ADD_OBSERVATION_TOOLTIP = "Add axioms to observation";
    private static final String DELETE_OBSERVATION_COMMAND = "DELETE_OBSERVATION";
    private static final String DELETE_OBSERVATION_NAME = "Delete";
    private static final String DELETE_OBSERVATION_TOOLTIP = "Delete selected axioms from observation";
    private static final String RESET_OBSERVATION_COMMAND = "RESET_OBSERVATION";
    private static final String RESET_OBSERVATION_NAME = "Reset";
    private static final String RESET_OBSERVATION_TOOLTIP = "Delete all axioms from observation";
    private static final String LOAD_OBSERVATION_COMMAND = "LOAD_OBSERVATION";
    private static final String LOAD_OBSERVATION_BUTTON_NAME = "Load from file";
    private static final String LOAD_OBSERVATION_TOOLTIP = "Load an observation from a file";
    private static final String SAVE_OBSERVATION_COMMAND = "SAVE_OBSERVATION";
    private static final String SAVE_OBSERVATION_BUTTON_NAME = "Save to file";
    private static final String SAVE_OBSERVATION_TOOLTIP = "Save an observation to a file";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentViewComponent.class);

    public NonEntailmentViewComponent(){
        this.nonEntailmentExplainerManager = new NonEntailmentExplainerManager();
        this.changeListener = new ViewComponentOntologyChangeListener();
        this.logger.debug("Object NonEntailmentViewComponent created");
    }

    protected ArrayList<OWLObject> getObservations(){
        return new ArrayList<>(this.selectedObservationListModel.getOwlObjects());
    }

    @Override
    protected void initialiseOWLView() {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new NonEntailmentVocabularySelectionUI(
                this, this.getOWLEditorKit());
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
                this.logger.error("Error while loading non-entailment explanation plugin:\n" + e);
            }
        }
//        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.createGeneralSettingsComponent();
            this.nonEntailmentExplainerManager.setExplanationService((String) this.serviceNamesComboBox.getSelectedItem());
            this.createSignatureManagementComponent();
            this.createObservationManagementComponent();
            this.resetMainComponent();
//        });
        this.getOWLEditorKit().getOWLModelManager().addListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.logger.debug("initialisation completed");
    }

    private void resetMainComponent(){
        this.logger.debug("Resetting viewComponent");
        this.holderPanel = new JPanel(new GridBagLayout());
        this.signatureAndObservationComponent = new JPanel();
        this.signatureAndObservationComponent.setLayout(new BoxLayout(this.signatureAndObservationComponent, BoxLayout.PAGE_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Signature", this.signatureManagementPanel);
        tabbedPane.addTab("Observation", this.observationManagementPanel);
        this.signatureAndObservationComponent.add(tabbedPane);
        this.nonEntailmentExplanationServiceComponent = new JPanel();
        this.nonEntailmentExplanationServiceComponent.setLayout(new BoxLayout(this.nonEntailmentExplanationServiceComponent, BoxLayout.PAGE_AXIS));
        this.resultHolderComponent = new JPanel();
        this.resultHolderComponent.setLayout(new BoxLayout(this.resultHolderComponent, BoxLayout.PAGE_AXIS));
        INonEntailmentExplanationService<?> explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        if (explainer != null){
            if (explainer.getSettingsComponent() != null){
                JSplitPane serviceSettingsAndResultSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        explainer.getSettingsComponent(), this.resultHolderComponent);
                serviceSettingsAndResultSplitPane.setDividerLocation(0.2);
                this.nonEntailmentExplanationServiceComponent.add(serviceSettingsAndResultSplitPane);
            }
            else {
                this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
            }
        }
        else {
            this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
        }
        this.leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                this.signatureAndObservationComponent,
                this.nonEntailmentExplanationServiceComponent);
        this.leftRightSplitPane.setDividerLocation(0.3);
//        this.splitPaneHolderComponent = new JPanel();
//        this.splitPaneHolderComponent.setLayout(new BoxLayout(this.splitPaneHolderComponent, BoxLayout.PAGE_AXIS));
//        this.splitPaneHolderComponent.add(this.leftRightSplitPane);
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
        this.holderPanel.add(this.leftRightSplitPane, constraints);
        this.removeAll();
        this.add(holderPanel);
//        this.repaint();
//        this.revalidate();
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
//    private void resetSignatureAndObservationComponent(){
////        improve: temporary save indices of selected tabs and reselect these tabs on the new panel
//        SwingUtilities.invokeLater(() -> {
//            this.createSignatureManagementComponent();
//            this.createObservationManagementComponent();
//            this.signatureAndObservationComponent = new JPanel();
//            this.signatureAndObservationComponent.setLayout(new BoxLayout(this.signatureAndObservationComponent, BoxLayout.PAGE_AXIS));
//            JTabbedPane tabbedPane = new JTabbedPane();
//            tabbedPane.addTab("Signature", this.signatureManagementPanel);
//            tabbedPane.addTab("Observation", this.observationManagementPanel);
//            this.signatureAndObservationComponent.add(tabbedPane);
//        });
//    }
//
//    private void resetExplanationServiceComponent(){
//        SwingUtilities.invokeLater(() -> {
//            this.nonEntailmentExplanationServiceComponent = new JPanel();
//            this.nonEntailmentExplanationServiceComponent.setLayout(new BoxLayout(this.nonEntailmentExplanationServiceComponent, BoxLayout.PAGE_AXIS));
//            this.resultHolderComponent = new JPanel();
//            this.resultHolderComponent.setLayout(new BoxLayout(this.resultHolderComponent, BoxLayout.PAGE_AXIS));
//            INonEntailmentExplanationService<?> explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
//            if (explainer != null){
//                if (explainer.getSettingsComponent() != null){
//                    JSplitPane serviceSettingsAndResultSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                            explainer.getSettingsComponent(), this.resultHolderComponent);
//                    serviceSettingsAndResultSplitPane.setDividerLocation(0.3);
//                    this.nonEntailmentExplanationServiceComponent.add(serviceSettingsAndResultSplitPane);
//                }
//                else {
//                    this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
//                }
//            }
//            else {
//                this.nonEntailmentExplanationServiceComponent.add(this.resultHolderComponent);
//            }
//        });
//    }
//
//    private void resetSignatureObservationAndResultSplitPane(){
//        SwingUtilities.invokeLater(() -> {
//            this.leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//                    this.signatureAndObservationComponent,
//                    this.nonEntailmentExplanationServiceComponent);
//            this.leftRightSplitPane.setDividerLocation(0.3);
//            this.splitPaneHolderComponent = new JPanel();
//            this.splitPaneHolderComponent.setLayout(new BoxLayout(this.splitPaneHolderComponent, BoxLayout.PAGE_AXIS));
//            this.splitPaneHolderComponent.add(this.leftRightSplitPane);
//        });
//    }

    private void repaintComponents(){
        SwingUtilities.invokeLater(() -> {
            this.repaint();
            this.revalidate();
        });
    }



    @Override
    protected void disposeOWLView() {
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
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
            this.changeComputeButtonStatus();
        }
        else{
            switch (e.getActionCommand()){
                case COMPUTE_COMMAND:
                    this.computeExplanation();
                    break;
                case ADD_OBSERVATION_COMMAND:
                    this.addObservation();
                    break;
                case DELETE_OBSERVATION_COMMAND:
                    this.deleteObservation();
                    break;
                case RESET_OBSERVATION_COMMAND:
                    this.resetObservation();
                    break;
                case LOAD_OBSERVATION_COMMAND:
                    this.loadObservation();
                    break;
                case SAVE_OBSERVATION_COMMAND:
                    this.saveObservation();
                    break;
            }
        }
    }

    @Override
    public void handleEvent(ExplanationEvent<INonEntailmentExplanationService<?>> event){
        switch (event.getType()){
            case COMPUTATION_COMPLETE :
                this.showResult(event.getSource().getResult());
                break;
            case ERROR :
                this.showError(event.getSource().getErrorMessage(), this.getOWLEditorKit());
                break;
        }
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

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setActionCommand(actionCommand);
        newButton.setToolTipText(toolTip);
        newButton.addActionListener(this);
        return newButton;
    }

    private void createObservationManagementComponent(){
        this.observationManagementPanel = new JPanel();
        this.observationManagementPanel.setLayout(new GridBagLayout());
        JPanel observationTextPanel = this.createObservationTextPanel();
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
        this.observationManagementPanel.add(observationTextPanel, constraints);
        JPanel buttonPanel = this.createObservationButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        this.observationManagementPanel.add(buttonPanel, constraints);
        JPanel selectedObservationPanel = this.createSelectedObservationPanel();
//        specific for selected observations:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.observationManagementPanel.add(selectedObservationPanel, constraints);
    }

    private JPanel createObservationTextPanel(){
        JPanel observationEditorPanel = new JPanel();
        observationEditorPanel.setLayout(new BoxLayout(observationEditorPanel, BoxLayout.PAGE_AXIS));
        OWLExpressionChecker<OWLAxiom> logicalAxiomChecker =
                new OWLLogicalAxiomChecker(this.getOWLModelManager());
        this.observationTextEditor = new ExpressionEditor<>(this.getOWLEditorKit(), logicalAxiomChecker);
        JScrollPane editorScrollPane = ComponentFactory.createScrollPane(this.observationTextEditor);
        editorScrollPane.setPreferredSize(new Dimension(400, 400));
        observationEditorPanel.add(editorScrollPane);
        observationEditorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Enter observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationEditorPanel;
    }

    private JPanel createObservationButtonPanel(){
        JPanel buttonHolderPanel = new JPanel();
        buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.PAGE_AXIS));
        JToolBar firstRowToolbar = new JToolBar();
        firstRowToolbar.setOrientation(JToolBar.HORIZONTAL);
        firstRowToolbar.setFloatable(false);
        firstRowToolbar.setLayout(new BoxLayout(firstRowToolbar, BoxLayout.LINE_AXIS));
        JButton addObservationButton = this.createButton(ADD_OBSERVATION_COMMAND,
                ADD_OBSERVATION_NAME, ADD_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(addObservationButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton deleteObservationButton = this.createButton(DELETE_OBSERVATION_COMMAND,
                DELETE_OBSERVATION_NAME, DELETE_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(deleteObservationButton);
        firstRowToolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton resetObservationButton = this.createButton(RESET_OBSERVATION_COMMAND,
                RESET_OBSERVATION_NAME, RESET_OBSERVATION_TOOLTIP);
        firstRowToolbar.add(resetObservationButton);
        buttonHolderPanel.add(firstRowToolbar);
        buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JToolBar secondRowToolBar = new JToolBar();
        secondRowToolBar.setOrientation(JToolBar.HORIZONTAL);
        secondRowToolBar.setFloatable(false);
        secondRowToolBar.setLayout(new BoxLayout(secondRowToolBar, BoxLayout.LINE_AXIS));
        JButton loadObservationButton = this.createButton(LOAD_OBSERVATION_COMMAND, LOAD_OBSERVATION_BUTTON_NAME, LOAD_OBSERVATION_TOOLTIP);
        secondRowToolBar.add(loadObservationButton);
        secondRowToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
        JButton saveObservationButton = this.createButton(SAVE_OBSERVATION_COMMAND, SAVE_OBSERVATION_BUTTON_NAME, SAVE_OBSERVATION_TOOLTIP);
        secondRowToolBar.add(saveObservationButton);
        buttonHolderPanel.add(secondRowToolBar);
        buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonHolderPanel;
    }

    private JPanel createSelectedObservationPanel(){
        JPanel observationPanel = new JPanel();
        observationPanel.setLayout(new BoxLayout(observationPanel, BoxLayout.PAGE_AXIS));
        this.selectedObservationListModel = new OWLObjectListModel<>();
        this.selectedObservationList = new JList<>(this.selectedObservationListModel);
        this.selectedObservationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    JList list = (JList) e.getSource();
                    Object selectedValue = list.getSelectedValue();
                    if (selectedValue instanceof OWLObject){
                        observationTextEditor.setText(reverseParseOWLObject((OWLObject) selectedValue));
                    }
                }
            }
        });
        OWLCellRenderer renderer = new OWLCellRenderer(this.getOWLEditorKit());
        renderer.setHighlightKeywords(true);
        renderer.setHighlightUnsatisfiableClasses(false);
        renderer.setHighlightUnsatisfiableProperties(false);
        this.selectedObservationList.setCellRenderer(renderer);
        JScrollPane scrollPane = new JScrollPane(this.selectedObservationList);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        observationPanel.add(scrollPane);
        observationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Selected observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationPanel;
    }

    private void createGeneralSettingsComponent(){
        this.serviceSelectionComponent = new JPanel();
        this.serviceSelectionComponent.setLayout(new BoxLayout(this.serviceSelectionComponent, BoxLayout.PAGE_AXIS));
        Vector<String> serviceNames = this.nonEntailmentExplainerManager.getExplanationServiceNames();
        this.serviceNamesComboBox = new JComboBox<>(serviceNames);
        this.serviceNamesComboBox.addActionListener(this);
        this.serviceSelectionComponent.add(this.serviceNamesComboBox);
        this.serviceSelectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = this.createButton(COMPUTE_COMMAND, COMPUTE_NAME, COMPUTE_TOOLTIP);
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
                        "Non-Entailment Explanation Service:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void computeExplanation(){
        this.logger.debug("Computation of explanation requested");
//        SwingUtilities.invokeLater(() -> {
            INonEntailmentExplanationService<?> explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(this.getOWLModelManager().getActiveOntology());
            explainer.setSignature(this.signatureSelectionUI.getPermittedVocabulary());
            explainer.setObservation(new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
            this.resetMainComponent();
            explainer.computeExplanation();
//        });
    }

    private void showResult(Component resultComponent){
        SwingUtilities.invokeLater(() -> {
            this.resultHolderComponent.removeAll();
            this.resultHolderComponent.add(resultComponent);
            this.repaint();
            this.revalidate();
        });
    }

    public void showError(String message, OWLEditorKit owlEditorKit){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            errorDialog.setVisible(true);
        });
    }

    private void addObservation(){
        SwingUtilities.invokeLater(() -> {
            try{
                OWLAxiom axiomToAdd = this.observationTextEditor.createObject();
                this.selectedObservationListModel.checkAndAddElement(axiomToAdd);
            }
            catch (OWLException e) {
                this.logger.debug("Exception caught when trying to add observation: " + e);
            }
            finally {
                this.selectedObservationList.clearSelection();
                this.observationTextEditor.setText("");
                this.changeComputeButtonStatus();
            }
        });
    }

    private void deleteObservation(){
        SwingUtilities.invokeLater(() -> {
            List<OWLAxiom> toDelete = this.selectedObservationList.getSelectedValuesList();
            this.selectedObservationListModel.removeElements(toDelete);
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
            this.changeComputeButtonStatus();
        });
    }

    private void resetObservation(){
        SwingUtilities.invokeLater(() -> {
            this.selectedObservationListModel.removeAll();
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
            this.changeComputeButtonStatus();
        });
    }

    private JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }

    private void loadObservation() {
        this.logger.debug("Loading observation from file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(this);
        Set<OWLLogicalAxiom> observationAxioms = new HashSet<>();
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try {
                OWLOntology observationOntology = manager.loadOntologyFromOntologyDocument(file);
                OWLOntology activeOntology = this.getOWLEditorKit().getOWLModelManager().getActiveOntology();
                Set<OWLLogicalAxiom> loadedAxioms = observationOntology.getLogicalAxioms();
                observationAxioms = loadedAxioms.stream().filter(
                                axiom -> activeOntology.getSignature().containsAll(
                                        axiom.getSignature())).collect(Collectors.toSet());
            } catch (OWLOntologyCreationException e) {
                this.logger.error("Error when loading observation from file: " + e);
                UIUtilities.showError(e.getMessage(), this.getOWLEditorKit());
            }
        }
        this.selectedObservationListModel.removeAll();
        this.selectedObservationListModel.addElements(observationAxioms);
        this.selectedObservationList.clearSelection();
    }

    private void saveObservation(){
        this.logger.debug("Saving observation to file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            try {
                String observationOntologyName = this.getOWLEditorKit().getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI() + "observationOntology";
                OWLOntology observationOntology = ontologyManager.createOntology(IRI.create(observationOntologyName));
                ontologyManager.addAxioms(observationOntology, new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
                ontologyManager.saveOntology(observationOntology, new RDFXMLDocumentFormat(), new FileOutputStream(file));
            } catch (OWLOntologyCreationException | OWLOntologyStorageException | FileNotFoundException exception) {
                this.logger.error("Error when saving observation ontology to file: " + exception);
                UIUtilities.showError(exception.getMessage(), this.getOWLEditorKit());
            }
        }
        this.selectedObservationList.clearSelection();
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
        INonEntailmentExplanationService<?> currentExplainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        SwingUtilities.invokeLater(() -> {
            if (currentExplainer == null) {
                this.computeButton.setEnabled(false);
            } else {
                assertNotNull(this.getOWLModelManager().getActiveOntology());
                assertNotNull(this.signatureSelectionUI.getPermittedVocabulary());
                assertNotNull(this.selectedObservationListModel.getOwlObjects());
                currentExplainer.setOntology(this.getOWLModelManager().getActiveOntology());
                currentExplainer.setSignature(this.signatureSelectionUI.getPermittedVocabulary());
                currentExplainer.setObservation(new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
                boolean enabled = currentExplainer.supportsExplanation();
                if (enabled) {
                    this.computeMessageLabel.setText("");
                } else {
                    this.computeMessageLabel.setText(currentExplainer.getSupportsExplanationMessage());
                }
                this.computeButton.setEnabled(enabled);
//                this.resetView();
            }
        });
        this.repaintComponents();
    }

    private class ViewComponentOntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                        changeEvent.isType(EventType.ONTOLOGY_RELOADED)) {
                    selectedObservationListModel.removeAll();
                    change();
                }
            });
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            change();
        }

        private void change(){
            INonEntailmentExplanationService<?> explainer = nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(getOWLModelManager().getActiveOntology());
            resetMainComponent();
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
