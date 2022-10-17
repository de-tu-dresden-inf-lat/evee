package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
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
import java.util.*;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

public class AbductionViewComponent extends AbstractOWLViewComponent implements ActionListener {

    private final AbductionGeneratorManager abductionGeneratorManager;
    private AbductionLoadingUI loadingUI;
    private Set<OWLObject> currentObservations;
    private AbductionSignatureSelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private JTabbedPane observationTabbedPane;
    private ExpressionEditor<OWLClassAxiom> observationTBoxEditor;
    private ExpressionEditor<OWLClassAssertionAxiom> observationABoxEditor;
    private OWLObjectListModel<OWLObject> selectedObservationListModel;
    private JList<OWLObject> selectedObservationList;
    private JButton computeButton;
    private JPanel abductionPanel;
    private JScrollPane abductionScrollPane;
    private JButton addObservationButton;
    private JButton deleteObserationButton;
    private JButton resetObservationButton;
    private JSpinner abductionNumberSpinner;
    private final String COMPUTE_COMMAND = "COMPUTE_ABDUCTION";
    private final String COMPUTE_NAME = "Compute";
    private final String COMPUTE_TOOLTIP = "Compute abduction using OWLObjects selected in \"Excluded Dignature\" and Observation class expression";
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
    private final String SETTINGS_LABEL = "Number of abductions:";
    private final String SETTINGS_SPINNER_TOOLTIP = "Number of abductions to be generated in each computation step";

    private final Logger logger = LoggerFactory.getLogger(AbductionViewComponent.class);

    public AbductionViewComponent(){
        this.abductionGeneratorManager = new AbductionGeneratorManager();
        this.logger.debug("Object AbductionViewComponent created");
    }

    protected ArrayList<OWLObject> getObservations(){
        return new ArrayList<>(this.selectedObservationListModel.getOwlObjects());
    }

