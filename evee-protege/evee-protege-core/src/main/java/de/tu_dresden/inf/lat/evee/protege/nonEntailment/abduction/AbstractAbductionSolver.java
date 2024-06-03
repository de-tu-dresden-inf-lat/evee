package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverOntologyChangeEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverSingleResultPanelEventListener;
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
        ISignatureModificationEventListener,
        ISignatureModificationEventGenerator,
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
    private OWLEditorKit owlEditorKit;
    private final AbductionSolverResultManager resultManager;
    private boolean cancelled;
    private boolean activeOntologyEditedExternally = false;
    private boolean activeOntologyEditedByAbductionSolver = false;
    private boolean activeOntologyChanged = false;
    private IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    private final Map<OWLOntology, AbductionCache<Result>> cachedResults;
//    private AbductionCache<Result> savedCache = null;
    private ISignatureModificationEventListener signatureModificationEventListener;
    private final AbductionGeneralPreferencesManager preferencesManager;

    private final Logger logger = LoggerFactory.getLogger(AbstractAbductionSolver.class);

    public AbstractAbductionSolver(){
        this.logger.debug("Creating AbstractAbductionSolver");
        this.cancelled = false;
        this.cachedResults = new HashMap<>();
        this.resultManager = new AbductionSolverResultManager();
        this.resultManager.registerOntologyChangeEventListener(this);
//        this.resultManager.registerSingleResultPanelEventListener(this);
        this.resultManager.registerSignatureModificationEventListener(this);
        this.preferencesManager = new AbductionGeneralPreferencesManager();
        this.logger.debug("AbstractAbductionSolver created successfully.");
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
        return null;
    }

    @Override
    public void registerListener(
            IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
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
//        this.resetSavedCache();
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

    protected boolean parametersChanged(){
        return (! this.vocabulary.equals(this.lastUsedVocabulary)) ||
                (! this.missingEntailment.equals(this.lastUsedMissingEntailment));
    }

    protected boolean checkActiveOntologyEdited(){
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

//    protected void saveCache(){
//        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
//        this.logger.debug("Saving cache for ontology " + ontology.getOntologyID().getOntologyIRI()
//                .or(IRI.create("")));
//        this.savedCache = this.cachedResults.get(ontology);
//    }
//
//    protected void reinstateCache(){
//        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
//        this.logger.debug("Reinstating saved cache for ontology " + ontology.getOntologyID().getOntologyIRI()
//                .or(IRI.create("")));
//        this.cachedResults.put(ontology, this.savedCache);
//    }

    protected void resetCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Resetting AbductionCache for ontology " + ontology.getOntologyID().getOntologyIRI()
                .or(IRI.create("")));
        AbductionCache<Result> newCache = new AbductionCache<>();
        this.cachedResults.put(ontology, newCache);
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
//        this.resetSavedCache();
        this.resetCache();
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this,ExplanationEventType.RESULT_RESET));
    }

    private void activeOntologyChanged(){
        this.setActiveOntologyChanged(true);
        this.setActiveOntologyEditedExternally(false);
        this.setActiveOntologyEditedInternally(false);
        this.resetAbductionParameters();
//        this.resetSavedCache();
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this, ExplanationEventType.RESULT_RESET));
    }

//    @Override
//    public void handleEvent(AbductionSolverSingleResultPanelEvent event){
//        switch (event.getType()){
//            case ADD:
//                this.setActiveOntologyEditedInternally(true);
//                this.resetCache();
//                break;
//            case EXPLAIN:
//                this.setActiveOntologyEditedInternally(true);
//                this.saveCache();
//                this.resetCache();
//                break;
//            case EXPLANATION_DIALOG_CLOSED:
//                this.setActiveOntologyEditedInternally(false);
//                this.reinstateCache();
//                break;
//        }
//    }

//    protected void resetSavedCache(){
//        this.logger.debug("Resetting edit ontology status");
//        this.savedCache = null;
//    }

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
        int resultNumber = this.preferencesManager.loadMaximumHypothesisNumber();
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

    @Override
    public void handleSignatureModificationEvent(SignatureModificationEvent event) {
        Set<OWLEntity> additionalSignatureElements = new HashSet<>();
        event.getAdditionalSignatureNames()
                .stream().filter(name -> this.activeOntology.getSignature().contains(name))
                .forEach(additionalSignatureElements::add);
        this.signatureModificationEventListener.handleSignatureModificationEvent(
                new SignatureModificationEvent(additionalSignatureElements));
    }

    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener){
        this.signatureModificationEventListener = listener;
    }

    @Override
    public void repaintResultComponent() {
        this.resultManager.repaintResultComponent();
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                ExplanationEventType.COMPUTATION_COMPLETE));
    }

}
