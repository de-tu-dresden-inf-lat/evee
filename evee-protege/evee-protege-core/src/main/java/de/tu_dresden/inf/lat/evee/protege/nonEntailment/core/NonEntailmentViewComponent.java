package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationPlugin;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationPluginLoader;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationService;
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
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

public class NonEntailmentViewComponent extends AbstractOWLViewComponent implements ActionListener, NonEntailmentExplanationListener {

    private final NonEntailmentExplainerManager nonEntailmentExplainerManager;
    private final ViewComponentOntologyChangeListener changeListener;

    private NonEntailmentSignatureSelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> observationTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedObservationListModel;
    private JList<OWLAxiom> selectedObservationList;
    private JButton computeButton;
    private JPanel resultHolderPanel;
    private JPanel holderPanel;
    private JPanel serviceSelectionPanel;
    private JPanel signatureAndObservationPanel;
    private JPanel signatureManagementPanel;
    private JPanel observationManagementPanel;
    private JPanel nonEntailmentExplanationServicePanel;
    private JSplitPane outerSplitPane;
    private JButton addObservationButton;
    private JButton deleteObservationButton;
    private JButton resetObservationButton;
    private JPanel splitPaneHolderPanel;
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

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentViewComponent.class);

    public NonEntailmentViewComponent(){
        this.nonEntailmentExplainerManager = new NonEntailmentExplainerManager();
        this.logger.debug("Object NonEntailmentViewComponent created");
        this.changeListener = new ViewComponentOntologyChangeListener();
    }

    protected ArrayList<OWLObject> getObservations(){
        return new ArrayList<>(this.selectedObservationListModel.getOwlObjects());
    }

    @Override
    protected void initialiseOWLView() {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new NonEntailmentSignatureSelectionUI(
                this, this.getOWLEditorKit(),
                this.getOWLModelManager());
        NonEntailmentExplanationPluginLoader loader = new NonEntailmentExplanationPluginLoader(this.getOWLEditorKit());
        for (NonEntailmentExplanationPlugin plugin : loader.getPlugins()){
            try{
                NonEntailmentExplanationService service = plugin.newInstance();
                service.setup(this.getOWLEditorKit());
                service.initialise();
                service.registerListener(this);
                this.nonEntailmentExplainerManager.registerNonEntailmentExplanationService(service, plugin.getName());
            }
            catch (Exception e){
                this.logger.error("Error while loading non-entailment explanation plugin:\n" + e);
            }
        }
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.createGeneralSettingsComponent();
            this.createSignatureManagementComponent();
            this.createObservationComponent();
            this.resetView();
        });
        this.getOWLEditorKit().getOWLModelManager().addListener(this.changeListener);
        this.getOWLEditorKit().getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.logger.debug("initialisation completed");
    }

    private void resetView(){
        this.holderPanel = new JPanel(new GridBagLayout());
        this.signatureAndObservationPanel = new JPanel();
        this.signatureAndObservationPanel.setLayout(new BoxLayout(this.signatureAndObservationPanel, BoxLayout.PAGE_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Signature", this.signatureManagementPanel);
        tabbedPane.addTab("Observation", this.observationManagementPanel);
        this.signatureAndObservationPanel.add(tabbedPane);
        this.nonEntailmentExplanationServicePanel = new JPanel();
        this.nonEntailmentExplanationServicePanel.setLayout(new BoxLayout(this.nonEntailmentExplanationServicePanel, BoxLayout.PAGE_AXIS));
        this.resultHolderPanel = new JPanel();
        this.resultHolderPanel.setLayout(new BoxLayout(this.resultHolderPanel, BoxLayout.PAGE_AXIS));
        NonEntailmentExplanationService explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
        if (explainer != null){
            if (explainer.getSettingsComponent() != null){
                JSplitPane settingsAndResultSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        explainer.getSettingsComponent(), this.resultHolderPanel);
                settingsAndResultSplitPane.setDividerLocation(0.3);
                this.nonEntailmentExplanationServicePanel.add(settingsAndResultSplitPane);
            }
            else {
                this.nonEntailmentExplanationServicePanel.add(this.resultHolderPanel);
            }
        }
        else {
            this.nonEntailmentExplanationServicePanel.add(this.resultHolderPanel);
        }
        this.outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                this.signatureAndObservationPanel,
                this.nonEntailmentExplanationServicePanel);
        this.outerSplitPane.setDividerLocation(0.3);
        this.splitPaneHolderPanel = new JPanel();
        this.splitPaneHolderPanel.setLayout(new BoxLayout(this.splitPaneHolderPanel, BoxLayout.PAGE_AXIS));
        this.splitPaneHolderPanel.add(this.outerSplitPane);
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
        constraints.weighty = 0.0;
        this.holderPanel.add(this.serviceSelectionPanel, constraints);
//        lower panel constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.9;
        this.holderPanel.add(this.splitPaneHolderPanel, constraints);
        this.removeAll();
        this.add(holderPanel);
        this.repaint();
        this.revalidate();
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
        }
    }

    @Override
    public void handleEvent(NonEntailmentExplanationEvent event){
        if (event.getType() == NonEntailmentExplanationEventType.COMPUTATION_COMPLETE) {
            this.showResult(event.getSource().getResultComponent());
        }
    }


    private void createSignatureManagementComponent(){
        this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
        JPanel ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedPanel();
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
        constraints.weighty = 0.6;
        this.signatureManagementPanel.add(ontologySignaturePanel, constraints);
        JPanel signatureSelectionToolPanel = this.signatureSelectionUI.getSignatureSelectionButtons();
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        this.signatureManagementPanel.add(signatureSelectionToolPanel, constraints);
        JPanel listPanel = this.signatureSelectionUI.getSelectedSignatureListPanel();
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        this.signatureManagementPanel.add(listPanel, constraints);
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setActionCommand(actionCommand);
        newButton.setToolTipText(toolTip);
        newButton.addActionListener(this);
        return newButton;
    }

    private void createObservationComponent(){
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
        constraints.weighty = 0.6;
        this.observationManagementPanel.add(observationTextPanel, constraints);
        JPanel buttonPanel = this.createObservationButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
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
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
        this.addObservationButton = this.createButton(ADD_OBSERVATION_COMMAND,
                ADD_OBSERVATION_NAME, ADD_OBSERVATION_TOOLTIP);
        toolbar.add(this.addObservationButton);
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        this.deleteObservationButton = this.createButton(DELETE_OBSERVATION_COMMAND,
                DELETE_OBSERVATION_NAME, DELETE_OBSERVATION_TOOLTIP);
        toolbar.add(this.deleteObservationButton);
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        this.resetObservationButton = this.createButton(RESET_OBSERVATION_COMMAND,
                RESET_OBSERVATION_NAME, RESET_OBSERVATION_TOOLTIP);
        toolbar.add(this.resetObservationButton);
        buttonHolderPanel.add(toolbar);
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
        this.serviceSelectionPanel = new JPanel();
        this.serviceSelectionPanel.setLayout(new BoxLayout(this.serviceSelectionPanel, BoxLayout.PAGE_AXIS));
        Vector<String> serviceNames = this.nonEntailmentExplainerManager.getExplanationServiceNames();
        JComboBox<String> serviceNamesComboBox = new JComboBox<>(serviceNames);
        serviceNamesComboBox.addActionListener(this.nonEntailmentExplainerManager);
        this.serviceSelectionPanel.add(serviceNamesComboBox);
        this.serviceSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = this.createButton(COMPUTE_COMMAND, COMPUTE_NAME, COMPUTE_TOOLTIP);
        this.computeButton.setEnabled(false);
        JPanel buttonHelperPanel = new JPanel();
        buttonHelperPanel.setLayout(new BoxLayout(buttonHelperPanel, BoxLayout.LINE_AXIS));
        buttonHelperPanel.add(this.computeButton);
        buttonHelperPanel.add(Box.createGlue());
        this.serviceSelectionPanel.add(buttonHelperPanel);
        this.serviceSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.serviceSelectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Non-Entailment Explanation Service:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void computeExplanation(){
        SwingUtilities.invokeLater(() -> {
            NonEntailmentExplanationService explainer = this.nonEntailmentExplainerManager.getCurrentExplainer();
            explainer.setOntology(this.getOWLModelManager().getActiveOntology());
            explainer.setSignature(this.signatureSelectionUI.getSelectedSignature());
            explainer.setObservation(new HashSet<>(this.selectedObservationListModel.getOwlObjects()));
            explainer.computeExplanation();
        });
    }

    private void showResult(Component resultComponent){
        SwingUtilities.invokeLater(() -> {
            this.resultHolderPanel.removeAll();
            this.resultHolderPanel.add(resultComponent);
            this.repaint();
            this.revalidate();
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
        boolean enabled = true;
        if (this.selectedObservationListModel.getSize() <= 0 ||
                this.signatureSelectionUI.listModelIsEmpty()){
            enabled = false;
        }
        if (this.nonEntailmentExplainerManager.getCurrentExplainer() == null){
            enabled = false;
        }
        else if (!(this.nonEntailmentExplainerManager.getCurrentExplainer().supportsMultiObservation()) &&
                this.selectedObservationListModel.getSize() != 1){
            enabled = false;
        }
        boolean finalEnabled = enabled;
        SwingUtilities.invokeLater(() -> this.computeButton.setEnabled(finalEnabled));
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
            nonEntailmentExplainerManager.getCurrentExplainer().setOntology(getOWLModelManager().getActiveOntology());
            resetView();
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
