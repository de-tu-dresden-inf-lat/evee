package de.tu_dresden.inf.lat.evee.protege.abduction;

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
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

public class AbductionViewComponent extends AbstractOWLViewComponent implements ActionListener {

    private final AbductionGeneratorManager abductionGeneratorManager;
    private final AbductionViewComponentOWLModelChangeListener owlModelChangeListener;
    private OWLAbductionSolver abductionSolver;
    private AbductionLoadingUI loadingUI;
    private Set<OWLAxiom> lastObservation;
    private List<OWLEntity> lastAbducibles;
    private AbductionSignatureSelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<OWLAxiom> observationTextEditor;
    private OWLObjectListModel<OWLAxiom> selectedObservationListModel;
    private JList<OWLAxiom> selectedObservationList;
    private List<Set<OWLAxiom>> hypotheses;
    private boolean parametersChanged;
    private JButton computeButton;
    private JPanel abductionHolderPanel;
    private int hypothesisIndex;
    private JPanel holderPanel;
    private JPanel signatureAndObservationPanel;
    private JPanel signatureManagementPanel;
    private JPanel observationManagementPanel;
    private JPanel settingsAndResultPanel;
    private JPanel settingsHolderPanel;
    private JSplitPane outerSplitPane;
    private JPanel resultScrollingPanel;
    private JScrollPane abductionScrollPane;
    private JButton addObservationButton;
    private JButton deleteObservationButton;
    private JButton resetObservationButton;
    private JSpinner abductionNumberSpinner;
    private final String COMPUTE_COMMAND = "COMPUTE_ABDUCTION";
    private final String COMPUTE_NAME = "Compute";
    private final String COMPUTE_TOOLTIP = "Compute hypotheses using Selected Signature and Observation";
    private final String ADD_OBSERVATION_COMMAND = "ADD_OBSERVATION";
    private final String ADD_OBSERVATION_NAME = "Add";
    private final String ADD_OBSERVATION_TOOLTIP = "Add axioms to observation";
    private final String DELETE_OBSERVATION_COMMAND = "DELETE_OBSERVATION";
    private final String DELETE_OBSERVATION_NAME = "Delete";
    private final String DELETE_OBSERVATION_TOOLTIP = "Delete selected axioms from observation";
    private final String RESET_OBSERVATION_COMMAND = "RESET_OBSERVATION";
    private final String RESET_OBSERVATION_NAME = "Reset";
    private final String RESET_OBSERVATION_TOOLTIP = "Delete all axioms from observation";
    private final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    private final String ADD_TO_ONTO_NAME = "Add to Ontology";
    private final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the current active ontology";
    private final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    private final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";

    private final Logger logger = LoggerFactory.getLogger(AbductionViewComponent.class);

    public AbductionViewComponent(){
        this.abductionGeneratorManager = new AbductionGeneratorManager();
        this.hypotheses = new ArrayList<>();
        this.lastObservation = new HashSet<>();
        this.lastAbducibles = new ArrayList<>();
        this.parametersChanged = true;
        this.logger.debug("Object AbductionViewComponent created");
        this.owlModelChangeListener = new AbductionViewComponentOWLModelChangeListener();
    }

    protected ArrayList<OWLObject> getObservations(){
        return new ArrayList<>(this.selectedObservationListModel.getOwlObjects());
    }

    @Override
    protected void initialiseOWLView() {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new AbductionSignatureSelectionUI(
                this, this.getOWLEditorKit(),
                this.getOWLModelManager());
        this.setLayout(new BorderLayout(10, 10));
        this.createSignatureManagementComponent();
        this.createObservationComponent();
        this.createSettingsComponent();
        this.createAbductionComponent();
        this.resetView();
        this.getOWLEditorKit().getOWLModelManager().addListener(this.owlModelChangeListener);
        this.logger.debug("initialisation completed");
    }

