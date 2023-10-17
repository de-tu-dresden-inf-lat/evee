package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelView;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.GraphStyleSheets;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphViewService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IInteractiveComponent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

abstract public class AbstractCounterexampleGenerationService
        implements INonEntailmentExplanationService<OWLIndividualAxiom> {

    protected String supportsExplanationMessage;
    protected OWLEditorKit owlEditorKit;
    protected SwingWorker worker;
    protected String errorMessage;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected OWLOntology workingCopy;
    protected IInteractiveComponent interactiveGraphModel;
    protected IOWLCounterexampleGenerator counterexampleGenerator;
    protected OWLOntologyManager man;
    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);
    private IProgressTracker progressTracker;

    public AbstractCounterexampleGenerationService() {
        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.supportsExplanationMessage = "Please enter some observation containing a single OWLSubClassOfAxiom";
        this.man = OWLManager.createOWLOntologyManager();
    }

    public void computeExplanation() {
        worker = new InteractiveModelGenerationSwingWorker(this);
        worker.execute();
    }

    public Component getResult() {
        return interactiveGraphModel.toComponent();
//        return resultComponent;
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
        ((IOWLNonEntailmentExplainer) counterexampleGenerator).setOntology(workingCopy);
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
    public Component getSettingsComponent() {
        return null;
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
        this.progressTracker = tracker;
    };

    @Override
    public void cancel() {
        logger.info("cancellation of computation is called");
        this.worker.cancel(true);
    }

    private class InteractiveModelGenerationSwingWorker extends SwingWorker<Void, Void> {

        private boolean computationSuccessful = false;
        private INonEntailmentExplanationService<OWLIndividualAxiom> service;
        public InteractiveModelGenerationSwingWorker(INonEntailmentExplanationService<OWLIndividualAxiom> service) {
           this.service = service;
        }
        @Override
        protected Void doInBackground() throws Exception {
            computationSuccessful = false;

            try {
                IGraphViewService graphViewGenerator = new GraphViewGenerator(GraphStyleSheets.PROTEGE,2000);
                OWLSubClassOfAxiom observationAxiom = (OWLSubClassOfAxiom) observation.stream().findFirst().get();
                interactiveGraphModel = new InteractiveGraphModel<>(counterexampleGenerator,
                        graphViewGenerator,
                        workingCopy,
                        observationAxiom,
                        owlEditorKit);
                computationSuccessful = true;
            } catch (Exception e) {
                logger.error("model generation error",e);
                errorMessage = "model generation error";
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
}
