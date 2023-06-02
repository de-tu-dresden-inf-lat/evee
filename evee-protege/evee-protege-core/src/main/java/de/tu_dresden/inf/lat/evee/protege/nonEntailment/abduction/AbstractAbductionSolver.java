package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IAbductionSolverOntologyChangeEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IAbductionSolverResultButtonEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.*;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;

abstract public class AbstractAbductionSolver<Result>
        implements INonEntailmentExplanationService<OWLAxiom>,
        IOWLAbductionSolver,
        IAbductionSolverOntologyChangeEventListener,
        IAbductionSolverResultButtonEventListener {

    protected boolean canceled = false;
    protected Set<OWLAxiom> observation = null;
    protected Set<OWLAxiom> lastUsedObservation = null;
    protected Set<OWLEntity> abducibles = null;
    protected Set<OWLEntity> lastUsedAbducibles = null;
    protected OWLOntology ontology = null;
    protected JPanel settingsHolderPanel;
    private JSpinner abductionNumberSpinner;
    private OWLEditorKit owlEditorKit;
    private final AbductionSolverResultManager resultManager;
    private boolean computationSuccessful;
    private String errorMessage;
    private boolean activeOntologyEditedExternally = false;
    private boolean activeOntologyEditedByAbductionSolver = false;
    private boolean activeOntologyChanged = false;
    protected IProgressTracker progressTracker;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected final Map<OWLOntology, AbductionCache<Result>> cachedResults;
    private AbductionCache<Result> savedCache = null;
    protected static final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    protected static final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";

    private final Logger logger = LoggerFactory.getLogger(AbstractAbductionSolver.class);

    public AbstractAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.errorMessage = "";
        this.cachedResults = new HashMap<>();
        this.resultManager = new AbductionSolverResultManager(this, this);
        this.createSettingsComponent();
        this.logger.debug("AbstractAbductionSolver created successfully.");
    }

    private void createSettingsComponent(){
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
    }

    public void setComputationSuccessful(boolean successful){
        this.computationSuccessful = successful;
    }

    public boolean computationSuccessful(){
        return this.computationSuccessful;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * uses current activeOntology, observation and abducibles to save result
     * @param result newly computed result of abduction process that should be saved to cache
     */
    protected void saveResultToCache(Result result){
        if (this.ontology == null ||
                this.observation == null ||
                this.abducibles == null){
            return;
        }
        this.cachedResults.get(this.ontology).
                putResult(this.observation, this.abducibles, result);
    }

    /**
     * uses current activeOntology, observation and abductibles to load result
     * @return cached result of abduction process or null if no cached result was found
     */
    protected Result loadResultFromCache(){
        if (! this.cachedResults.get(this.ontology).containsResultFor(
                this.observation, this.abducibles)){
            return null;
        } else {
            return this.cachedResults.get(this.ontology).getResult(
                    this.observation, this.abducibles);
        }
    }

    @Override
    public void initialise(){
        this.logger.debug("Initialising AbductionSolver");
        this.resetCache();
        this.resultManager.initialise();
        this.logger.debug("AbductionSolver initialised");
    }

    @Override
    public void dispose() {
        this.logger.debug("Disposing AbductionSolver");
        this.resultManager.dispose();
        this.logger.debug("AbductionSolver disposed");
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker){
        this.progressTracker = tracker;
    }

    @Override
    public boolean successful(){
        return ! this.canceled;
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
    public void setup(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        this.resultManager.setup(owlEditorKit);
    }

    @Override
    public Component getResult() {
        return this.resultManager.getResultComponent();
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
                this.resetSavedCache();
                this.resultManager.resetResultComponent();
                this.createNewExplanation();
            }
            this.activeOntologyChanged = false;
            this.resultManager.resetHypothesisIndex();
        }
        else{
            this.logger.debug("Abduction parameters unchanged");
            if (this.activeOntologyChanged){
                this.logger.debug("Active ontology changed since last request for computation");
                this.activeOntologyChanged = false;
                if (this.cachedResults.get(this.ontology).containsResultFor(
                        this.observation, this.abducibles)){
                    this.logger.debug("Cached result found, no computation of hypotheses necessary");
                    this.computationSuccessful = true;
                    this.redisplayCachedExplanation();
                } else{
                    this.logger.debug("No cached result found, computing new hypotheses");
                    this.resetSavedCache();
                    this.resultManager.resetResultComponent();
                    this.createNewExplanation();
                }
                this.resultManager.resetHypothesisIndex();
            } else{
                this.logger.debug("Active ontology not changed since last request for computation");
                if (this.activeOntologyEdited()){
                    this.logger.debug("Changes made to ontology since last computation, re-computation of hypotheses necessary");
                    this.lastUsedObservation = this.observation;
                    this.lastUsedAbducibles = this.abducibles;
                    this.resetSavedCache();
                    this.resultManager.resetResultComponent();
                    this.createNewExplanation();
                    this.resultManager.resetHypothesisIndex();
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
        this.resultManager.resetResultComponent();
        this.resetSavedCache();
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    abstract protected void createNewExplanation();

    abstract protected void prepareResultComponentCreation();

    protected boolean parametersChanged(){
        return (! this.abducibles.equals(this.lastUsedAbducibles)) ||
                (! this.observation.equals(this.lastUsedObservation));
    }

    protected boolean activeOntologyEdited(){
        this.logger.debug("externally: {} - internally: {}", this.activeOntologyEditedExternally, this.activeOntologyEditedByAbductionSolver);
        return this.activeOntologyEditedExternally || this.activeOntologyEditedByAbductionSolver;
    }

    protected void setActiveOntologyEditedExternally(boolean edited){
        this.activeOntologyEditedExternally = edited;
    }

    protected void setActiveOntologyEditedInternally(boolean edited){
        this.activeOntologyEditedByAbductionSolver = edited;
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

    public void handleEvent(AbductionSolverOntologyChangeEvent event){
        switch (event.getType()){
            case ONTOLOGY_EDITED:
                this.ontologyEdited();
                break;
            case ACTIVE_ONTOLOGY_CHANGED:
                this.activeOntologyChanged();
                break;
        }
    }

    private void ontologyEdited(){
        this.setActiveOntologyEditedExternally(true);
        this.setActiveOntologyEditedInternally(false);
        this.resetSavedCache();
        this.resetCache();
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this,ExplanationEventType.RESULT_RESET));
    }

    private void activeOntologyChanged(){
        this.activeOntologyChanged = true;
        this.setActiveOntologyEditedExternally(false);
        this.setActiveOntologyEditedInternally(false);
        this.resetAbductionParameters();
        this.resetSavedCache();
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this, ExplanationEventType.RESULT_RESET));
    }

    public void handleEvent(AbductionSolverResultButtonEvent event){
        switch (event.getType()){
            case ADD:
                this.setActiveOntologyEditedInternally(true);
                this.saveCache();
                this.resetCache();
                break;
            case ADD_AND_PROVE:
                this.setActiveOntologyEditedInternally(true);
                this.saveCache();
                this.resetCache();
                break;
            case DELETE:
                this.setActiveOntologyEditedInternally(false);
                this.reinstateCache();
                break;
        }
    }

//    protected void resetResultComponent(){
//        this.logger.debug("Resetting result component");
//        this.resultHolderPanel = new JPanel(new BorderLayout());
//        this.resultScrollingPanel = new JPanel();
//        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
//        JScrollPane resultScrollPane = new JScrollPane();
//        resultScrollPane.setViewportView(this.resultScrollingPanel);
//        this.resultHolderPanel.add(resultScrollPane);
//        this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createTitledBorder(
//                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
//                        "Hypotheses:"),
//                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//        this.resultHolderPanel.repaint();
//        this.resultHolderPanel.revalidate();
//    }

    protected void resetSavedCache(){
        this.logger.debug("Resetting edit ontology status");
        this.savedCache = null;
    }

    protected void resetAbductionParameters(){
        this.lastUsedObservation = null;
        this.lastUsedAbducibles = null;
    }

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
            this.resultManager.createResultComponent(ontology,
                    this.lastUsedObservation, hypotheses);

//            event-handling needs to happen within invokeLater-Block
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        });
    }

}
