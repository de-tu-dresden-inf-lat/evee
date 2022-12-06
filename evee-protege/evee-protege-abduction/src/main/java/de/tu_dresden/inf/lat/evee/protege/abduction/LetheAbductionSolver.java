package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.DisjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LetheAbductionSolver extends AbstractAbductionSolver implements Supplier<Set<OWLAxiom>> {

    private Set<OWLAxiom> observation = null;
    private Collection<OWLEntity> abducibles = null;
    private OWLOntology activeOntology = null;
    private final OWLAbducer abducer = new OWLAbducer();
    private final List<DLStatementAdapter> resultConverterList;
    private final Map<OWLOntology, DLStatementCache> ontoStatementMap;
    private int maxLevel;
    private int currentConverterIndex;
    boolean continueStream = false;

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        this.observation = new HashSet<>();
        this.resultConverterList = new ArrayList<>();
        this.ontoStatementMap = new HashMap<>();
        this.maxLevel = 0;
        this.currentConverterIndex = 0;
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
        this.continueStream = false;
    }

    @Override
    public void setAbducibles(Collection<OWLEntity> abducibles) {
        this.abducibles = abducibles;
        this.continueStream = false;
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        this.logger.debug("Resetting DLStatementCache for ontology " + this.activeOntology.getOntologyID().getOntologyIRI());
        DLStatementCache newCache = new DLStatementCache();
        this.ontoStatementMap.put(activeOntology, newCache);
        this.continueStream = false;
    }

    @Override
    public Stream<Set<OWLAxiom>> generateHypotheses() {
        if (this.continueStream) {
            this.logger.debug("Continuing old stream of hypotheses");
        }
        else{
            this.logger.debug("Creating new stream of hypotheses");
            this.continueStream = true;
            DLStatement hypotheses = this.ontoStatementMap.get(this.activeOntology).getStatement(
                    this.observation, this.abducibles);
            assert (hypotheses != null);
            this.maxLevel = 0;
            this.currentConverterIndex = 0;
            this.resultConverterList.clear();
            ((DisjunctiveDLStatement) hypotheses).statements().foreach(statement -> {
                        this.resultConverterList.add(new DLStatementAdapter(
                                (ConjunctiveDLStatement) statement,
                                this.activeOntology));
                        return null;
            });
        }
        return Stream.generate(this);
    }

    @Override
    public Set<OWLAxiom> get() {
        int startIndex = this.currentConverterIndex;
        boolean checkedStartIndex = false;
        while (true){
            if (this.currentConverterIndex == this.resultConverterList.size()){
                this.maxLevel += 1;
                this.currentConverterIndex = 0;
                for (DLStatementAdapter converter : this.resultConverterList){
                    if (! converter.singletonResult()){
                        converter.setMaxLevel(this.maxLevel);
                        converter.createNextLevelList();
                    }
                }
            }
            Set<OWLAxiom> result = this.resultConverterList.get(this.currentConverterIndex).getNextConversion();
            this.currentConverterIndex += 1;
            if (result == null){
//                check if we tried all statements
                if (startIndex == this.currentConverterIndex)
                {
                    if (checkedStartIndex){
                        break;
                    }
                    else {
                        checkedStartIndex = true;
                    }
                }
            }
            else{
                return result;
            }
        }
        return null;
    }

    @Override
    public void abduce() {
        this.logger.debug("Computing new abduction");
        assert (this.activeOntology != null);
        assert (this.observation != null);
        assert (this.abducibles != null);
        this.abducer.setBackgroundOntology(this.activeOntology);
        this.abducer.setAbducibles(new HashSet<>(this.abducibles));
        DLStatement hypotheses = this.abducer.abduce(this.observation);
        this.ontoStatementMap.get(this.activeOntology).putStatement(this.observation, this.abducibles, hypotheses);
    }

    @Override
    public void computeNonEntailmentExplanation() {

    }

    @Override
    public void addListener(ActionListener listener) {

    }

    @Override
    public Component getResultComponent() {
        return null;
    }


//    leftover from computeAbductions:
    //        this.logger.debug("Hypotheses requested, checking if new results need to be calculated");
//        HashSet<OWLAxiom> currentObservation = new HashSet<>(this.selectedObservationListModel.getOwlObjects());
//        ArrayList<OWLEntity> currentAbducibles = new ArrayList<>(this.signatureSelectionUI.getSelectedSignature());
////        todo: check for change of abductionSolver!
//        if ((! this.lastObservation.equals(currentObservation)) || (! this.lastAbducibles.equals(currentAbducibles))){
//            this.lastObservation = currentObservation;
//            this.lastAbducibles = currentAbducibles;
//            this.parametersChanged = true;
//        }
//        if (this.parametersChanged){
//            this.parametersChanged = false;
//            this.logger.debug("Change in observation or abducibles detected, computing NEW hypotheses");
//
//            AbductionSolverThread abductionGenerationThread = new AbductionSolverThread(this, this.abductionSolver);
//            this.hypothesisIndex = 1;
//            this.createAbductionComponent();
//            this.resetView();
//            this.loadingUI = new AbductionLoadingUI(this.nonEntailmentExplainerManager.getCurrentAbductionGeneratorName(),
//                    this.getOWLEditorKit());
//            this.loadingUI.showLoadingScreen();
//            abductionGenerationThread.start();
//        }
//        else {
//            this.logger.debug("No change in observation or abducibles detected, retrieving more previously computed results.");
//            this.showResults();
//        }




