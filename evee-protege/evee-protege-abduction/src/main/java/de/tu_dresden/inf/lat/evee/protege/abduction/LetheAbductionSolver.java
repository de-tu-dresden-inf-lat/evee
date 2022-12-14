package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationEventType;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.DisjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import javax.annotation.Nonnull;
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

public class LetheAbductionSolver implements NonEntailmentExplanationService, Supplier<Set<OWLAxiom>> {

    private Set<OWLAxiom> observation = null;
//    private Set<OWLAxiom> lastUsedObservation = null;
    private Collection<OWLEntity> abducibles = null;
//    private Collection<OWLEntity> lastUsedAbducibles = null;
    private OWLOntology activeOntology = null;
//    private OWLOntology lastUsedOntology = null;
    private OWLEditorKit owlEditorKit;
    private AbductionLoadingUI loadingUI;
    protected JPanel settingsHolderPanel;
    protected JSpinner abductionNumberSpinner;
    private JPanel resultHolderPanel;
    private JPanel resultScrollingPanel;
    private int hypothesisIndex;
    private final LetheAbductionSolverOntologyChangeListener changeListener;
    private NonEntailmentExplanationListener viewComponentListener;
    private final OWLAbducer abducer;
    private final List<DLStatementAdapter> hypothesesAdapterList;
    private final Map<OWLOntology, DLStatementCache> cachedResults;
    private int maxLevel;
    private int currentResultAdapterIndex;
    private boolean parametersChanged = true;
//    private final static String SERVICE_NAME = "Abduction Solver (Lethe)";
    private final static String LOADING = "LOADING";
    protected static final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    protected static final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";
    protected static final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    protected static final String ADD_TO_ONTO_NAME = "Add to Ontology";
    protected static final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the current active ontology";

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.changeListener = new LetheAbductionSolverOntologyChangeListener();
        this.observation = new HashSet<>();
        this.hypothesesAdapterList = new ArrayList<>();
        this.cachedResults = new HashMap<>();
        this.abducer  = new OWLAbducer();
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.hypothesisIndex = 1;
        this.createSettingsComponent();
        this.logger.debug("AbstractAbductionSolver created successfully.");
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
        this.parametersChanged = true;
    }

    @Override
    public void setSignature(Collection<OWLEntity> abducibles) {
        this.abducibles = abducibles;
        this.parametersChanged = true;
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        this.parametersChanged = true;
    }

    @Override
    public Stream<Set<OWLAxiom>> generateHypotheses() {
        return Stream.generate(this);
    }

    @Override
    public boolean supportsMultiObservation() {
        return true;
    }

    @Override
    public Set<OWLAxiom> get() {
        int startIndex = this.currentResultAdapterIndex;
        boolean checkedStartIndex = false;
        while (true){
            if (this.currentResultAdapterIndex == this.hypothesesAdapterList.size()){
                this.maxLevel += 1;
                this.currentResultAdapterIndex = 0;
                for (DLStatementAdapter converter : this.hypothesesAdapterList){
                    if (! converter.singletonResult()){
                        converter.setMaxLevel(this.maxLevel);
                        converter.createNextLevelList();
                    }
                }
            }
            Set<OWLAxiom> result = this.hypothesesAdapterList.get(this.currentResultAdapterIndex).getNextConversion();
            this.currentResultAdapterIndex += 1;
            if (result == null){
//                check if we tried all statements
                if (startIndex == this.currentResultAdapterIndex)
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
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

//    @Override
//    public String getName() {
//        return SERVICE_NAME;
//    }

    @Override
    public void computeExplanation() {
        if (this.parametersChanged){
            this.logger.debug("Parameters changed, creating new stream");
            if (this.cachedResults.get(this.activeOntology).containsStatement(this.observation, this.abducibles)){
                this.logger.debug("Cached result found, no computation of hypotheses necessary");
                this.prepareResultComponentCreation();
                this.createResultComponent();
            }
            else{
                this.logger.debug("No cached result found, computing new hypotheses");
                this.computeNewExplanation();
            }
            this.hypothesisIndex = 1;
            this.parametersChanged = false;
        }
        else{
            this.logger.debug("Parameters unchanged, continuing old stream");
            this.createResultComponent();
        }
    }

    @Override
    public Component getResultComponent() {
        return this.resultHolderPanel;
    }

    @Override
    public Component getSettingsComponent() {
        return this.settingsHolderPanel;
    }

    @Override
    public void registerListener(NonEntailmentExplanationListener listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public void initialise() {
        this.logger.debug("Initialising AbstractAbductionSolver");
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.resetCache();
        this.logger.debug("AbstractAbductionSolver initialised successfully");
    }

    @Override
    public void dispose() {
        this.owlEditorKit.getOWLModelManager().removeOntologyChangeListener(this.changeListener);
    }

    private void computeNewExplanation(){
        assert (this.activeOntology != null);
        assert (this.observation != null);
        assert (this.abducibles != null);
        this.abducer.setBackgroundOntology(this.activeOntology);
        this.abducer.setAbducibles(new HashSet<>(this.abducibles));
        AbductionSolverThread thread = new AbductionSolverThread(this, this.abducer, this.observation);
        this.loadingUI = new AbductionLoadingUI(LOADING, this.owlEditorKit);
        this.loadingUI.showLoadingScreen();
        thread.start();
    }

    protected void newExplanationComputationCompleted(DLStatement hypotheses){
        this.cachedResults.get(this.activeOntology).putStatement(this.observation, this.abducibles, hypotheses);
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    protected void prepareResultComponentCreation(){
        DLStatement hypotheses = this.cachedResults.get(this.activeOntology).getStatement(
                this.observation, this.abducibles);
        assert (hypotheses != null);
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.hypothesesAdapterList.clear();
        ((DisjunctiveDLStatement) hypotheses).statements().foreach(statement -> {
            this.hypothesesAdapterList.add(new DLStatementAdapter(
                    (ConjunctiveDLStatement) statement,
                    this.activeOntology));
            return null;
        });
        SwingUtilities.invokeLater(() -> {
            this.resultHolderPanel = new JPanel(new BorderLayout());
            this.resultScrollingPanel = new JPanel();
            this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
            JScrollPane resultScrollPane = new JScrollPane();
            resultScrollPane.setViewportView(this.resultScrollingPanel);
            this.resultHolderPanel.add(resultScrollPane);
            this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(5, 5, 5, 5),
                            "Hypotheses:"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        });
    }

    private void resetCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Resetting DLStatementCache for ontology " + ontology.getOntologyID().getOntologyIRI());
        DLStatementCache newCache = new DLStatementCache();
        this.cachedResults.put(ontology, newCache);
        this.parametersChanged = true;
    }

    protected JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    private void createSettingsComponent(){
        SwingUtilities.invokeLater(() -> {
            this.settingsHolderPanel = new JPanel();
            this.settingsHolderPanel.setLayout(new BoxLayout(settingsHolderPanel, BoxLayout.PAGE_AXIS));
            JPanel spinnerHelperPanel = new JPanel();
            spinnerHelperPanel.setLayout(new BoxLayout(spinnerHelperPanel, BoxLayout.LINE_AXIS));
            JLabel label = this.createLabel(SETTINGS_LABEL);
            spinnerHelperPanel.add(label);
            spinnerHelperPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, null, 1);
            this.abductionNumberSpinner = new JSpinner(spinnerModel);
            this.abductionNumberSpinner.setToolTipText(SETTINGS_SPINNER_TOOLTIP);
            this.abductionNumberSpinner.setMaximumSize(new Dimension(500, this.abductionNumberSpinner.getPreferredSize().height));
            spinnerHelperPanel.add(this.abductionNumberSpinner);
            spinnerHelperPanel.add(Box.createGlue());
            this.settingsHolderPanel.add(spinnerHelperPanel);
//        this.settingsHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            this.settingsHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(), "Settings:"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        });
    }

    private void createResultComponent(){
        SwingUtilities.invokeLater(() -> {
            int resultNumber = (int) this.abductionNumberSpinner.getValue();
            this.logger.debug("Trying to show {} results of abduction generation process", resultNumber);
            List<Set<OWLAxiom>> hypotheses = new ArrayList<>();
            Stream<Set<OWLAxiom>> resultStream = this.generateHypotheses();
            resultStream.limit(resultNumber).forEach(result -> {
                if (result != null){
                    hypotheses.add(result);}
            });
            this.logger.debug("Actually showing {} results of abduction generation process", hypotheses.size());
            for (Set<OWLAxiom> result : hypotheses) {
                JPanel singleResultPanel = new JPanel();
                singleResultPanel.setLayout(new BorderLayout());
                singleResultPanel.setBorder(new CompoundBorder(
                        new EmptyBorder(5, 5, 5, 5),
                        new CompoundBorder(new LineBorder(Color.BLACK, 1),
                                new EmptyBorder(5, 5, 5, 5))));
                JPanel labelAndButtonPanel = new JPanel();
                labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("Hypothesis " + this.hypothesisIndex);
                OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
                resultListModel.addElements(result);
                JList<OWLAxiom> resultList = new JList<>(resultListModel);
                labelAndButtonPanel.add(label);
                labelAndButtonPanel.add(Box.createHorizontalGlue());
                JButton addToOntologyButton = new JButton(ADD_TO_ONTO_NAME);
                addToOntologyButton.setToolTipText(ADD_TO_ONTO_TOOLTIP);
                addToOntologyButton.setActionCommand(ADD_TO_ONTO_COMMAND);
                addToOntologyButton.addActionListener(new AddToOntologyButtonListener(
                        this.activeOntology, resultList, this.hypothesisIndex));
                this.hypothesisIndex++;
                labelAndButtonPanel.add(addToOntologyButton);
                singleResultPanel.add(labelAndButtonPanel, BorderLayout.PAGE_START);
                OWLCellRenderer renderer = new OWLCellRenderer(this.owlEditorKit);
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
            this.resultHolderPanel.repaint();
            this.resultHolderPanel.revalidate();
            this.viewComponentListener.handleEvent(new NonEntailmentExplanationEvent(this,
                    NonEntailmentExplanationEventType.COMPUTATION_COMPLETE));
        });
    }

    protected void disposeLoadingScreen(){
        this.loadingUI.disposeLoadingScreen();
    }

    public void showError(String message){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
                    this.owlEditorKit.getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.owlEditorKit.getWorkspace())));
            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            errorDialog.setVisible(true);
        });
    }

    private class LetheAbductionSolverOntologyChangeListener implements OWLOntologyChangeListener {

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            resetCache();
        }
    }

    private class AddToOntologyButtonListener implements ActionListener {

        private final OWLOntology ontology;
        private final JList<OWLAxiom> axiomList;
        private final int hypothesisIndex;
        private final Logger logger = LoggerFactory.getLogger(AddToOntologyButtonListener.class);

        private AddToOntologyButtonListener(OWLOntology ontology, JList<OWLAxiom> axiomList, int index){
            this.ontology = ontology;
            this.axiomList = axiomList;
            this.hypothesisIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.logger.debug("Button AddToOntology clicked");
            if (e.getActionCommand().equals(ADD_TO_ONTO_COMMAND)){
                this.logger.debug("Adding new Axioms to Ontology:");
                SwingUtilities.invokeLater(() -> {
                    Set<OWLAxiom> selectedAxioms = new HashSet<>(axiomList.getSelectedValuesList());
                    String msgString = "";
                    if (selectedAxioms.size() == 0){
                        for (int index = 0; index < axiomList.getModel().getSize(); index++){
                            selectedAxioms.add(axiomList.getModel().getElementAt(index));
                        }
                        msgString += "All axioms ";
                    }
                    else {
                        if (selectedAxioms.size() == 1){
                            msgString += "Selected axiom ";
                        }
                        else{
                            msgString += "Selected " + selectedAxioms.size() + " axioms ";
                        }
                    }
                    selectedAxioms.forEach(axiom -> {
                        this.logger.debug(axiom.toString());
                        this.ontology.getOWLOntologyManager().addAxiom(this.ontology, axiom);
                    });
                    msgString += "of hypothesis " + hypothesisIndex + " added to ontology.";
                    JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
                    JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                            owlEditorKit.getWorkspace()), "Added to ontology");
                    msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
                    msgDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                            ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
                    msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    msgDialog.setVisible(true);
                });
            }
        }

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


}
