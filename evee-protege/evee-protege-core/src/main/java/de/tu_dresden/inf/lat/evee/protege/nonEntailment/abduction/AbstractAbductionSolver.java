package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
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
        IAbductionSolverResultButtonEventListener,
        IExplanationGenerationListener<
                ExplanationEvent<
                        IExplanationGenerator<
                                Stream<Set<OWLAxiom>>>>> {

    protected Set<OWLAxiom> missingEntailment = null;
    protected Set<OWLAxiom> lastUsedMissingEntailment = null;
    protected Set<OWLEntity> vocabulary = null;
    protected Set<OWLEntity> lastUsedVocabulary = null;
    protected OWLOntology activeOntology = null;
    private Iterator<Set<OWLAxiom>> resultStreamIterator = null;
    protected JPanel settingsHolderPanel;
    private JSpinner abductionNumberSpinner;
    private OWLEditorKit owlEditorKit;
    private final AbductionSolverResultManager resultManager;
    private boolean cancelled;
    private boolean activeOntologyEditedExternally = false;
    private boolean activeOntologyEditedByAbductionSolver = false;
    private boolean activeOntologyChanged = false;
    private IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    private final Map<OWLOntology, AbductionCache<Result>> cachedResults;
    private AbductionCache<Result> savedCache = null;
    protected static final String SETTINGS_LABEL = "Maximal number of hypotheses:";
    protected static final String SETTINGS_SPINNER_TOOLTIP = "Number of hypotheses to be generated in each computation step";

    private final Logger logger = LoggerFactory.getLogger(AbstractAbductionSolver.class);

    public AbstractAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.cancelled = false;
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

    /**
     * @return true iff the cache contains a result for the currently active ontology, missing entailment and vocabulary
     */
    protected boolean checkResultInCache(){
        return this.cachedResults.get(this.activeOntology)
                .containsResultFor(this.missingEntailment, this.vocabulary);
    }

    /**
     * uses current activeOntology, missing entailment and vocabulary to save result
     * @param result newly computed result of abduction process that should be saved to cache
     */
    protected void saveResultToCache(Result result){
        this.logger.debug("Trying to save result to cache");
        if (this.activeOntology == null ||
                this.missingEntailment == null ||
                this.vocabulary == null){
            this.logger.debug("Cannot save result to cache");
            return;
        }
        this.cachedResults.get(this.activeOntology).
                putResult(this.missingEntailment, this.vocabulary, result);
        this.logger.debug("Result saved to cache");
    }

    /**
     * uses current activeOntology, missing entailment and vocabulary to load result
     * @return cached result of abduction process or null if no cached result was found
     */
    protected Result loadResultFromCache(){
        this.logger.debug("Trying to load result from cache");
        if (! this.cachedResults.get(this.activeOntology).containsResultFor(
                this.missingEntailment, this.vocabulary)){
            this.logger.debug("No cached result found");
            return null;
        } else {
            this.logger.debug("Cached result loaded");
            return this.cachedResults.get(this.activeOntology).getResult(
                    this.missingEntailment, this.vocabulary);
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
    public void setObservation(Set<OWLAxiom> missingEntailment) {
        StringBuilder misEntString = new StringBuilder();
        missingEntailment.forEach(misEnt -> misEntString.append(misEnt).append("\n"));
        this.logger.debug("Setting missing entailment:\n{}", misEntString);
        this.missingEntailment = missingEntailment;
    }

    @Override
    public void setSignature(Collection<OWLEntity> vocabulary) {
        StringBuilder vocString = new StringBuilder();
        vocabulary.forEach(voc -> vocString.append(voc).append("\n"));
        this.logger.debug("Setting signature:\n{}", vocString);
        this.vocabulary = new HashSet<>(vocabulary);
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.logger.debug("Setting ontology:\n{}", ontology.getOntologyID());
        this.activeOntology = ontology;
        if (this.cachedResults.get(ontology) == null){
            this.logger.debug("No cache for ontology detected, creating new cache");
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
        assertNotNull(this.activeOntology);
        assertNotNull(this.missingEntailment);
        assertNotNull(this.vocabulary);
        SwingUtilities.invokeLater(() -> {
            if (this.parametersChanged() ||
                    this.checkActiveOntologyChanged() ||
                    this.checkActiveOntologyEdited()){
                this.logger.debug("Parameters or ontology changed/edited, requesting new stream");
                this.cancelled = false;
                this.computeNewExplanation();
            } else{
                this.logger.debug("Parameters unchanged");
                if (this.cancelled){
                    this.logger.debug("Previous computation was cancelled, requesting new stream");
                    this.cancelled = false;
                    this.computeNewExplanation();
                }
                else if (this.resultStreamIterator != null){
                    this.logger.debug("Previous computation was successful, continuing to display result");
                    this.createResultComponent();
                    this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                            ExplanationEventType.COMPUTATION_COMPLETE));
                } else{
                    this.logger.debug("Previous computation failed, re-displaying error-message");
                    this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.ERROR));
                }
            }
        });

    }

    private void computeNewExplanation(){
        this.lastUsedMissingEntailment = this.missingEntailment;
        this.lastUsedVocabulary = this.vocabulary;
        this.setActiveOntologyChanged(false);
        this.resetSavedCache();
        this.resetResultComponent();
        this.resultStreamIterator = null;
        AbductionSolverThread thread = new AbductionSolverThread(
                this, this);
        thread.start();
    }

    @Override
    public void handleEvent(ExplanationEvent<
            IExplanationGenerator<
                    Stream<Set<OWLAxiom>>>> event){
        if (event.getType().equals(ExplanationEventType.COMPUTATION_COMPLETE)){
            Stream<Set<OWLAxiom>> resultStream = event.getSource().getResult();
            if (resultStream != null){
                this.logger.debug("Stream of explanations is NOT null, computation was successful");
                this.resultStreamIterator = resultStream.iterator();
                this.createResultComponent();
                this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                        ExplanationEventType.COMPUTATION_COMPLETE));
            } else{
                this.logger.debug("Stream of explanations is null, computation was not successful");
                this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                        ExplanationEventType.ERROR));
            }
        }
    }

    protected void sendViewComponentEvent(ExplanationEventType type){
        this.logger.debug("Sending event of type {} to view component", type);
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this, type));
    }

    protected void resetResultComponent(){
        this.resultManager.resetResultComponent();
    }

    protected boolean parametersChanged(){
        return (! this.vocabulary.equals(this.lastUsedVocabulary)) ||
                (! this.missingEntailment.equals(this.lastUsedMissingEntailment));
    }

    protected boolean checkActiveOntologyEdited(){
//        this.logger.debug("externally: {} - internally: {}", this.activeOntologyEditedExternally, this.activeOntologyEditedByAbductionSolver);
        return this.activeOntologyEditedExternally || this.activeOntologyEditedByAbductionSolver;
    }

    protected boolean checkActiveOntologyChanged(){
        return this.activeOntologyChanged;
    }

    protected void setActiveOntologyEditedExternally(boolean edited){
        this.activeOntologyEditedExternally = edited;
    }

    protected void setActiveOntologyEditedInternally(boolean edited){
        this.activeOntologyEditedByAbductionSolver = edited;
    }

    protected void setActiveOntologyChanged(boolean changed){
        this.activeOntologyChanged = changed;
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
        this.setActiveOntologyChanged(true);
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

    protected void resetSavedCache(){
        this.logger.debug("Resetting edit ontology status");
        this.savedCache = null;
    }

    protected void resetAbductionParameters(){
        this.lastUsedMissingEntailment = null;
        this.lastUsedVocabulary = null;
    }

    protected void createResultComponent(){
        this.resultManager.createResultComponent(activeOntology,
                this.lastUsedMissingEntailment,
                this.createHypothesesListFromStream());
    }

    protected List<Set<OWLAxiom>> createHypothesesListFromStream(){
        int resultNumber = (int) this.abductionNumberSpinner.getValue();
        this.logger.debug("Trying to show {} results of abduction generation process", resultNumber);
        List<Set<OWLAxiom>> hypotheses = new ArrayList<>();
        int idx = 0;
        while (idx < resultNumber && this.resultStreamIterator.hasNext()){
            Set<OWLAxiom> nextSolution = this.resultStreamIterator.next();
            if (nextSolution != null){
                hypotheses.add(nextSolution);
            }
            idx += 1;
        }
        this.logger.debug("Actually showing {} results of abduction generation process", hypotheses.size());
        return hypotheses;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
