package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IAbductionSolverResultButtonEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverResultButtonEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ResultButtonEventType;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.ExplanationDialog;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class SingleResultPanel extends JPanel {

    private IAbductionSolverResultButtonEventListener resultButtonListener;
    private JButton addButton, addAndProveButton, deleteButton;
    private final OWLEditorKit owlEditorKit;
    private final List<ExplanationDialogPanel> openedDialogPanels;
    protected static final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    protected static final String ADD_TO_ONTO_NAME = "Add to Ontology";
    protected static final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the ontology";
    protected static final String ADD_TO_ONTO_AND_PROVE_COMMAND = "ADD_AND_PROVE";
    protected static final String ADD_TO_ONTO_AND_PROVE_NAME = "Add to Ontology and prove";
    protected static final String ADD_TO_ONTO_AND_PROVE_TOOLTIP = "Add the axioms of this result to the ontology and show a prove for the missing entailment";
    protected static final String DELETE_FROM_ONTO_COMMAND = "DELETE_FROM_ONTO";
    protected static final String DELETE_FROM_ONTO_NAME = "Delete from Ontology";
    protected static final String DELETE_FROM_ONTO_TOOLTIP = "Delete the axioms of this result from the ontology";

    public SingleResultPanel(OWLEditorKit owlEditorKit, OWLOntology ontology,
                             Set<OWLAxiom> missingEntailment, Set<OWLAxiom> result,
                             int hypothesisIndex) {
        super(new BorderLayout());
        this.owlEditorKit = owlEditorKit;
        this.openedDialogPanels = new ArrayList<>();
        this.createUI(ontology, missingEntailment,
                result, hypothesisIndex);
    }

    private void createUI(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                          Set<OWLAxiom> result, int hypothesisIndex){
        this.setBorder(new CompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                new CompoundBorder(new LineBorder(Color.BLACK, 1),
                        new EmptyBorder(5, 5, 5, 5))));
//        label and buttons at top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
//        JLabel label = new JLabel("Hypothesis " + (hypothesisIndex+1));
//        buttonPanel.add(label);
        buttonPanel.add(Box.createHorizontalGlue());
        this.addButton = UIUtilities.createNamedButton(
                ADD_TO_ONTO_COMMAND, ADD_TO_ONTO_NAME, ADD_TO_ONTO_TOOLTIP,
                new EditOntologyButtonListener(
                ontology, missingEntailment,
                        result, hypothesisIndex));
        buttonPanel.add(this.addButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        EditOntologyButtonListener addAndProveListener = new EditOntologyButtonListener(
                ontology, missingEntailment,
                result, hypothesisIndex);
        this.addAndProveButton = UIUtilities.createNamedButton(
                ADD_TO_ONTO_AND_PROVE_COMMAND, ADD_TO_ONTO_AND_PROVE_NAME,
                ADD_TO_ONTO_AND_PROVE_TOOLTIP, addAndProveListener);
        buttonPanel.add(this.addAndProveButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        this.deleteButton = UIUtilities.createNamedButton(
                DELETE_FROM_ONTO_COMMAND, DELETE_FROM_ONTO_NAME,
                DELETE_FROM_ONTO_TOOLTIP, new EditOntologyButtonListener(
                        ontology, missingEntailment,
                        result, hypothesisIndex));
        this.deleteButton.setEnabled(false);
        buttonPanel.add(this.deleteButton);
        this.add(buttonPanel, BorderLayout.PAGE_START);
//        hypothesis
        String label = "Hypothesis " + (hypothesisIndex+1);
        HypothesisFrame frame = new HypothesisFrame(this.owlEditorKit, label);
        HypothesisFrameList frameList = new HypothesisFrameList(this.owlEditorKit, frame);
        JPanel frameListHolderPanel = new JPanel(new BorderLayout());
        frameListHolderPanel.add(frameList, BorderLayout.CENTER);
        frame.setRootObject(result);
        JScrollPane singleResultScrollPane = new JScrollPane(frameListHolderPanel);
        this.add(singleResultScrollPane, BorderLayout.CENTER);
    }

    public void registerListener(IAbductionSolverResultButtonEventListener listener){
        this.resultButtonListener = listener;
    }

    public void setAddButtonStatus(boolean newStatus){
        this.addButton.setEnabled(newStatus);
        this.addAndProveButton.setEnabled(newStatus);
    }

    public void dispose(){
        for (ExplanationDialogPanel panel : this.openedDialogPanels){
            panel.dispose();
        }
        this.openedDialogPanels.clear();
    }

    private class EditOntologyButtonListener implements ActionListener {

        private final OWLOntology ontology;
        private final Set<OWLAxiom> missingEntailment;
        private final Set<OWLAxiom> hypothesis;
        private final int hypothesisIndex;
        private ExplanationDialogPanel explanationDialogPanel;
        private final Logger logger = LoggerFactory.getLogger(EditOntologyButtonListener.class);

        private EditOntologyButtonListener(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                                           Set<OWLAxiom> hypothesis, int index){
            this.ontology = ontology;
            this.missingEntailment = missingEntailment;
            this.hypothesis = hypothesis;
            this.hypothesisIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()){
                case ADD_TO_ONTO_COMMAND:
                    deleteButton.setEnabled(true);
                    resultButtonListener.handleEvent(
                            new AbductionSolverResultButtonEvent(
                                    ResultButtonEventType.ADD));
                    this.addToOntology();
                    break;
                case ADD_TO_ONTO_AND_PROVE_COMMAND:
                    deleteButton.setEnabled(true);
                    resultButtonListener.handleEvent(
                            new AbductionSolverResultButtonEvent(
                                    ResultButtonEventType.ADD_AND_PROVE));
                    this.addAndProve();
                    break;
                case DELETE_FROM_ONTO_COMMAND:
                    deleteButton.setEnabled(false);
                    resultButtonListener.handleEvent(
                            new AbductionSolverResultButtonEvent(
                                    ResultButtonEventType.DELETE));
                    this.deleteFromOntology();
                    break;
            }
        }

        private void addToOntology(){
            this.logger.debug("Adding axioms of hypothesis {} to ontology", this.hypothesisIndex+1);
            this.add();
            String msgString = "Added " + this.hypothesis.size() + " axioms of hypothesis "
                    + (this.hypothesisIndex+1) + " to the ontology.";
            JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
            JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Added to ontology");
            msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            msgDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
            msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            msgDialog.setVisible(true);
        }

        private void addAndProve(){
            this.logger.debug("Adding axioms of hypothesis {} to ontology and showing proof for missing entailment",
                    this.hypothesisIndex+1);
            this.add();
            this.showProveDialog();
        }

        private void add(){
            this.logger.debug("The following axioms have been added:");
            for (OWLAxiom axiom : this.hypothesis){
                this.logger.debug(axiom.toString());
            }
            this.ontology.getOWLOntologyManager().addAxioms(this.ontology, this.hypothesis);
        }

        private void showProveDialog(){
            JComboBox<OWLAxiom> missingEntailmentComboBox = new JComboBox<>(
                    new Vector<>(this.missingEntailment));
            missingEntailmentComboBox.setSelectedIndex(0);
            this.explanationDialogPanel = new ExplanationDialogPanel(
                    owlEditorKit.getOWLModelManager().getExplanationManager(),
                    (OWLAxiom) missingEntailmentComboBox.getSelectedItem());
            missingEntailmentComboBox.addActionListener(this.explanationDialogPanel);
            openedDialogPanels.add(explanationDialogPanel);
            JPanel explanationHolderPanel = new JPanel();
            this.explanationDialogPanel.refreshPanel();
            explanationHolderPanel.setLayout(new BoxLayout(
                    explanationHolderPanel, BoxLayout.PAGE_AXIS));
            JPanel missingEntailmentComboBoxPanel = new JPanel();
            missingEntailmentComboBoxPanel.setLayout(new BoxLayout(missingEntailmentComboBoxPanel, BoxLayout.LINE_AXIS));
            JLabel missingEntailmentLabel = UIUtilities.createLabel("Missing Entailment:");
            missingEntailmentComboBoxPanel.add(missingEntailmentLabel);
            missingEntailmentComboBoxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            missingEntailmentComboBoxPanel.add(missingEntailmentComboBox);
            explanationHolderPanel.add(missingEntailmentComboBoxPanel);
            explanationHolderPanel.add(this.explanationDialogPanel);
            JOptionPane optionPane = new JOptionPane(explanationHolderPanel,
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
            JDialog dialog = optionPane.createDialog(
                    ProtegeManager.getInstance().getFrame(
                            owlEditorKit.getWorkspace()),
                    "Explanation for hypothesis " + (this.hypothesisIndex+1));
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    openedDialogPanels.remove(explanationDialogPanel);
                    explanationDialogPanel.dispose();
                    explanationDialogPanel = null;
                }
            });
            dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    openedDialogPanels.remove(explanationDialogPanel);
                    explanationDialogPanel.dispose();
                    explanationDialogPanel = null;
                }
            });
            dialog.setModalityType(Dialog.ModalityType.MODELESS);
            dialog.setResizable(true);
            dialog.pack();
            dialog.setVisible(true);
        }

        private void deleteFromOntology(){
            this.logger.debug("Deleting axioms of hypothesis {} from ontology", this.hypothesisIndex+1);
            this.logger.debug("The following axioms have been deleted:");
            for (OWLAxiom axiom : this.hypothesis){
                this.logger.debug(axiom.toString());
            }
            this.ontology.getOWLOntologyManager().removeAxioms(this.ontology, this.hypothesis);
            String msgString = "Removed " + this.hypothesis.size() + " axioms of hypothesis "
                    + (this.hypothesisIndex+1) + " from the ontology.";
            JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
            JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Removed from ontology");
            msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            msgDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
            msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            msgDialog.setVisible(true);
        }

