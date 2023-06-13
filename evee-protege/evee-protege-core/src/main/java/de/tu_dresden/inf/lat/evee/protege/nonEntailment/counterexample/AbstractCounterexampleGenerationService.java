package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;

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
    protected String errorMessage;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected OWLOntology workingCopy;
    protected Set<OWLIndividualAxiom> model;
    protected IOWLCounterexampleGenerator counterexampleGenerator;
    protected OWLOntologyManager man;
    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);
    public AbstractCounterexampleGenerationService() {
        this.observation = new HashSet<>();
        this.errorMessage = "";
        this.supportsExplanationMessage = "Please enter some observation containing a single OWLSubClassOfAxiom";
        this.man = OWLManager.createOWLOntologyManager();
    }

//    protected JTabbedPane getTabbedPane() {
//
//        ModelManager man = new ModelManager(this.model, this.owlEditorKit, this, this.workingCopy);
//        Component graphComponent = man.getGraphModel();
//        Component tableComponent = man.getTableModel();
//        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.setPreferredSize(new Dimension(400, 400));
//        tabbedPane.addTab("Graph View", graphComponent);
//        tabbedPane.addTab("Table View", tableComponent);
//        return tabbedPane;
//    }

    public void computeExplanation() {
        try {
            checkSubsumption((OWLSubClassOfAxiom) observation.stream().findFirst().get());
//            ((IOWLNonEntailmentExplainer) counterexampleGenerator).setOntology(workingCopy);
            model = counterexampleGenerator.generateModel();
            logger.debug("model is generated:"+model);
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            logger.info(sStackTrace);
            this.errorMessage = e.getMessage();
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.ERROR));
        }
    }

    public Component getResult() {
        ModelManager man = new ModelManager(this.model, this.owlEditorKit, this.counterexampleGenerator, this.workingCopy, this.observation);
        return man.generateGraphModel();
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
}
