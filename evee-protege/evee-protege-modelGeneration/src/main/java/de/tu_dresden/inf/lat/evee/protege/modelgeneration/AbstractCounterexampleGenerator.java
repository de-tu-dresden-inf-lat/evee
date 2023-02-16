package de.tu_dresden.inf.lat.evee.protege.modelgeneration;


import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterExampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

abstract public class AbstractCounterexampleGenerator implements INonEntailmentExplanationService<OWLAxiom>, IOWLCounterExampleGenerator {

    protected OWLEditorKit owlEditorKit;
    protected String errorMessage ;
    protected IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    protected Set<OWLAxiom> observation;
    protected OWLOntology activeOntology;
    protected Set<OWLAxiom> model;
    protected IOWLModelGenerator modelGenerator;
    protected String supportsExplanationMessage = "Please enter some signature and observation";


    protected JTabbedPane getTabbedPane() {

        ModelRepresentationManager man = new ModelRepresentationManager(model,owlEditorKit,activeOntology);
        Component graphComponent = man.getGraphModel();
        Component tableComponent = man.getTableModel();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(400, 400));
        tabbedPane.addTab("Graph View",graphComponent);
        tabbedPane.addTab("Table View",tableComponent);
        return tabbedPane;
    }
    public void computeExplanation() {
        try {

            modelGenerator = new ELSmallModelGenerator();
            modelGenerator.setOntology(activeOntology);
            model = modelGenerator.generateModel();
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.COMPUTATION_COMPLETE));
        } catch (Exception e) {
            this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                    ExplanationEventType.ERROR));
        }
    }

    public Component getResult() {
        return getTabbedPane();
    }
    @Override
    public String getSupportsExplanationMessage() {
        return supportsExplanationMessage;
    }
    public boolean supportsExplanation() {
        return true;
    }
    @Override
    public Set<OWLAxiom> generateModel() {
        return modelGenerator.generateModel();
    }

    public void setOntology(OWLOntology ontology)  {
        this.activeOntology = ontology;
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
    }
    @Override

    public void setObservation(Set<OWLAxiom> owlAxioms) {
        this.observation = owlAxioms;
    }
    @Override
    public Stream generateExplanation() {
        return Stream.of(generateModel());
    }

    @Override

    public void initialise() throws Exception {}
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
    }
    public void dispose() throws Exception {}


}