//        public void dispose(){
//            this.logger.debug("Disposing");
//            if (this.explanationDialogPanel != null){
//                this.explanationDialogPanel.dispose();
//                this.explanationDialogPanel = null;
//            }
//            this.logger.debug("Disposed");
//        }
    }

    private class ExplanationDialogPanel extends JPanel implements ActionListener{

        private final ExplanationManager explanationManager;
        private OWLAxiom missingEntailment;
        private ExplanationDialog internalExplanationDialog = null;

        private final Logger logger = LoggerFactory.getLogger(ExplanationDialogPanel.class);

        private ExplanationDialogPanel(ExplanationManager manager, OWLAxiom missingEntailment){
            this.explanationManager = manager;
            this.missingEntailment = missingEntailment;
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.logger.debug("ExplanationDialogPanel created");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.missingEntailment = (OWLAxiom) ((JComboBox) e.getSource()).getSelectedItem();
            this.logger.debug("Selected missing entailment: {}", this.missingEntailment);
            this.refreshPanel();
        }

        protected void refreshPanel(){
            this.logger.debug("Refreshing panel");
            if (this.internalExplanationDialog != null){
                this.internalExplanationDialog.dispose();
            }
            this.internalExplanationDialog = new ExplanationDialog(
                    this.explanationManager, this.missingEntailment);
            this.removeAll();
            this.add(this.internalExplanationDialog);
            this.revalidate();
            this.repaint();
            this.logger.debug("Panel refreshed");
        }

        public void dispose(){
            if (this.internalExplanationDialog != null){
                this.internalExplanationDialog.dispose();
            }
            this.logger.debug("Internal explanation dialog disposed");
        }
    }

}
