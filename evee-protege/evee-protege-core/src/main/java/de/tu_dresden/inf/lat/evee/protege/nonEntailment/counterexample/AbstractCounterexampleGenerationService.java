package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
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
import javafx.application.Platform;

import org.apache.log4j.Logger;
import javax.swing.SwingWorker;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.awt.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

abstract public class AbstractCounterexampleGenerationService
        implements INonEntailmentExplanationService<OWLIndividualAxiom>,
        IPreferencesChangeListener {

    // javafx runtime has to be initialized exactly once
    private static final Object FX_LOCK = new Object();
    private static AtomicBoolean INITIALIZED_FX = new AtomicBoolean(false);
    private final static Logger loggerStatic = Logger.getLogger(AbstractCounterexampleGenerationService.class);


    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);

    protected final NonEntailmentGeneralPreferencesManager preferencesManager = NonEntailmentGeneralPreferencesManager
            .getInstance();

    protected String supportsExplanationMessage = "Please enter some observation containing a single OWLSubClassOfAxiom";
    protected String errorMessage = "";

    protected OWLOntology activeOntology;
    protected OWLEditorKit owlEditorKit;
    protected OWLOntologyManager man;

    protected SwingWorker<Void, Void> generationTask;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected IInteractiveComponent interactiveGraphModel;
    protected IProgressTracker progressTracker;

    protected CounterexampleModel counterexampleModel;

    protected boolean simpleMode;
    protected boolean computationSuccessful = false;

    public AbstractCounterexampleGenerationService() {
        logger.warn("in constructor"); // debugLog

        this.man = OWLManager.createOWLOntologyManager();
        this.simpleMode = preferencesManager.loadUseSimpleMode();
        this.counterexampleModel = new CounterexampleModel();

        logger.warn("finished constructor"); // debugLog
    }

    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
        this.counterexampleModel.setOWLEditorKit(editorKit);
        this.preferencesManager.registerPreferencesChangeEventListener(this);
    }

    public void computeExplanation() {
        logger.warn("computeExplanation is called"); // debugLog
        generationTask = new InteractiveModelGenerationWorker(this);
        logger.warn("starting generation task thread"); // debugLog
        generationTask.execute();
    }

    public Component getResult() {
        if (interactiveGraphModel == null) {
            return null;
        }

        return interactiveGraphModel.toComponent();
    }

    public boolean supportsExplanation() {
        return this.counterexampleModel.supportsExplanation();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return supportsExplanationMessage;
    }

    @Override
    public void handlePreferenceChange(GeneralPreferencesChangeEvent event) {
        if (event.isType(GeneralPreferencesChangeEventType.SIMPLE_MODE_CHANGE)) {
            simpleMode = preferencesManager.loadUseSimpleMode();
        }
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.counterexampleModel.setObservation(observation);
    }

    @Override
    public void setSignature(Collection<OWLEntity> signature) {
        this.counterexampleModel.setSignature(signature);
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void registerListener(
            IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        this.viewComponentListener = listener;
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return counterexampleModel.generateExplanations();
    }

    @Override
    public void initialise() throws Exception {
        initFx();
    }

    @Override
    public boolean successful() {
        return true;
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker) {
        this.counterexampleModel.addProgressTracker(tracker);
        this.progressTracker = tracker;
    }

    @Override
    public void cancel() {
        logger.info("cancellation of computation is called");
        computationSuccessful = true;
        generationTask.cancel(true);
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

    public void dispose() throws Exception {
    }

    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            this.counterexampleModel.setOntology(man.copyOntology(activeOntology, DEEP));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setCounterexampleGenerator(IOWLCounterexampleGenerator counterexampleGenerator) {
        this.counterexampleModel.setCounterexampleGenerator(counterexampleGenerator);
    }

    protected void setSupportsExplanationMessage(String supportsExplanationMessage) {
        this.supportsExplanationMessage = supportsExplanationMessage;
    }

    private static void initFx() {
        synchronized (FX_LOCK){
            if (INITIALIZED_FX.compareAndSet(false, true)) {
                try {
                    Platform.startup(() -> {
                        loggerStatic.info("starting JavaFX runtime");
                    });

                } catch (Throwable e) {
                    e.printStackTrace();
                    loggerStatic.error("Failed to initialize JavaFX runtime", e);
                }
            } 
        }
    }

    private class InteractiveModelGenerationWorker extends SwingWorker<Void, Void> {

        private final INonEntailmentExplanationService<OWLIndividualAxiom> service;
        private final Logger loggerThread = Logger.getLogger(InteractiveModelGenerationWorker.class);

        private IGraphViewService graphViewGenerator;

        public InteractiveModelGenerationWorker(INonEntailmentExplanationService<OWLIndividualAxiom> service) {
            this.service = service;
        }

        @Override
        protected Void doInBackground() throws Exception {
            loggerThread.warn("model generation task background thread tarted"); //debugLog
            computationSuccessful = false;
            counterexampleModel.computeModel();
            graphViewGenerator = new GraphViewGenerator(GraphStyleSheets.PROTEGE, 2000);
            return null;
        }

        @Override
        protected void done() {
            try {
                interactiveGraphModel = new InteractiveGraphModel(
                        counterexampleModel,
                        graphViewGenerator,
                        viewComponentListener,
                        simpleMode);

                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service, ExplanationEventType.COMPUTATION_COMPLETE));

            } catch (Throwable e) {
                if (computationSuccessful) {
                    loggerThread.info("Counterexample generation is canceled");
                    return;
                }

                loggerThread.error("Model generation error", e);
                errorMessage = "Model generation error:\n" + e.getMessage();
                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service, ExplanationEventType.ERROR));    
            }
            
        }

    }
}
