package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;

public class AbductionViewComponent extends AbstractOWLViewComponent implements ActionListener {

    private AbductionGeneratorManager abductionGeneratorManager;
    private AbductionSignatureSelectionUI signatureSelectionUI;
    private final Insets STANDARD_INSETS = new Insets(5, 5, 5, 5);
    private ExpressionEditor<Set<OWLClassExpression>> observationExpressionEditor;
    private JButton computeButton;
    private JPanel abductionPanel;
    private JScrollPane abductionScrollPane;
    private final String COMPUTE_COMMAND = "COMPUTE_ABDUCTION";
    private final String COMPUTE_NAME = "Compute";
    private final String COMPUTE_TOOLTIP = "Compute abduction using OWLObjects selected in \"Excluded Dignature\" and Observation class expression";
    private final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    private final String ADD_TO_ONTO_NAME = "Add to Ontology";
    private final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the current active ontology";

    private final Logger logger = LoggerFactory.getLogger(AbductionViewComponent.class);

    public AbductionViewComponent(){
        this.abductionGeneratorManager = new AbductionGeneratorManager();
        this.logger.debug("Object AbductionViewComponent created");
    }

    @Override
    protected void initialiseOWLView() throws Exception {
        this.logger.debug("initialisation started");
        this.signatureSelectionUI = new AbductionSignatureSelectionUI(
                this, this.getOWLEditorKit(),
                this.getOWLModelManager());
            this.setLayout(new BorderLayout(10, 10));
            JPanel signatureManagementPanel = this.createSignatureManagementComponent();
            JSplitPane ObservationAndResultPanel = this.createObservationAndAbductionComponent();
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
                this.computeAbduction();
                break;
        }
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
        JPanel signatureSelectionToolPanel = this.signatureSelectionUI.getSignatureSelectionButtonsAndCheckBox();
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

    private JSplitPane createObservationAndAbductionComponent(){
        JPanel upperPanel = this.createObservationComponent();
        JPanel lowerPanel = this.createAbductionComponent();
        JSplitPane observationAndAbductionPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                upperPanel, lowerPanel);
        observationAndAbductionPanel.setDividerLocation(0.3);
        return observationAndAbductionPanel;
    }

    private JPanel createObservationComponent(){
        JPanel observationAndButtonPanel = new JPanel();
        observationAndButtonPanel.setLayout(new BoxLayout(observationAndButtonPanel, BoxLayout.PAGE_AXIS));
        observationAndButtonPanel.setAlignmentX(CENTER_ALIGNMENT);
        JPanel observationPanel = new JPanel(new BorderLayout());
        OWLExpressionChecker<Set<OWLClassExpression>> checker =
                this.getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionSetChecker();
        this.observationExpressionEditor = new ExpressionEditor<>(this.getOWLEditorKit(), checker);
        this.observationExpressionEditor.setPreferredSize(new Dimension(1000, 100));
        observationPanel.add(ComponentFactory.createScrollPane(this.observationExpressionEditor));
        observationAndButtonPanel.add(observationPanel);
        JPanel buttonAndComboBoxPanel = new JPanel();
        buttonAndComboBoxPanel.setLayout(new BoxLayout(buttonAndComboBoxPanel, BoxLayout.LINE_AXIS));
        this.computeButton = this.createButton(this.COMPUTE_COMMAND, this.COMPUTE_NAME, this.COMPUTE_TOOLTIP);
        buttonAndComboBoxPanel.add(this.computeButton);
        this.observationExpressionEditor.addStatusChangedListener(newState -> {
            this.computeButton.setEnabled(newState);
            this.logger.debug("Compute-Button statusChangeListener was fired");
        });
        buttonAndComboBoxPanel.add(Box.createHorizontalGlue());
        Vector<String> abductionNames = this.abductionGeneratorManager.getAbductionGeneratorNames();
        JComboBox<String> abductionNamesComboBox = new JComboBox<>(abductionNames);
        abductionNamesComboBox.setSelectedIndex(0);
        abductionNamesComboBox.addActionListener(this.abductionGeneratorManager);
        buttonAndComboBoxPanel.add(abductionNamesComboBox);
        observationAndButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        observationAndButtonPanel.add(buttonAndComboBoxPanel);
        observationAndButtonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Observation (class expression):"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return observationAndButtonPanel;
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

    private void computeAbduction(){
        this.logger.debug("computeAbduction called");
        AbductionGenerator<Set<OWLAxiom>, Set<OWLEntity>, OWLOntology, Set<Set<OWLAxiom>>> abductionGenerator =
                this.abductionGeneratorManager.getCurrentAbductionGenerator();
        abductionGenerator.setSignature(new HashSet<>(this.signatureSelectionUI.getSelectedSignature()));
        abductionGenerator.setOntology(this.getOWLModelManager().getActiveOntology());
        Set<OWLClassExpression> observations = null;
        try {
            observations = this.observationExpressionEditor.createObject();
//            todo: check what format observations should have (Axiom? ClassAssertion?)
//            abductionGenerator.setObservations(observations);
        } catch (OWLException e) {
            this.logger.error("Error during creation of Observation axiom from expression checker: " + e);
//            todo: improve error-msg/check when this might happen
            this.showError("Error: Check Observation again, no abduction was computed.");
            return;
        }
        this.showResults(observations, abductionGenerator.generateAbductions());
    }

    private void showResults(Set<OWLClassExpression> observations, Set<Set<OWLAxiom>> abductions){
        this.logger.debug("showResults called");
//        SwingUtilities.invokeLater(() ->{
            StringBuilder topLabelText = new StringBuilder("<html><center>Generated for observation</center>");
            for (OWLClassExpression observation : observations){
                topLabelText.append("<center>").append(observation.toString()).append("</center>");
            }
            JLabel topLabel = this.createLabel(topLabelText.toString());
            JPanel scrollingPanel = new JPanel();
            scrollingPanel.setLayout(new BoxLayout(scrollingPanel, BoxLayout.PAGE_AXIS));
            this.abductionScrollPane.setViewportView(scrollingPanel);
            scrollingPanel.add(topLabel);
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


}