    @Override
    protected void initialiseOWLView() throws Exception {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new AbductionSignatureSelectionUI(
                this, this.getOWLEditorKit(),
                this.getOWLModelManager());
            this.setLayout(new BorderLayout(10, 10));
            JPanel signatureManagementPanel = this.createSignatureAndObservationComponent();
            JSplitPane ObservationAndResultPanel = this.createSettingsAndAbductionComponent();
            JSplitPane holderPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    signatureManagementPanel, ObservationAndResultPanel);
            holderPanel.setDividerLocation(0.3);
            this.add(holderPanel, BorderLayout.CENTER);
        this.logger.debug("initialisation completed");
    }

    @Override
    protected void disposeOWLView() {
        this.signatureSelectionUI.dispose(this.getOWLModelManager());
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

    private JPanel createSignatureAndObservationComponent(){
        JPanel tabbedPaneHolderPanel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Signature", this.createSignatureManagementComponent());
        tabbedPane.addTab("Observation", this.createObservationComponent());
        tabbedPaneHolderPanel.add(tabbedPane, BorderLayout.CENTER);
        return tabbedPaneHolderPanel;
    }

    private JPanel createSignatureManagementComponent(){
        this.signatureSelectionUI.createSignatureSelectionComponents(this.getOWLEditorKit());
        JPanel ontologySignaturePanel = this.signatureSelectionUI.getOntologySignatureTabbedPanel();
        JPanel signatureManagementPanel = new JPanel(new GridBagLayout());
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
        signatureManagementPanel.add(ontologySignaturePanel, constraints);
        JPanel signatureSelectionToolPanel = this.signatureSelectionUI.getSignatureSelectionButtons();
//        specific for signature selected buttons:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        signatureManagementPanel.add(signatureSelectionToolPanel, constraints);
        JPanel listPane = this.signatureSelectionUI.getSelectedSignatureListPanel();
//        specific for selected signature pane:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        signatureManagementPanel.add(listPane, constraints);
        return signatureManagementPanel;
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

    private JSplitPane createSettingsAndAbductionComponent(){
        JPanel upperPanel = this.createSettingsComponent();
        JPanel lowerPanel = this.createAbductionComponent();
        JSplitPane observationAndAbductionPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                upperPanel, lowerPanel);
        observationAndAbductionPanel.setDividerLocation(0.3);
        return observationAndAbductionPanel;
    }

    private JPanel createSettingsComponent(){
        JPanel settingsHolderPanel = new JPanel();
        settingsHolderPanel.setLayout(new BoxLayout(settingsHolderPanel, BoxLayout.PAGE_AXIS));
        Vector<String> abductionNames = this.abductionGeneratorManager.getAbductionGeneratorNames();
        JComboBox<String> abductionNamesComboBox = new JComboBox<>(abductionNames);
        abductionNamesComboBox.setSelectedIndex(0);
        abductionNamesComboBox.addActionListener(this.abductionGeneratorManager);
        settingsHolderPanel.add(abductionNamesComboBox);
        settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
        settingsHolderPanel.add(spinnerHelperPanel);
        settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        this.computeButton = this.createButton(this.COMPUTE_COMMAND, this.COMPUTE_NAME, this.COMPUTE_TOOLTIP);
        JPanel buttonHelperPanel = new JPanel();
        buttonHelperPanel.setLayout(new BoxLayout(buttonHelperPanel, BoxLayout.LINE_AXIS));
        buttonHelperPanel.add(this.computeButton);
        buttonHelperPanel.add(Box.createGlue());
        settingsHolderPanel.add(buttonHelperPanel);
        settingsHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), "Settings:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return settingsHolderPanel;
    }

    private JPanel createObservationComponent(){
        JPanel observationHolderPanel = new JPanel();
        observationHolderPanel.setLayout(new GridBagLayout());
        JPanel observationTabbedPanel = this.createObservationEditorTabbedPanel();
        GridBagConstraints constraints = new GridBagConstraints();
//        general constraints:
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = this.STANDARD_INSETS;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
//        specific for tabbed editor panel:
        constraints.gridy= 0;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        observationHolderPanel.add(observationTabbedPanel, constraints);
        JPanel buttonPanel = this.createObservationButtonPanel();
//        specific for button panel:
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        observationHolderPanel.add(buttonPanel, constraints);
        JPanel selectedObservationPanel = this.createSelectedObservationPanel();
//        specific for selected observations:
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.6;
        observationHolderPanel.add(selectedObservationPanel, constraints);
        return observationHolderPanel;
    }

    private JPanel createObservationEditorTabbedPanel(){
        this.observationTabbedPane = new JTabbedPane();
        OWLExpressionChecker<OWLClassAxiom> classAxiomChecker =
                this.getOWLModelManager().getOWLExpressionCheckerFactory().getClassAxiomChecker();
        this.observationTBoxEditor = new ExpressionEditor<>(this.getOWLEditorKit(), classAxiomChecker);
        this.observationTabbedPane.addTab("OWLClassAxiom", ComponentFactory.createScrollPane(this.observationTBoxEditor));
        OWLExpressionChecker<OWLClassAssertionAxiom> classAssertionChecker =
                new OWLClassAssertionChecker(this.getOWLModelManager());
        this.observationABoxEditor = new ExpressionEditor<>(this.getOWLEditorKit(), classAssertionChecker);
        this.observationTabbedPane.addTab("OWLClassAssertionAxiom", ComponentFactory.createScrollPane(this.observationABoxEditor));
        JPanel observationEditorHolderPanel = new JPanel(new BorderLayout());
        observationEditorHolderPanel.add(this.observationTabbedPane, BorderLayout.CENTER);
        observationEditorHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Enter observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationEditorHolderPanel;
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
        this.deleteObserationButton = this.createButton(this.DELETE_OBSERVATION_COMMAND,
                this.DELETE_OBSERVATION_NAME, this.DELETE_OBSERVATION_TOOLTIP);
        toolbar.add(this.deleteObserationButton);
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
        this.selectedObservationList.setCellRenderer(new OWLCellRendererSimple(this.getOWLEditorKit()));
        observationPanel.add(this.selectedObservationList, BorderLayout.CENTER);
        observationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Selected observation:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationPanel;
    }

    private JPanel createAbductionComponent(){
        this.abductionPanel = new JPanel(new BorderLayout());
        this.abductionScrollPane = new JScrollPane();
        this.abductionPanel.add(this.abductionScrollPane);
        this.abductionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Abductions:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return this.abductionPanel;
    }

    private void computeAbductions(){
        this.logger.debug("computeAbduction called");
        AbductionGenerator<Set<OWLObject>, OWLEntity, OWLOntology, Set<Set<OWLAxiom>>> abductionSolver =
                this.abductionGeneratorManager.getCurrentAbductionGenerator();
        abductionSolver.setAbducibles(new HashSet<>(this.signatureSelectionUI.getSelectedSignature()));
        abductionSolver.setOntology(this.getOWLModelManager().getActiveOntology());
        this.currentObservations = new HashSet<>(this.selectedObservationListModel.getOwlObjects());
        abductionSolver.setObservation(this.currentObservations);
        AbductionGenerationThread abductionGenerationThread = new AbductionGenerationThread(this);
        abductionGenerationThread.setAbductionGenerator(abductionSolver);
        this.loadingUI = new AbductionLoadingUI(this.abductionGeneratorManager.getCurrentAbductionGeneratorName(),
                this.getOWLEditorKit());
        this.loadingUI.showLoadingScreen();
        abductionGenerationThread.start();
    }

    public void showResults(Set<Set<OWLAxiom>> abductions){
        this.logger.debug("Showing results of abduction generation process");
//        SwingUtilities.invokeLater(() ->{
            JLabel explanationLabel = new JLabel("Generated for the following observations:\n");
            explanationLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            explanationLabel.setVerticalTextPosition(SwingConstants.CENTER);
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
            labelPanel.add(explanationLabel);
            labelPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            int indent = 1;
            for (OWLObject observation : this.currentObservations){
                String observationText = "";
                if (indent == 1){
                    observationText += "            ";
                }
                else{
                    observationText += "        ";
                }
                indent = 1 - indent;
                observationText += observation.toString();
                JLabel observationLabel = new JLabel(observationText);
                observationLabel.setHorizontalAlignment(SwingConstants.LEFT);
                observationLabel.setVerticalTextPosition(SwingConstants.CENTER);
                labelPanel.add(observationLabel);
                labelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            JPanel labelHolderPanel = new JPanel();
            labelHolderPanel.setLayout(new BoxLayout(labelHolderPanel, BoxLayout.LINE_AXIS));
            labelHolderPanel.add(labelPanel);
            labelHolderPanel.add(Box.createGlue());
            JPanel scrollingPanel = new JPanel();
            scrollingPanel.setLayout(new BoxLayout(scrollingPanel, BoxLayout.PAGE_AXIS));
            this.abductionScrollPane.setViewportView(scrollingPanel);
            scrollingPanel.add(labelHolderPanel);
            int abductionIndex = 1;
            for (Set<OWLAxiom> abduction : abductions){
                JPanel singleResultPanel = new JPanel();
                singleResultPanel.setLayout(new BorderLayout());
                singleResultPanel.setBorder(new CompoundBorder(
                        new EmptyBorder(5, 5, 5, 5),
                        new CompoundBorder(new LineBorder(Color.BLACK, 1),
                                new EmptyBorder(5, 5, 5, 5))));
                JPanel labelAndButtonPanel = new JPanel();
                labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("Abduction " + abductionIndex);
                abductionIndex++;
                labelAndButtonPanel.add(label);
                labelAndButtonPanel.add(Box.createHorizontalGlue());
                JButton addToOntologyButton = new JButton(this.ADD_TO_ONTO_NAME);
                addToOntologyButton.setToolTipText(this.ADD_TO_ONTO_TOOLTIP);
                addToOntologyButton.setActionCommand(this.ADD_TO_ONTO_COMMAND);
                addToOntologyButton.addActionListener(new AddToOntologyButtonListener(
                        this.getOWLModelManager().getActiveOntology(), abduction));
                labelAndButtonPanel.add(addToOntologyButton);
                singleResultPanel.add(labelAndButtonPanel, BorderLayout.PAGE_START);
                OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
                resultListModel.addElements(abduction);
                JList<OWLAxiom> resultList = new JList<>(resultListModel);
                resultList.setCellRenderer(new OWLCellRendererSimple(this.getOWLEditorKit()));
                singleResultPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);
                scrollingPanel.add(singleResultPanel);
            }
            this.abductionScrollPane.revalidate();

//        });
    }

    private void showError(String message){
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
            int tabIndex = this.observationTabbedPane.getSelectedIndex();
            if (tabIndex == 0){
                try {
                    OWLClassAxiom axiomToAdd = this.observationTBoxEditor.createObject();
                    this.selectedObservationListModel.checkAndAddElement(axiomToAdd);
                } catch (OWLException e) {
                    this.logger.debug("Exception caught when adding observation from TBox: " + e);
                }
                finally{
                    this.selectedObservationList.clearSelection();
                }
            }
            else if (tabIndex == 1){
                try{
                    OWLClassAssertionAxiom axiomToAdd = this.observationABoxEditor.createObject();
                    this.selectedObservationListModel.checkAndAddElement(axiomToAdd);
                } catch (OWLException e) {
                    this.logger.debug("Exception caught when adding observation from ABox: " + e);
                }
                finally{
                    this.selectedObservationList.clearSelection();
                }
            }
        });
    }

    private void deleteObservation(){
        SwingUtilities.invokeLater(() -> {
            java.util.List<OWLObject> toDelete = this.selectedObservationList.getSelectedValuesList();
            this.selectedObservationListModel.removeElements(toDelete);
            this.selectedObservationList.clearSelection();
        });
    }

    private void resetObservation(){
        SwingUtilities.invokeLater(() -> {
            this.selectedObservationListModel.removeAll();
            this.selectedObservationList.clearSelection();
        });
    }

    public void abductionGenerationCompleted(){
        this.loadingUI.disposeLoadingScreen();
    }

