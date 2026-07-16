package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.model.IRI;

import static org.semanticweb.owlapi.model.parameters.OntologyCopy.DEEP;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.SubsumptionHoldsException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.MappingUtils;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.ReasoningUtils;

public class CounterexampleModel {
    private final Logger logger = Logger.getLogger(CounterexampleModel.class);
 
    private OWLEditorKit owlEditorKit;
    private OWLOntology ontology;
    private OWLOntologyManager man;

    private IOWLCounterexampleGenerator modelGenerator;

    private Set<OWLAxiom> observation;
    private OWLSubClassOfAxiom currentObservationAxiom;
    private Set<OWLIndividualAxiom> model;

    private boolean requiresSubsumptionCheck = true;
    
    public CounterexampleModel() {}

    public CounterexampleModel(OWLEditorKit editorKit, OWLOntology ontology, Set<OWLAxiom> observation, IOWLCounterexampleGenerator modelGenerator) {
        this.owlEditorKit = editorKit;
        this.ontology = ontology;
        this.observation = observation;
        this.modelGenerator = modelGenerator;
        ((IOWLNonEntailmentExplainer<OWLIndividualAxiom>)modelGenerator).setOntology(ontology);
    }

    public boolean supportsExplanation() {
        return modelGenerator.supportsExplanation();
    }

    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return modelGenerator.generateExplanations();
    }
    
    public void addProgressTracker(IProgressTracker tracker) {
        this.modelGenerator.addProgressTracker(tracker); //TODO
    }

    public void setSignature(Collection<OWLEntity> signature) {
        this.modelGenerator.setSignature(signature);
        this.model = null;
    }

    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
        this.modelGenerator.setObservation(observation);
        this.currentObservationAxiom = null;
        this.model = null;
    }
    
    public void setCounterexampleGenerator(IOWLCounterexampleGenerator counterexampleGenerator) {
        this.modelGenerator = counterexampleGenerator;
        this.model = null;
    }

    //the ontology may be modified, so is copied to keep ownership coherent
    public void setOntology(OWLOntology ontology) { //TODO exception handling
        try {
            this.ontology = man.copyOntology(ontology, DEEP);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        ((IOWLNonEntailmentExplainer<OWLIndividualAxiom>)modelGenerator).setOntology(this.ontology);
        this.model = null;
    }

    public void setOWLEditorKit(OWLEditorKit kit){
        this.owlEditorKit = kit;
    }

    /**
     * Get the computed model. 
     * Make sure computeModel() is called at least once or check for returned null.
     *
     * @return Returns current model. May return null, it's not checked wether a model is present.
     *  
     */
    public Set<OWLIndividualAxiom> getModel(){
        return model;
    }

    public Set<IRI> getMarkedInds() {
        return modelGenerator.getMarkedIndividuals();
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLEditorKit getOWLEditorKit(){
        return this.owlEditorKit;
    }

    public void computeModel()
            throws SubsumptionHoldsException, ModelGenerationException, InconsistentOntologyException {
        
        if(ontology == null) 
            throw new ModelGenerationException("ontology not set");
        if (observation == null) 
            throw new ModelGenerationException("observation not set");
        if (modelGenerator == null) 
            throw new ModelGenerationException("counterexample generator not set");     
        
        Optional<OWLAxiom> opt = observation.stream().findFirst();
        if (!opt.isPresent()) {
            throw new ModelGenerationException("empty observation provided");
        }
        if(!(opt.get() instanceof OWLSubClassOfAxiom)) {
            throw new ModelGenerationException("invalid observation: not an OWLSubClassOfAxiom");

        }
        currentObservationAxiom = (OWLSubClassOfAxiom) opt.get();
      

        if(!ReasoningUtils.isConsistent(ontology, currentObservationAxiom))
            throw new InconsistentOntologyException();
        
        if(requiresSubsumptionCheck) {
            if(ReasoningUtils.subsumptionHolds(ontology, currentObservationAxiom)) {
                throw new SubsumptionHoldsException();
            }
        }

        model = modelGenerator.generateModel();
        logger.info("Model is computed");
    }


    public void recomputeModel(Set<OWLAxiom> additionalAxioms)
            throws ModelGenerationException, InconsistentOntologyException {

        Set<OWLSubClassOfAxiom> subClassOfAxioms = MappingUtils.disjToSubclassOfAx(additionalAxioms);
        ontology.getOWLOntologyManager().addAxioms(ontology, subClassOfAxioms);

        try {
            if(!ReasoningUtils.isConsistent(ontology, currentObservationAxiom)) {
                throw new InconsistentOntologyException();
            }
            model = modelGenerator.generateModel();
            ontology.getOWLOntologyManager().removeAxioms(ontology, subClassOfAxioms);
        } catch ( InconsistentOntologyException | ModelGenerationException e) {
            ontology.getOWLOntologyManager().removeAxioms(ontology, subClassOfAxioms);
            throw  e;
        }
        logger.info("Model is recomputed");
    }

    public void addToOntology(Set<OWLAxiom> additionalAxioms){
        ontology.getOWLOntologyManager().addAxioms(ontology, additionalAxioms);
        owlEditorKit.getModelManager().getOWLOntologyManager().addAxioms(
                owlEditorKit.getModelManager().getActiveOntology(), 
                additionalAxioms);
    }

}
