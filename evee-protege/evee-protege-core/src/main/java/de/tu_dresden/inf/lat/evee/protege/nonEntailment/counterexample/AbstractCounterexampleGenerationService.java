package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences.NonEntailmentGeneralPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.GraphStyleSheets;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IPreferencesChangeListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphViewService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IInteractiveComponent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEventType;

import org.apache.log4j.Logger;
import javax.swing.SwingWorker;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

abstract public class AbstractCounterexampleGenerationService
        implements INonEntailmentExplanationService<OWLIndividualAxiom>,
        IPreferencesChangeListener {

    protected String supportsExplanationMessage;
    protected OWLEditorKit owlEditorKit;
    protected SwingWorker<Void, Void> generationTask;
    protected String errorMessage;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected OWLOntology workingCopy;
    protected boolean simpleMode;
    protected IInteractiveComponent interactiveGraphModel;
    protected IOWLCounterexampleGenerator counterexampleGenerator;
    protected OWLOntologyManager man;
    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);
    protected IProgressTracker progressTracker;
    protected boolean computationSuccessful = false;
    protected final NonEntailmentGeneralPreferencesManager preferencesManager = NonEntailmentGeneralPreferencesManager.getInstance();

    public AbstractCounterexampleGenerationService() {
        logger.warn("in constructor"); //debugLog
        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.supportsExplanationMessage = "Please enter some observation containing a single OWLSubClassOfAxiom";
        this.man = OWLManager.createOWLOntologyManager();
        this.preferencesManager.registerPreferencesChangeEventListener(this);
        this.simpleMode = preferencesManager.loadUseSimpleMode();
        logger.warn("finished constructor"); //debugLog
    }

    public void computeExplanation() {
        logger.warn("computeExplanation is called");
        generationTask = new InteractiveModelGenerationWorker(this);
        logger.warn("starting generation task thread");
        generationTask.execute();
    }


    public Component getResult() {
        if (interactiveGraphModel == null) {
            return null;
        }

        return interactiveGraphModel.toComponent();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return supportsExplanationMessage;
    }

    public boolean supportsExplanation() {
        return this.counterexampleGenerator.supportsExplanation();
    }

    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            this.workingCopy = man.copyOntology(activeOntology,DEEP);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        ((IOWLNonEntailmentExplainer<OWLIndividualAxiom>) counterexampleGenerator).setOntology(workingCopy);
    }

    @Override
    public void handlePreferenceChange(GeneralPreferencesChangeEvent event) {
        if(event.isType(GeneralPreferencesChangeEventType.SIMPLE_MODE_CHANGE)) {
            simpleMode = preferencesManager.loadUseSimpleMode();
        }

    }
    @Override
    public void setObservation(Set<OWLAxiom> owlAxioms) {
        this.observation = owlAxioms;
        this.counterexampleGenerator.setObservation(observation);
    }
    @Override
    public void setSignature(Collection<OWLEntity> signature) {
        this.counterexampleGenerator.setSignature(signature);
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void registerListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return counterexampleGenerator.generateExplanations();
    }

    @Override
    public void initialise() throws Exception {}

    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

    public void dispose() throws Exception {}

    protected void setCounterexampleGenerator(IOWLCounterexampleGenerator counterexampleGenerator) {
        this.counterexampleGenerator = counterexampleGenerator;
    }

    protected void setSupportsExplanationMessage(String supportsExplanationMessage) {
        this.supportsExplanationMessage = supportsExplanationMessage;
    }

    @Override
    public boolean successful() {
        return true;
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker) {
        this.counterexampleGenerator.addProgressTracker(tracker);
        this.progressTracker = tracker;
    }

    @Override
    public void cancel() {
        logger.info("cancellation of computation is called");
        computationSuccessful = true;
        generationTask.cancel(true);
        
    }

    private class InteractiveModelGenerationWorker extends SwingWorker<Void, Void> {

        private final INonEntailmentExplanationService<OWLIndividualAxiom> service;
       private final Logger loggerThread = Logger.getLogger(InteractiveModelGenerationWorker.class);

        public InteractiveModelGenerationWorker(INonEntailmentExplanationService<OWLIndividualAxiom> service) {
           this.service = service;
        }

        @Override
        protected Void doInBackground() throws Exception {
            loggerThread.warn("in call");
            logger.warn("model generation task started"); //debugLog
            computationSuccessful = false;
            try {                
                logger.warn("line 175"); //debugLog
                IGraphViewService graphViewGenerator = new GraphViewGenerator(GraphStyleSheets.PROTEGE,2000);
                logger.warn("line 177"); //debugLog
                OWLSubClassOfAxiom observationAxiom = (OWLSubClassOfAxiom) observation.stream().findFirst().get();
                logger.warn("line 179"); //debugLog
                interactiveGraphModel = new InteractiveGraphModel(counterexampleGenerator,
                        graphViewGenerator,
                        workingCopy,
                        observationAxiom,
                        owlEditorKit,
                        viewComponentListener,
                        simpleMode
                    );
                
                logger.warn("model generation task finished"); //debugLog
                computationSuccessful = true;

            } catch (Throwable e) {
                if (computationSuccessful) {
                    logger.info("Counterexample generation is canceled");
                } else {
                    logger.error("Model generation error", e);
                    errorMessage = "Model generation error:\n" + e.getMessage();
                }
            }

            return null;
        }

        @Override
        protected void done() {
            if (computationSuccessful) {
                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service,
                        ExplanationEventType.COMPUTATION_COMPLETE));
            } else {
                logger.info("error event started.");
                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service,
                        ExplanationEventType.ERROR));
            }
        }
  
     }

    @Override
    public String getFilterWarningMessage() {
        return null;
    }

    @Override
    public boolean ignoresPartsOfOntology() {
        return false;
    }

    @Override
    public void repaintResultComponent() {

    }
}