//    leftover form showResults:
//int resultNumber = (int) this.abductionNumberSpinner.getValue();
//        this.logger.debug("Showing {} results of abduction generation process", resultNumber);
//        this.hypotheses.clear();
//        this.logger.debug("hypotheses cleared");
//    Stream<Set<OWLAxiom>> resultStream = this.currentExplainer.generateHypotheses();
//        resultStream.limit(resultNumber).forEach(result -> {
//        if (result != null){
//            this.hypotheses.add(result);}
//    });
//        for (Set<OWLAxiom> result : this.hypotheses){
//        JPanel singleResultPanel = new JPanel();
//        singleResultPanel.setLayout(new BorderLayout());
//        singleResultPanel.setBorder(new CompoundBorder(
//                new EmptyBorder(5, 5, 5, 5),
//                new CompoundBorder(new LineBorder(Color.BLACK, 1),
//                        new EmptyBorder(5, 5, 5, 5))));
//        JPanel labelAndButtonPanel = new JPanel();
//        labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
//        JLabel label = new JLabel("Hypothesis " + this.hypothesisIndex);
//        OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
//        resultListModel.addElements(result);
//        JList<OWLAxiom> resultList = new JList<>(resultListModel);
//        labelAndButtonPanel.add(label);
//        labelAndButtonPanel.add(Box.createHorizontalGlue());
//        JButton addToOntologyButton = new JButton(this.ADD_TO_ONTO_NAME);
//        addToOntologyButton.setToolTipText(this.ADD_TO_ONTO_TOOLTIP);
//        addToOntologyButton.setActionCommand(this.ADD_TO_ONTO_COMMAND);
//        addToOntologyButton.addActionListener(new AddToOntologyButtonListener(
//                this.getOWLModelManager().getActiveOntology(), resultList, this.hypothesisIndex));
//        this.hypothesisIndex++;
//        labelAndButtonPanel.add(addToOntologyButton);
//        singleResultPanel.add(labelAndButtonPanel, BorderLayout.PAGE_START);
//        OWLCellRenderer renderer = new OWLCellRenderer(this.getOWLEditorKit());
//        renderer.setHighlightUnsatisfiableClasses(false);
//        renderer.setHighlightUnsatisfiableProperties(false);
//        renderer.setHighlightKeywords(true);
//        renderer.setFeint(true);
//        resultList.setCellRenderer(renderer);
//        JScrollPane singleResultScrollPane = new JScrollPane(resultList);
//        singleResultScrollPane.setPreferredSize(resultList.getPreferredSize());
//        singleResultPanel.add(singleResultScrollPane, BorderLayout.CENTER);
//
//        this.resultScrollingPanel.add(singleResultPanel);
//    }

//    public void abductionGenerationCompleted(){
//        this.loadingUI.disposeLoadingScreen();
//    }

//    leftover from createAbductionComponent
//            this.abductionScrollPane = new JScrollPane();
//        this.resultScrollingPanel = new JPanel();
//        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
//        this.abductionScrollPane.setViewportView(this.resultScrollingPanel);

//    general leftovers
//private final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
//    private final String ADD_TO_ONTO_NAME = "Add to Ontology";
//    private final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the current active ontology";

//    addToOntologyLeftovers
//    private class AddToOntologyButtonListener implements ActionListener{
//
//        private final OWLOntology ontology;
//        private final JList<OWLAxiom> axiomList;
//        private final int hypothesisIndex;
//        private final Logger logger = LoggerFactory.getLogger(AddToOntologyButtonListener.class);
//
//        private AddToOntologyButtonListener(OWLOntology ontology, JList<OWLAxiom> axiomList, int index){
//            this.ontology = ontology;
//            this.axiomList = axiomList;
//            this.hypothesisIndex = index;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            this.logger.debug("Button AddToOntology clicked");
//            if (e.getActionCommand().equals(ADD_TO_ONTO_COMMAND)){
//                this.logger.debug("Adding new Axioms to Ontology:");
//                SwingUtilities.invokeLater(() -> {
//                    Set<OWLAxiom> selectedAxioms = new HashSet<>(axiomList.getSelectedValuesList());
//                    String msgString = "";
//                    if (selectedAxioms.size() == 0){
//                        for (int index = 0; index < axiomList.getModel().getSize(); index++){
//                            selectedAxioms.add(axiomList.getModel().getElementAt(index));
//                        }
//                        msgString += "All axioms ";
//                    }
//                    else {
//                        if (selectedAxioms.size() == 1){
//                            msgString += "Selected axiom ";
//                        }
//                        else{
//                            msgString += "Selected " + selectedAxioms.size() + " axioms ";
//                        }
//                    }
//                    selectedAxioms.forEach(axiom -> {
//                        this.logger.debug(axiom.toString());
//                        this.ontology.getOWLOntologyManager().addAxiom(this.ontology, axiom);
//                    });
//                    msgString += "of hypothesis " + hypothesisIndex + " added to ontology.";
//                    JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
//                    JDialog errorDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
//                            getOWLEditorKit().getWorkspace()), "Added to ontology");
//                    errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
//                    errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
//                            ProtegeManager.getInstance().getFrame(getOWLEditorKit().getWorkspace())));
//                    errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//                    errorDialog.setVisible(true);
//                });
//            }
//        }
//
//    }

}
