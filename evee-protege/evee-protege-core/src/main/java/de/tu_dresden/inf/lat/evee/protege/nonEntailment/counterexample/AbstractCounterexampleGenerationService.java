package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;


import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
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
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

abstract public class AbstractCounterexampleGenerationService implements INonEntailmentExplanationService<OWLIndividualAxiom> {

    protected OWLEditorKit owlEditorKit;
    protected String errorMessage;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected OWLOntology workingCopy;
    protected Set<OWLIndividualAxiom> model;
    protected IOWLCounterexampleGenerator counterexampleGenerator;

    protected OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private final Logger logger = Logger.getLogger(AbstractCounterexampleGenerationService.class);


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
            ((IOWLNonEntailmentExplainer) counterexampleGenerator).setOntology(workingCopy);
            counterexampleGenerator.setObservation(observation);
            model = counterexampleGenerator.generateModel();
            logger.debug(model);

            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        } catch (Exception e) {
            this.errorMessage = e.getMessage();
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.ERROR));
        }
    }

    public Component getResult() {
        ModelManager man = new ModelManager(this.model, this.owlEditorKit, this.counterexampleGenerator, this.workingCopy, this.observation);
        return man.getGraphModel();
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter some observation containing a single OWLSubClassOfAxiom";
    }

    public boolean supportsExplanation() {
        return this.observation.size() == 1 && this.observation.stream()
                .anyMatch((ax) -> ax.isOfType(AxiomType.SUBCLASS_OF));
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
    }

    @Override
    public Stream generateExplanations() {
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


}