package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences.NonEntailmentGeneralPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IPreferencesChangeListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEventType;
import javafx.application.Platform;

import org.apache.log4j.Logger;
import javax.swing.SwingWorker;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

import java.awt.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

abstract public class AbstractCounterexampleGenerationService
        implements INonEntailmentExplanationService<OWLIndividualAxiom>,
        IPreferencesChangeListener {

    // javafx runtime has to be initialized exactly once
    private static final Object FX_LOCK = new Object();
    private static AtomicBoolean INITIALIZED_FX = new AtomicBoolean(false);
    private static final Logger loggerStatic = Logger.getLogger(AbstractCounterexampleGenerationService.class);


    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);

    protected final NonEntailmentGeneralPreferencesManager preferencesManager = NonEntailmentGeneralPreferencesManager
            .getInstance();

    protected String supportsExplanationMessage = "Please enter some observation containing a single OWLSubClassOfAxiom";

    protected OWLOntology activeOntology; //TODO not needed here?

    protected SwingWorker<Void, Void> generationTask;

   // protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected IProgressTracker progressTracker;
    protected CounterexampleController controller;

    protected boolean simpleMode;
    protected boolean computationSuccessful = false;

    public AbstractCounterexampleGenerationService() {
        this.simpleMode = preferencesManager.loadUseSimpleMode();
        this.controller = new CounterexampleController(this, simpleMode);
    }

    public void setup(OWLEditorKit editorKit) {
        this.controller.setOWLEditorKit(editorKit);
        this.preferencesManager.registerPreferencesChangeEventListener(this);
    }

    public void computeExplanation() {
        controller.computeCounterexampleGraph();
    }

    public Component getResult() {
      return controller.getGraphComponent();
    }

    public boolean supportsExplanation() {
        return this.controller.supportsExplanation();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return supportsExplanationMessage;
    }

    @Override
    public void handlePreferenceChange(GeneralPreferencesChangeEvent event) {
        if (event.isType(GeneralPreferencesChangeEventType.SIMPLE_MODE_CHANGE)) {
            simpleMode = preferencesManager.loadUseSimpleMode();
            controller.setSimpleMode(simpleMode);
        }
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.controller.setObservation(observation);
    }
    @Override
    public void setSignature(Collection<OWLEntity> signature) {
        this.controller.setSignature(signature);
    }

    @Override
    public String getErrorMessage() {
        return controller.getErrorMessage();
    }

    @Override
    public void registerListener(
            IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        controller.setViewListener(listener);
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return controller.generateExplanations();
    }

    @Override
    public void initialise() throws Exception {
        System.setProperty("org.graphstream.ui", "javafx");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        initFx();
    }

    @Override
    public boolean successful() {
        return true;
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker) {
        this.controller.setProgressTracker(tracker);
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
        controller.dispose();
    }

    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        controller.setOntology(ontology);
    }

    protected void setCounterexampleGenerator(IOWLCounterexampleGenerator counterexampleGenerator) {
        this.controller.setCounterexampleGenerator(counterexampleGenerator);
    }

    protected void setSupportsExplanationMessage(String supportsExplanationMessage) {
        this.supportsExplanationMessage = supportsExplanationMessage;
    }

    private static void initFx() {
        synchronized (FX_LOCK){
            if (INITIALIZED_FX.compareAndSet(false, true)) {
                Platform.setImplicitExit(false);
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

 
}
