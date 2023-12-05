package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverSingleResultPanelEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverSingleResultPanelEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverSingleResultPanelEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.SingleResultPanelEventType;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.SignatureModificationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.ExplanationDialog;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SingleResultPanel extends JPanel
        implements ISignatureModificationEventListener, ISignatureModificationEventGenerator,
        IAbductionSolverSingleResultPanelEventGenerator {

    private IAbductionSolverSingleResultPanelEventListener singleResultPanelEventListener;
    private final OWLEditorKit owlEditorKit;
    protected static final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    protected static final String ADD_TO_ONTO_NAME = "Add to ontology";
    protected static final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the ontology";
    protected static final String EXPLAIN_COMMAND = "EXPLAIN";
    protected static final String EXPLAIN_NAME = "Explain";
    protected static final String EXPLAIN_TOOLTIP = "Show an explanation for the missing entailment";
    protected static final String FORBID_SIGNATURE_COMMAND = "FORBID_SIGNATURE";
    protected static final String FORBID_SIGNATURE_NAME = "Forbid vocabulary";
    protected static final String FORBID_SIGNATURE_TOOLTIP = "Adds the signature of this explanation ";
    private HypothesisFrameList hypothesisFrameList = null;
    private ISignatureModificationEventListener resultManagerSignatureModificationEventListener;
    private SingleResultPanelButtonEventHandler singleResultPanelButtonEventHandler;
    private final Logger logger = LoggerFactory.getLogger(SingleResultPanel.class);

    public SingleResultPanel(OWLEditorKit owlEditorKit, OWLOntology ontology,
                             Set<OWLAxiom> missingEntailment, Set<OWLAxiom> result,
                             int hypothesisIndex) {
        super(new BorderLayout());
        this.logger.debug("Creating single result panel");
        this.owlEditorKit = owlEditorKit;
        this.createUI(ontology, missingEntailment,
                result, hypothesisIndex);
        this.logger.debug("Single result panel created");
    }

    private void createUI(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                          Set<OWLAxiom> result, int hypothesisIndex){
        this.setLayout(new BorderLayout());
        this.setBorder(new CompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                new CompoundBorder(new LineBorder(Color.BLACK, 1),
                        new EmptyBorder(5, 5, 5, 5))));
//        label and buttons at top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        this.singleResultPanelButtonEventHandler =
                new SingleResultPanelButtonEventHandler(
                        ontology, missingEntailment, result, hypothesisIndex);
        this.singleResultPanelButtonEventHandler.registerSignatureModificationEventListener(this);
        JButton explainButton = UIUtilities.createNamedButton(
                EXPLAIN_COMMAND, EXPLAIN_NAME, EXPLAIN_TOOLTIP,
                this.singleResultPanelButtonEventHandler);
        buttonPanel.add(explainButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        JButton forbidSignatureButton = UIUtilities.createNamedButton(
                FORBID_SIGNATURE_COMMAND, FORBID_SIGNATURE_NAME, FORBID_SIGNATURE_TOOLTIP,
                this.singleResultPanelButtonEventHandler);
        buttonPanel.add(forbidSignatureButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        JButton addButton = UIUtilities.createNamedButton(
                ADD_TO_ONTO_COMMAND, ADD_TO_ONTO_NAME, ADD_TO_ONTO_TOOLTIP,
                this.singleResultPanelButtonEventHandler);
        buttonPanel.add(addButton);
        this.add(buttonPanel, BorderLayout.PAGE_START);
//        hypothesis
        String label = "Hypothesis " + (hypothesisIndex+1);
        HypothesisFrame frame = new HypothesisFrame(this.owlEditorKit, label);
        this.hypothesisFrameList = new HypothesisFrameList(this.owlEditorKit, frame);
        IgnoreSelectionModel selectionModel = new IgnoreSelectionModel();
        this.hypothesisFrameList.setSelectionModel(selectionModel);
        JPanel frameListHolderPanel = new JPanel(new BorderLayout());
        frameListHolderPanel.add(this.hypothesisFrameList, BorderLayout.CENTER);
        frame.setRootObject(result);
        JScrollPane singleResultScrollPane = new JScrollPane(frameListHolderPanel);
        this.add(singleResultScrollPane, BorderLayout.CENTER);
    }


    public void dispose(){
        if (this.hypothesisFrameList != null){
            this.hypothesisFrameList.dispose();
        }
        if (this.singleResultPanelButtonEventHandler != null){
            this.singleResultPanelButtonEventHandler.dispose();
        }
    }

    @Override
    public void registerSingleResultPanelEventListener(IAbductionSolverSingleResultPanelEventListener listener){
        this.singleResultPanelEventListener = listener;
    }


    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {
        this.resultManagerSignatureModificationEventListener = listener;
    }

    @Override
    public void handleSignatureModificationEvent(SignatureModificationEvent event) {
        this.resultManagerSignatureModificationEventListener.handleSignatureModificationEvent(event);
    }


    private class SingleResultPanelButtonEventHandler implements ActionListener, ISignatureModificationEventGenerator {

        private final OWLOntology ontology;
        private final Set<OWLAxiom> missingEntailment;
        private final Set<OWLAxiom> result;
        private final int hypothesisIndex;
        private final Set<OWLEntity> resultSignature;
        private ExplanationDialogPanel explanationDialogPanel;
        private ISignatureModificationEventListener signatureModificationEventListener;
        private final Logger logger = LoggerFactory.getLogger(SingleResultPanelButtonEventHandler.class);

        private SingleResultPanelButtonEventHandler(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                                                    Set<OWLAxiom> result, int index){
            this.ontology = ontology;
            this.missingEntailment = missingEntailment;
            this.result = result;
            this.resultSignature = new HashSet<>();
            result.forEach(axiom -> resultSignature.addAll(axiom.getSignature()));
            this.hypothesisIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()){
                case ADD_TO_ONTO_COMMAND:
                    singleResultPanelEventListener.handleEvent(
                            new AbductionSolverSingleResultPanelEvent(
                                    SingleResultPanelEventType.ADD));
                    this.addToOntology();
                    break;
                case EXPLAIN_COMMAND:
                    singleResultPanelEventListener.handleEvent(
                            new AbductionSolverSingleResultPanelEvent(
                                    SingleResultPanelEventType.EXPLAIN));
                    this.explain();
                    break;
                case FORBID_SIGNATURE_COMMAND:
                    this.forbidSignature();
                    break;
            }
        }

        private void addToOntology(){
            this.logger.debug("Adding axioms of hypothesis {} to ontology", this.hypothesisIndex+1);
            this.add();
            String msgString = "Added " + this.result.size() + " axioms of hypothesis "
                    + (this.hypothesisIndex+1) + " to the ontology.";
            JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
            JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Added to ontology");
            msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            msgDialog.pack();
            msgDialog.setLocationRelativeTo(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace()));
            msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            msgDialog.setVisible(true);
        }

        private void explain(){
            this.logger.debug("Adding axioms of hypothesis {} to ontology and showing proof for missing entailment",
                    this.hypothesisIndex+1);
            this.add();
            this.showExplanationDialog();
        }

        private void add(){
            this.logger.debug("The following axioms have been added:");
            for (OWLAxiom axiom : this.result){
                this.logger.debug(axiom.toString());
            }
            this.ontology.getOWLOntologyManager().addAxioms(this.ontology, this.result);
        }

        private void showExplanationDialog(){
            JComboBox<OWLAxiom> missingEntailmentComboBox = new JComboBox<>(
                    new Vector<>(this.missingEntailment));
            missingEntailmentComboBox.setSelectedIndex(0);
            this.explanationDialogPanel = new ExplanationDialogPanel(
                    owlEditorKit.getOWLModelManager().getExplanationManager(),
                    (OWLAxiom) missingEntailmentComboBox.getSelectedItem());
            missingEntailmentComboBox.addActionListener(this.explanationDialogPanel);
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
                    explanationDialogPanel.dispose();
                    explanationDialogPanel = null;
                    singleResultPanelEventListener.handleEvent(
                            new AbductionSolverSingleResultPanelEvent(
                                    SingleResultPanelEventType.EXPLANATION_DIALOG_CLOSED));
                    ontology.getOWLOntologyManager().removeAxioms(ontology, result);
                }
            });
            dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    explanationDialogPanel.dispose();
                    explanationDialogPanel = null;
                    singleResultPanelEventListener.handleEvent(
                            new AbductionSolverSingleResultPanelEvent(
                                    SingleResultPanelEventType.EXPLANATION_DIALOG_CLOSED));
                    ontology.getOWLOntologyManager().removeAxioms(ontology, result);
                }
            });
            dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            dialog.setResizable(true);
            dialog.pack();
            dialog.setVisible(true);
        }

        @Override
        public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {
            this.signatureModificationEventListener = listener;
        }

        public void dispose(){
            this.logger.debug("Disposing abduction result explanation dialog");
            if (this.explanationDialogPanel != null){
                this.explanationDialogPanel.dispose();
                this.explanationDialogPanel = null;
            }
            this.logger.debug("Abduction result explanation dialog disposed");
        }

        private void forbidSignature(){
            this.signatureModificationEventListener.handleSignatureModificationEvent(
                    new SignatureModificationEvent(this.resultSignature));
        }

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

    private static class IgnoreSelectionModel extends DefaultListSelectionModel {

        private final Logger logger = LoggerFactory.getLogger(IgnoreSelectionModel.class);

        private IgnoreSelectionModel(){
            logger.debug("SelectionModel created");
        }

        @Override
        public void setAnchorSelectionIndex(int anchorIndex) {
            logger.debug("setAnchorSelectionIndex called with index: {}", anchorIndex);
        }

        @Override
        public void setLeadAnchorNotificationEnabled(boolean flag) {
            logger.debug("setLeadAnchorNotificationEnabled called with boolean: {}", flag);
        }

        @Override
        public void setLeadSelectionIndex(int leadIndex) {
            logger.debug("setLeadSelectionIndex called with index: {}", leadIndex);
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            logger.debug("setSelectionInterval called with index0, index1: {}, {}", index0, index1);
        }
    }

}
