package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.OWLObjectListModel;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static org.junit.Assert.assertNotNull;

abstract public class AbstractAbductionSolver<Result> implements Supplier<Set<OWLAxiom>>,
        INonEntailmentExplanationService<OWLAxiom>, IOWLAbductionSolver {

    protected Set<OWLAxiom> observation = null;
    protected Set<OWLAxiom> lastUsedObservation = null;
    protected Set<OWLEntity> abducibles = null;
    protected Set<OWLEntity> lastUsedAbducibles = null;
    protected OWLOntology ontology = null;
    protected AbductionLoadingUI loadingUI;
    protected JPanel settingsHolderPanel;
    private JSpinner abductionNumberSpinner;
    private OWLEditorKit owlEditorKit;
    private JPanel resultHolderPanel = null;
    private JPanel resultScrollingPanel;
    protected int hypothesisIndex;
    private boolean computationSuccessful;
    private boolean activeOntologyEditedExternally = false;
    private boolean activeOntologyEditedByAbductionSolver = false;
    private boolean activeOntologyChanged = false;
    private boolean ignoreOntologyChangeEvent = false;
    private final List<JButton> addButtonList, addAndProveButtonList, deleteButtonList;
    private final AbductionSolverOntologyChangeListener ontologyChangeListener;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected final Map<OWLOntology, AbductionCache<Result>> cachedResults;
    private AbductionCache<Result> savedCache = null;
    protected static final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    protected static final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";
    protected static final String ADD_TO_ONTO_COMMAND = "ADD_TO_ONTO";
    protected static final String ADD_TO_ONTO_NAME = "Add to Ontology";
    protected static final String ADD_TO_ONTO_TOOLTIP = "Adds the axioms of this result to the ontology";
    protected static final String ADD_TO_ONTO_AND_PROVE_COMMAND = "ADD_AND_PROVE";
    protected static final String ADD_TO_ONTO_AND_PROVE_NAME = "Add to Ontology and prove";
    protected static final String ADD_TO_ONTO_AND_PROVE_TOOLTIP = "Add the axioms of this result to the ontology and show a prove for the observation";
    protected static final String DELETE_FROM_ONTO_COMMAND = "DELETE_FROM_ONTO";
    protected static final String DELETE_FROM_ONTO_NAME = "Delete from Ontology";
    protected static final String DELETE_FROM_ONTO_TOOLTIP = "Delete the axioms of this result from the ontology";

    private final Logger logger = LoggerFactory.getLogger(AbstractAbductionSolver.class);

    public AbstractAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.hypothesisIndex = 0;
        this.cachedResults = new HashMap<>();
        this.addButtonList = new ArrayList<>();
        this.addAndProveButtonList = new ArrayList<>();
        this.deleteButtonList = new ArrayList<>();
        this.ontologyChangeListener = new AbductionSolverOntologyChangeListener();
        this.createSettingsComponent();
        this.logger.debug("AbstractAbductionSolver created successfully.");
    }

    public void setComputationSuccessful(boolean successful){
        this.computationSuccessful = successful;
    }

    public boolean computationSuccessful(){
        return this.computationSuccessful;
    }

    @Override
    public void initialise(){
        this.logger.debug("Initialising AbductionSolver");
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.ontologyChangeListener);
        this.owlEditorKit.getOWLModelManager().addListener(this.ontologyChangeListener);
        this.resetCache();
        this.logger.debug("AbductionSolver initialised");
    }

    @Override
    public void dispose() {
        this.logger.debug("Disposing AbductionSolver");
        this.owlEditorKit.getOWLModelManager().removeOntologyChangeListener(this.ontologyChangeListener);
        this.owlEditorKit.getOWLModelManager().removeListener(this.ontologyChangeListener);
        this.logger.debug("AbductionSolver disposed");
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.logger.debug("Setting observation");
        if (this.observation == null){
            this.observation = observation;
        }
        else if (! this.observation.equals(observation)){
            this.observation = observation;
        }
    }

    @Override
    public void setSignature(Collection<OWLEntity> abducibles) {
        this.logger.debug("Setting signature");
        HashSet<OWLEntity> abduciblesAsSet = new HashSet<>(abducibles);
        if (this.abducibles == null){
            this.abducibles = abduciblesAsSet;
        }
        else if (! this.abducibles.equals(abduciblesAsSet)){
            this.abducibles = abduciblesAsSet;
        }
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.logger.debug("Setting ontology");
        if (this.ontology == null){
            this.logger.debug("No ontology has been set yet, setting ontology");
            this.ontology = ontology;
        }
        else if (! this.ontology.equals(ontology)){
            this.logger.debug("Different ontology detected, setting ontology");
            this.ontology = ontology;
        }
        if (this.cachedResults.get(ontology) == null){
            this.logger.debug("No cache for ontology, creating new cache");
            this.cachedResults.put(ontology, new AbductionCache<>());
        }
    }

    @Override
    public Stream<Set<OWLAxiom>> generateExplanations() {
        return Stream.generate(this);
    }

    @Override
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

    @Override
    public Component getResult() {
        return this.resultHolderPanel;
    }

    @Override
    public Component getSettingsComponent() {
        return this.settingsHolderPanel;
    }

    @Override
    public void registerListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public void computeExplanation() {
        this.logger.debug("Computing explanation");
        assertNotNull(this.ontology);
        assertNotNull(this.observation);
        assertNotNull(this.abducibles);
        if (this.parametersChanged()){
            this.logger.debug("Abduction parameters changed, creating new stream");
            this.lastUsedObservation = this.observation;
            this.lastUsedAbducibles = this.abducibles;
            if ( ! (this.activeOntologyEdited() || this.activeOntologyChanged) &&
                    this.cachedResults.get(this.ontology).containsResultFor(
                            this.observation, this.abducibles)){
                this.logger.debug("Cached result found, no computation of hypotheses necessary");
                this.computationSuccessful = true;
                this.redisplayCachedExplanation();
            }
            else{
                this.logger.debug("No cached result found, computing new hypotheses");
                this.resetEditOntologyStatus();
                this.resetResultComponent();
                this.createNewExplanation();
            }
            this.activeOntologyChanged = false;
            this.hypothesisIndex = 0;
        }
        else{
            this.logger.debug("Abduction parameters unchanged");
            if (this.activeOntologyChanged){
                this.logger.debug("Ontology changed since last request for computation");
                this.activeOntologyChanged = false;
                if (this.cachedResults.get(this.ontology).containsResultFor(
                        this.observation, this.abducibles)){
                    this.logger.debug("Cached result found, no computation of hypotheses necessary");
                    this.computationSuccessful = true;
                    this.redisplayCachedExplanation();
                } else{
                    this.logger.debug("No cached result found, computing new hypotheses");
                    this.resetEditOntologyStatus();
                    this.resetResultComponent();
                    this.createNewExplanation();
                }
                this.hypothesisIndex = 0;
            } else{
                this.logger.debug("Ontology not changed since last request for computation");
                if (this.activeOntologyEdited()){
                    this.logger.debug("Changes made to ontology since last computation, re-computation of hypotheses necessary");
                    this.logger.debug("External changes: {} - AbductionSolverChangse: {}", this.activeOntologyEditedExternally, this.activeOntologyEditedByAbductionSolver);
                    this.lastUsedObservation = this.observation;
                    this.lastUsedAbducibles = this.abducibles;
                    this.resetEditOntologyStatus();
                    this.resetResultComponent();
                    this.createNewExplanation();
                    this.hypothesisIndex = 0;
                } else{
                    this.logger.debug("No changes made to ontology since last computation");
                    if (this.computationSuccessful){
                        this.logger.debug("Last computation was successful, continuing old stream");
                        this.createResultComponent();
                    } else {
                        this.logger.debug("Last computation failed, re-displaying error message");
                        this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                                ExplanationEventType.ERROR));
                    }
                }
            }
        }
    }

    protected void redisplayCachedExplanation() {
        this.resetResultComponent();
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    abstract protected void createNewExplanation();

    abstract protected void prepareResultComponentCreation();

    private void createSettingsComponent(){
        SwingUtilities.invokeLater(() -> {
            this.settingsHolderPanel = new JPanel();
            this.settingsHolderPanel.setLayout(new BoxLayout(settingsHolderPanel, BoxLayout.PAGE_AXIS));
            JPanel spinnerHelperPanel = new JPanel();
            spinnerHelperPanel.setLayout(new BoxLayout(spinnerHelperPanel, BoxLayout.LINE_AXIS));
            JLabel label = UIUtilities.createLabel(SETTINGS_LABEL);
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

    protected void resetResultComponent(){
        this.logger.debug("Resetting result component");
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
        this.resultHolderPanel.repaint();
        this.resultHolderPanel.revalidate();
    }

    protected void resetEditOntologyStatus(){
        this.logger.debug("Resetting edit ontology status");
        this.ignoreOntologyChangeEvent = false;
        this.savedCache = null;
        this.addButtonList.clear();
        this.addAndProveButtonList.clear();
        this.deleteButtonList.clear();
    }

    protected void resetAbductionParameters(){
        this.lastUsedObservation = null;
        this.lastUsedAbducibles = null;
    }

    /**
     * Compare two results based on the string representation. Shorter results should come first.
     */
    private final Comparator<Set<OWLAxiom>> resultComparator = (result1, result2) -> {
        if(result1.size()!=result2.size())
            return result1.size()-result2.size();
        else {
            String r1 = result1.stream().map(Object::toString).reduce("", (a,b) -> a+b);
            String r2 = result2.stream().map(Object::toString).reduce("", (a,b) -> a+b);
            if(r1.length()!=r2.length())
                return r1.length()-r2.length();
            else
                return r1.compareTo(r2);
        }
    };

    protected void createResultComponent(){
        SwingUtilities.invokeLater(() -> {
            int resultNumber = (int) this.abductionNumberSpinner.getValue();
            this.logger.debug("Trying to show {} results of abduction generation process", resultNumber);
            List<Set<OWLAxiom>> hypotheses = new ArrayList<>();
            Stream<Set<OWLAxiom>> resultStream = this.generateExplanations();
            resultStream.limit(resultNumber).forEach(result -> {
                if (result != null){
                    hypotheses.add(result);}
            });
            this.logger.debug("Actually showing {} results of abduction generation process", hypotheses.size());
            hypotheses.stream().sorted(resultComparator).forEach(result -> {
                JPanel singleResultPanel = new JPanel();
                singleResultPanel.setLayout(new BorderLayout());
                singleResultPanel.setBorder(new CompoundBorder(
                        new EmptyBorder(5, 5, 5, 5),
                        new CompoundBorder(new LineBorder(Color.BLACK, 1),
                                new EmptyBorder(5, 5, 5, 5))));
                JPanel labelAndButtonPanel = new JPanel();
                labelAndButtonPanel.setLayout(new BoxLayout(labelAndButtonPanel, BoxLayout.LINE_AXIS));
                JLabel label = new JLabel("Hypothesis " + (this.hypothesisIndex+1));
                OWLObjectListModel<OWLAxiom> resultListModel = new OWLObjectListModel<>();
//                todo: in order to resolve white-space bug, add elements to model only AFTER setting preferred size of scrollPane, but this is only part of the solution
                resultListModel.addElements(result);
                JList<OWLAxiom> resultList = new JList<>(resultListModel);
                labelAndButtonPanel.add(label);
                labelAndButtonPanel.add(Box.createHorizontalGlue());
                JButton addToOntologyButton = UIUtilities.createNamedButton(
                        ADD_TO_ONTO_COMMAND, ADD_TO_ONTO_NAME, ADD_TO_ONTO_TOOLTIP,
                        new EditOntologyButtonListener(
                        this.ontology, resultList, this.hypothesisIndex));
                labelAndButtonPanel.add(addToOntologyButton);
                this.addButtonList.add(addToOntologyButton);
                labelAndButtonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                JButton addAndProveButton = UIUtilities.createNamedButton(
                        ADD_TO_ONTO_AND_PROVE_COMMAND, ADD_TO_ONTO_AND_PROVE_NAME,
                        ADD_TO_ONTO_AND_PROVE_TOOLTIP, new EditOntologyButtonListener(
                                this.ontology, resultList, this.hypothesisIndex));
                labelAndButtonPanel.add(addAndProveButton);
                this.addAndProveButtonList.add(addAndProveButton);
                labelAndButtonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                JButton deleteFromOntologyButton = UIUtilities.createNamedButton(
                        DELETE_FROM_ONTO_COMMAND, DELETE_FROM_ONTO_NAME,
                        DELETE_FROM_ONTO_TOOLTIP, new EditOntologyButtonListener(
                                this.ontology, resultList, this.hypothesisIndex));
                deleteFromOntologyButton.setEnabled(false);
                labelAndButtonPanel.add(deleteFromOntologyButton);
                this.deleteButtonList.add(deleteFromOntologyButton);
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
                this.hypothesisIndex++;
            });
            this.resultHolderPanel.repaint();
            this.resultHolderPanel.revalidate();
//            event-handling needs to happen within invokeLater-Block
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        });
    }

    private void changeButtonStatus(int clickedButtonIndex, boolean addButtonClicked){
        boolean addButtonEnabled;
        boolean deleteButtonEnabled;
        if (addButtonClicked){
            addButtonEnabled = false;
            deleteButtonEnabled = true;
        } else{
            addButtonEnabled = true;
            deleteButtonEnabled = false;
        }
        for (int index = 0; index < this.deleteButtonList.size(); index++){
            if (index == clickedButtonIndex){
                this.deleteButtonList.get(index).setEnabled(deleteButtonEnabled);
            }
            this.addButtonList.get(index).setEnabled(addButtonEnabled);
            this.addAndProveButtonList.get(index).setEnabled(addButtonEnabled);
        }
    }

//    todo: move loadingUI to Lethe- and Capi-Solver (not every abduction solver might need a loading screen)
    public void disposeLoadingScreen(){
        if (this.loadingUI != null) {
            this.loadingUI.disposeLoadingScreen();
        }
    }

    protected boolean parametersChanged(){
        return (! this.abducibles.equals(this.lastUsedAbducibles)) ||
                (! this.observation.equals(this.lastUsedObservation));
    }

    protected boolean activeOntologyEdited(){
        return this.activeOntologyEditedExternally || this.activeOntologyEditedByAbductionSolver;
    }

    protected void setActiveOntologyEditedExternally(boolean edited){
        this.activeOntologyEditedExternally = edited;
    }

    private class EditOntologyButtonListener implements ActionListener {

        private final OWLOntology ontology;
        private final JList<OWLAxiom> axiomList;
        private final int hypothesisIndex;
        private final Logger logger = LoggerFactory.getLogger(EditOntologyButtonListener.class);

        private EditOntologyButtonListener(OWLOntology ontology, JList<OWLAxiom> axiomList, int index){
            this.ontology = ontology;
            this.axiomList = axiomList;
            this.hypothesisIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()){
                case ADD_TO_ONTO_COMMAND:
                    ignoreOntologyChangeEvent = true;
                    activeOntologyEditedByAbductionSolver = true;
                    saveCache();
                    resetCache();
                    this.addToOntology();
                    changeButtonStatus(this.hypothesisIndex, true);
                    break;
                case ADD_TO_ONTO_AND_PROVE_COMMAND:
                    ignoreOntologyChangeEvent = true;
                    activeOntologyEditedByAbductionSolver = true;
                    saveCache();
                    resetCache();
                    this.addAndProve();
                    changeButtonStatus(this.hypothesisIndex, true);
                    break;
                case DELETE_FROM_ONTO_COMMAND:
                    ignoreOntologyChangeEvent = true;
                    activeOntologyEditedByAbductionSolver = false;
                    reinstateCache();
                    this.deleteFromOntology();
                    changeButtonStatus(this.hypothesisIndex, false);
                    break;
            }
        }

        private void addToOntology(){
            this.logger.debug("Adding axioms of hypothesis {} to ontology", this.hypothesisIndex+1);
            this.add();
            String msgString = "Added " + this.axiomList.getModel().getSize() + " axioms of hypothesis "
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
            this.logger.debug("Adding axioms of hypothesis {} to ontology and showing proof for observation",
                    this.hypothesisIndex+1);
            this.add();
            OWLAxiom observation = new ArrayList<>(lastUsedObservation).get(0);
            ExplanationManager explanationManager = owlEditorKit.getOWLModelManager().getExplanationManager();
            if (explanationManager.hasExplanation(observation)) {
                JFrame frame = ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace());
                explanationManager.handleExplain(frame, observation);
            }
        }

        private void add(){
            Set<OWLAxiom> axiomsToAdd = new HashSet<>();
            this.logger.debug("The following axioms have been added:");
            for (int index = 0; index < this.axiomList.getModel().getSize(); index++){
                axiomsToAdd.add(this.axiomList.getModel().getElementAt(index));
                this.logger.debug(this.axiomList.getModel().getElementAt(index).toString());
            }
            this.ontology.getOWLOntologyManager().addAxioms(this.ontology, axiomsToAdd);
            this.axiomList.clearSelection();
        }

        private void deleteFromOntology(){
            this.logger.debug("Deleting axioms of hypothesis {} from ontology", this.hypothesisIndex+1);
            this.logger.debug("The following axioms have been deleted:");
            Set<OWLAxiom> axiomsToDelete = new HashSet<>();
            for (int index = 0; index < this.axiomList.getModel().getSize(); index++){
                axiomsToDelete.add(this.axiomList.getModel().getElementAt(index));
                this.logger.debug(this.axiomList.getModel().getElementAt(index).toString());
            }
            this.ontology.getOWLOntologyManager().removeAxioms(this.ontology, axiomsToDelete);
            String msgString = "Removed " + this.axiomList.getModel().getSize() + " axioms of hypothesis "
                    + (this.hypothesisIndex+1) + " from the ontology.";
            reinstateCache();
            JOptionPane msgPane = new JOptionPane(msgString, JOptionPane.INFORMATION_MESSAGE);
            JDialog msgDialog = msgPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Removed from ontology");
            msgDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            msgDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
            msgDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            msgDialog.setVisible(true);
        }

    }

    private void saveCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Saving cache for ontology " + ontology.getOntologyID().getOntologyIRI()
                .or(IRI.create("")));
        this.savedCache = cachedResults.get(ontology);
    }

    private void reinstateCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Reinstating saved cache for ontology " + ontology.getOntologyID().getOntologyIRI()
                .or(IRI.create("")));
        this.cachedResults.put(ontology, this.savedCache);
    }

    protected void resetCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Resetting AbductionCache for ontology " + ontology.getOntologyID().getOntologyIRI()
                .or(IRI.create("")));
        AbductionCache<Result> newCache = new AbductionCache<>();
        this.cachedResults.put(ontology, newCache);
    }

    protected void resetCompleteCache(){
        this.logger.debug("Resetting complete cache");
        this.cachedResults.clear();
        this.resetCache();
    }

    private class AbductionSolverOntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        private final Logger logger = LoggerFactory.getLogger(AbductionSolverOntologyChangeListener.class);

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            this.logger.debug("Change to ontology detected");
            if (ignoreOntologyChangeEvent){
                this.logger.debug("Change made by AbductionSolver, event ignored");
                ignoreOntologyChangeEvent = false;
            } else{
                this.logger.debug("Change not made by AbductionSolver");
                setActiveOntologyEditedExternally(true);
                activeOntologyEditedByAbductionSolver = false;
                resetResultComponent();
                resetEditOntologyStatus();
                resetCache();
                viewComponentListener.handleEvent(new ExplanationEvent<>(
                        AbstractAbductionSolver.this,
                        ExplanationEventType.RESULT_RESET));
            }
        }

        @Override
        public void handleChange(OWLModelManagerChangeEvent owlModelManagerChangeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (owlModelManagerChangeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                        owlModelManagerChangeEvent.isType(EventType.ONTOLOGY_RELOADED)){
                    this.logger.debug("Change/Reload of active ontology detected");
                    activeOntologyChanged = true;
                    resetAbductionParameters();
                    resetResultComponent();
                    resetEditOntologyStatus();
                    viewComponentListener.handleEvent(new ExplanationEvent<>(
                            AbstractAbductionSolver.this, ExplanationEventType.RESULT_RESET));
                }
            });
        }
    }

}