//    todo: listen for ontology-reloads/loads and clear observation + results in this case

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

    private class OWLClassAssertionChecker implements OWLExpressionChecker<OWLClassAssertionAxiom>{

        private OWLModelManager manager;

        public OWLClassAssertionChecker(OWLModelManager manager){
            this.manager = manager;
        }

        @Override
        public void check(String input) throws OWLExpressionParserException {
            this.createObject(input);
        }

        @Override
        public OWLClassAssertionAxiom createObject(String input) throws OWLExpressionParserException {
            ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(
                    OWLOntologyLoaderConfiguration::new,
                    this.manager.getOWLDataFactory());
            parser.setOWLEntityChecker(
                    new ProtegeOWLEntityChecker(
                            this.manager.getOWLEntityFinder()));
            parser.setStringToParse(input);
            try {
                OWLAxiom axiom = parser.parseAxiom();
//                todo: check axiom for isLogicalAxiom instead (oder: wrapper der beide checkt)
//                todo: parser für role-assertions muss eingebaut werden
                if(axiom instanceof OWLLogicalAxiom) {
                    return (OWLClassAssertionAxiom) axiom;
                }
                else {
                    throw new OWLExpressionParserException(
                            "Expected a class assertion axiom of the form A Type: C"
                            , 0, 0, true, false, false, true, false, false, Collections.emptySet());
                }
            }
            catch (ParserException e) {
                throw ParserUtil.convertException(e);
            }
        }
    }

}
