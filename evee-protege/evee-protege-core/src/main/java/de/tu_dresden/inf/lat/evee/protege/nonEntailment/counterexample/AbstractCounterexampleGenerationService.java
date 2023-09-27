package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

abstract public class AbstractCounterexampleGenerationService implements INonEntailmentExplanationService<OWLIndividualAxiom> {
    protected String supportsExplanationMessage;
    protected OWLEditorKit owlEditorKit;
    protected ModelGenerationSwingWorker worker;
    protected String errorMessage;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected OWLOntology workingCopy;
    protected Set<OWLIndividualAxiom> model;
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
        worker = new ModelGenerationSwingWorker(this);

        worker.execute();

    }

    public Component getResult() {
        ModelManager man = new ModelManager(this.model, this.owlEditorKit, this.counterexampleGenerator, this.workingCopy, this.observation);
        man.refreshGraphModelComponent();
        return man.getGraphModelComponent();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return supportsExplanationMessage;
    }

    public boolean supportsExplanation() {
        return this.counterexampleGenerator.supportsExplanation();
    }

    private void checkSubsumption(OWLSubClassOfAxiom ax) throws Exception {
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner resoner = reasonerFactory.createReasoner(this.workingCopy);
        if (resoner.isEntailed(ax)) {
            throw new Exception(ax.getSubClass().asOWLClass().getIRI().getShortForm().toString()+
                    " is subsumed by "+
                    ax.getSuperClass().asOWLClass().getIRI().getShortForm()+
                    "!");
        }
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
    public void setSignature(Collection<OWLEntity> signature) {
        this.counterexampleGenerator.setSignature(signature);
    }

    @Override

    public void setObservation(Set<OWLAxiom> owlAxioms) {
        this.observation = owlAxioms;
        this.counterexampleGenerator.setObservation(observation);
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return counterexampleGenerator.generateExplanations();
    }

    @Override

    public void initialise() throws Exception {
    }

    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }

    public void dispose() throws Exception {
    }
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


    private class ModelGenerationSwingWorker extends SwingWorker<Void, Void> {
        private INonEntailmentExplanationService<OWLIndividualAxiom> service;
        private boolean computationSucessfull = false;
        public ModelGenerationSwingWorker(INonEntailmentExplanationService<OWLIndividualAxiom> service) {
           this.service = service;
        }

        @Override
        protected Void doInBackground()  {
            try {
                computationSucessfull = false;
                checkSubsumption((OWLSubClassOfAxiom) observation.stream().findFirst().get());
                progressTracker.setMax(4);
                counterexampleGenerator.addProgressTracker(progressTracker);
                logger.info("model generation is started");
                model = counterexampleGenerator.generateModel();
                logger.info("model is generated");
                this.computationSucessfull = true;
            } catch (Exception e) {
                logger.error("model generation error",e);
                errorMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void done() {
            if (computationSucessfull) {
                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service,
                        ExplanationEventType.COMPUTATION_COMPLETE));
            } else {
                viewComponentListener.handleEvent(new ExplanationEvent<>(this.service,
                        ExplanationEventType.ERROR));
            }
            progressTracker.done();
        }
    }
    @Override
    public void cancel() {
        logger.info("cancellation of computation is called");
        this.worker.cancel(true);
    }

}