    private void resetView(){
        this.signatureAndObservationPanel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Signature", this.signatureManagementPanel);
        tabbedPane.addTab("Observation", this.observationManagementPanel);
        this.signatureAndObservationPanel.add(tabbedPane, BorderLayout.CENTER);
        JSplitPane innerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                this.settingsHolderPanel, this.abductionHolderPanel);
        innerSplitPane.setDividerLocation(0.3);
        this.settingsAndResultPanel = new JPanel();
        this.settingsAndResultPanel.setLayout(new BoxLayout(this.settingsAndResultPanel, BoxLayout.PAGE_AXIS));
        this.settingsAndResultPanel.add(innerSplitPane);
        this.outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                this.signatureAndObservationPanel,
                this.settingsAndResultPanel);
        this.outerSplitPane.setDividerLocation(0.3);
        this.removeAll();
        this.holderPanel = new JPanel();
        this.holderPanel.setLayout(new BoxLayout(this.holderPanel, BoxLayout.PAGE_AXIS));
        this.holderPanel.add(this.outerSplitPane);
        this.add(holderPanel, BorderLayout.CENTER);
        this.repaint();
        this.revalidate();
    }

    @Override
    protected void disposeOWLView() {
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
        this.getOWLEditorKit().getOWLModelManager().removeListener(this.owlModelChangeListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case COMPUTE_COMMAND:
                this.computeAbductions();
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

//    private JPanel createSignatureAndObservationComponent(){
//
//        return tabbedPaneHolderPanel;
//    }

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

    private JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setActionCommand(actionCommand);
        newButton.setToolTipText(toolTip);
        newButton.addActionListener(this);
        return newButton;
    }

//    private JPanel createSettingsAndAbductionComponent(){
//        JPanel upperPanel = this.createSettingsComponent();
//        JPanel lowerPanel = this.createAbductionComponent();
//        JSplitPane observationAndAbductionPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                upperPanel, lowerPanel);
//        observationAndAbductionPane.setDividerLocation(0.3);
//        JPanel holderPanel = new JPanel();
//        holderPanel.setLayout(new BoxLayout(holderPanel, BoxLayout.PAGE_AXIS));
//        holderPanel.add(observationAndAbductionPane);
//        return holderPanel;
//    }

    private void createSettingsComponent(){
        this.settingsHolderPanel = new JPanel();
        this.settingsHolderPanel.setLayout(new BoxLayout(settingsHolderPanel, BoxLayout.PAGE_AXIS));
        Vector<String> abductionNames = this.abductionGeneratorManager.getAbductionGeneratorNames();
        JComboBox<String> abductionNamesComboBox = new JComboBox<>(abductionNames);
        abductionNamesComboBox.setSelectedItem(this.abductionGeneratorManager.getCurrentAbductionGeneratorName());
//        abductionNamesComboBox.setSelectedIndex(0);
        abductionNamesComboBox.addActionListener(this.abductionGeneratorManager);
        this.settingsHolderPanel.add(abductionNamesComboBox);
        this.settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel spinnerHelperPanel = new JPanel();
        spinnerHelperPanel.setLayout(new BoxLayout(spinnerHelperPanel, BoxLayout.LINE_AXIS));
        JLabel label = this.createLabel(this.SETTINGS_LABEL);
        spinnerHelperPanel.add(label);
        spinnerHelperPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, null, 1);
        this.abductionNumberSpinner = new JSpinner(spinnerModel);
        this.abductionNumberSpinner.setToolTipText(this.SETTINGS_SPINNER_TOOLTIP);
        this.abductionNumberSpinner.setMaximumSize(new Dimension(500, this.abductionNumberSpinner.getPreferredSize().height));
        spinnerHelperPanel.add(this.abductionNumberSpinner);
        spinnerHelperPanel.add(Box.createGlue());
        this.settingsHolderPanel.add(spinnerHelperPanel);
        this.settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = this.createButton(this.COMPUTE_COMMAND, this.COMPUTE_NAME, this.COMPUTE_TOOLTIP);
        JPanel buttonHelperPanel = new JPanel();
        buttonHelperPanel.setLayout(new BoxLayout(buttonHelperPanel, BoxLayout.LINE_AXIS));
        buttonHelperPanel.add(this.computeButton);
        buttonHelperPanel.add(Box.createGlue());
        this.settingsHolderPanel.add(buttonHelperPanel);
        this.settingsHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), "Settings:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
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
        JPanel observationEditorPanel = new JPanel(new BorderLayout());
        OWLExpressionChecker<OWLAxiom> logicalAxiomChecker =
                new OWLLogicalAxiomChecker(this.getOWLModelManager());
        this.observationTextEditor = new ExpressionEditor<>(this.getOWLEditorKit(), logicalAxiomChecker);
        JScrollPane editorScrollPane = ComponentFactory.createScrollPane(this.observationTextEditor);
        editorScrollPane.setPreferredSize(new Dimension(400, 400));
        observationEditorPanel.add(editorScrollPane, BorderLayout.CENTER);
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
        this.addObservationButton = this.createButton(this.ADD_OBSERVATION_COMMAND,
                this.ADD_OBSERVATION_NAME, this.ADD_OBSERVATION_TOOLTIP);
        toolbar.add(this.addObservationButton);
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        this.deleteObservationButton = this.createButton(this.DELETE_OBSERVATION_COMMAND,
                this.DELETE_OBSERVATION_NAME, this.DELETE_OBSERVATION_TOOLTIP);
        toolbar.add(this.deleteObservationButton);
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        this.resetObservationButton = this.createButton(this.RESET_OBSERVATION_COMMAND,
                this.RESET_OBSERVATION_NAME, this.RESET_OBSERVATION_TOOLTIP);
        toolbar.add(this.resetObservationButton);
        buttonHolderPanel.add(toolbar);
        buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonHolderPanel;
    }

    private JPanel createSelectedObservationPanel(){
        JPanel observationPanel = new JPanel(new BorderLayout());
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
        observationPanel.add(scrollPane, BorderLayout.CENTER);
        observationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Selected observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationPanel;
    }

    private void createAbductionComponent(){
        this.abductionHolderPanel = new JPanel(new BorderLayout());
        this.abductionScrollPane = new JScrollPane();
        this.resultScrollingPanel = new JPanel();
        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
        this.abductionScrollPane.setViewportView(this.resultScrollingPanel);
        this.abductionHolderPanel.add(this.abductionScrollPane);
        this.abductionHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Hypotheses:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void computeAbductions(){
        this.logger.debug("Hypotheses requested, checking if new results need to be calculated");
        HashSet<OWLAxiom> currentObservation = new HashSet<>(this.selectedObservationListModel.getOwlObjects());
        ArrayList<OWLEntity> currentAbducibles = new ArrayList<>(this.signatureSelectionUI.getSelectedSignature());
//        todo: check for change of abductionSolver!
        if ((! this.lastObservation.equals(currentObservation)) || (! this.lastAbducibles.equals(currentAbducibles))){
            this.lastObservation = currentObservation;
            this.lastAbducibles = currentAbducibles;
            this.parametersChanged = true;
        }
        if (this.parametersChanged){
            this.parametersChanged = false;
            this.logger.debug("Change in observation or abducibles detected, computing NEW hypotheses");
            this.abductionSolver = this.abductionGeneratorManager.getCurrentAbductionGenerator();
            this.abductionSolver.setOntology(this.getOWLModelManager().getActiveOntology());
            this.abductionSolver.setAbducibles(this.signatureSelectionUI.getSelectedSignature());
            this.abductionSolver.setObservation(this.lastObservation);
            AbductionSolverThread abductionGenerationThread = new AbductionSolverThread(this, this.abductionSolver);
            this.hypothesisIndex = 1;
            this.createAbductionComponent();
            this.resetView();
            this.loadingUI = new AbductionLoadingUI(this.abductionGeneratorManager.getCurrentAbductionGeneratorName(),
                    this.getOWLEditorKit());
            this.loadingUI.showLoadingScreen();
            abductionGenerationThread.start();
        }
        else {
            this.logger.debug("No change in observation or abducibles detected, retrieving more previously computed results.");
            this.showResults();
        }
    }


    public void showResults(){
        int resultNumber = (int) this.abductionNumberSpinner.getValue();
        this.logger.debug("Showing {} results of abduction generation process", resultNumber);
        this.hypotheses.clear();
        this.logger.debug("hypotheses cleared");
        Stream<Set<OWLAxiom>> resultStream = this.abductionSolver.generateHypotheses();
        resultStream.limit(resultNumber).forEach(result -> {
            if (result != null){
                this.hypotheses.add(result);}
        });
        for (Set<OWLAxiom> result : this.hypotheses){
            JPanel singleResultPanel = new JPanel();
            singleResultPanel.setLayout(new BorderLayout());
            singleResultPanel.setBorder(new CompoundBorder(
                    new EmptyBorder(5, 5, 5, 5),
                    new CompoundBorder(new LineBorder(Color.BLACK, 1),
                            new EmptyBorder(5, 5, 5, 5))));
            JPanel labelAndButtonPanel = new JPanel();
            labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
            JLabel label = new JLabel("Hypothesis " + this.hypothesisIndex);
            this.hypothesisIndex++;
            labelAndButtonPanel.add(label);
            labelAndButtonPanel.add(Box.createHorizontalGlue());
            JButton addToOntologyButton = new JButton(this.ADD_TO_ONTO_NAME);
            addToOntologyButton.setToolTipText(this.ADD_TO_ONTO_TOOLTIP);
            addToOntologyButton.setActionCommand(this.ADD_TO_ONTO_COMMAND);
            addToOntologyButton.addActionListener(new AddToOntologyButtonListener(
                    this.getOWLModelManager().getActiveOntology(), result));
            labelAndButtonPanel.add(addToOntologyButton);
            singleResultPanel.add(labelAndButtonPanel, BorderLayout.PAGE_START);
            OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
            resultListModel.addElements(result);
            JList<OWLAxiom> resultList = new JList<>(resultListModel);
            OWLCellRenderer renderer = new OWLCellRenderer(this.getOWLEditorKit());
            renderer.setHighlightUnsatisfiableClasses(false);
            renderer.setHighlightUnsatisfiableProperties(false);
            renderer.setHighlightKeywords(true);
            renderer.setFeint(true);
            resultList.setCellRenderer(renderer);
            JScrollPane singleResultScrollPane = new JScrollPane(resultList);
            singleResultScrollPane.setPreferredSize(resultList.getPreferredSize());
            singleResultPanel.add(singleResultScrollPane, BorderLayout.CENTER);

            this.resultScrollingPanel.add(singleResultPanel);
        }
        this.resultScrollingPanel.repaint();
        this.resultScrollingPanel.revalidate();
    }

    public void showError(String message){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(this.getOWLEditorKit().getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.getOWLEditorKit().getWorkspace())));
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
            }
        });
    }

    private void deleteObservation(){
        SwingUtilities.invokeLater(() -> {
            java.util.List<OWLAxiom> toDelete = this.selectedObservationList.getSelectedValuesList();
            this.selectedObservationListModel.removeElements(toDelete);
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
        });
    }

    private void resetObservation(){
        SwingUtilities.invokeLater(() -> {
            this.selectedObservationListModel.removeAll();
            this.selectedObservationList.clearSelection();
            this.observationTextEditor.setText("");
        });
    }

    public void abductionGenerationCompleted(){
        this.loadingUI.disposeLoadingScreen();
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


    private class AbductionViewComponentOWLModelChangeListener implements OWLModelManagerListener {

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || changeEvent.isType(EventType.ONTOLOGY_RELOADED)) {
                    selectedObservationListModel.removeAll();
                    abductionSolver.setOntology(getOWLModelManager().getActiveOntology());
                    createAbductionComponent();
                    resetView();
                }
            });
        }
    }

    private class AddToOntologyButtonListener implements ActionListener{

        boolean alreadyAdded = false;
        private final OWLOntology ontology;
        private final Set<OWLAxiom> newAxioms;
        private final Logger logger = LoggerFactory.getLogger(AddToOntologyButtonListener.class);

        private AddToOntologyButtonListener(OWLOntology ontology, Set<OWLAxiom> newAxioms){
            this.ontology = ontology;
            this.newAxioms = newAxioms;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.logger.debug("Button AddToOntology clicked");
            if (e.getActionCommand().equals(ADD_TO_ONTO_COMMAND) && !alreadyAdded){
                this.logger.debug("Adding new Axioms to Ontology:");
                this.newAxioms.forEach(axiom -> this.logger.debug(axiom.toString()));
                this.ontology.getOWLOntologyManager().addAxioms(this.ontology, newAxioms);
                alreadyAdded = true;
            }
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
