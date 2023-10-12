package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.tools.OntologyFilter;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Stefan Borgwardt
 * @author Christian Alrabbaa
 *
 */

public class ELKRelevantCounterexampleGenerator implements IOWLCounterexampleGenerator {

    private ModelType type;
    private Set<OWLAxiom> observation;
    private Collection<OWLEntity> signature;
    private OWLOntology ontology;
    private Set<IRI> markedIndividuals;

    private boolean modelTypeReverted = false;
    private IProgressTracker progressTracker;

    public boolean isModelTypeReverted() {
        return modelTypeReverted;
    }

    private final OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private static final String ELEMENT_PREFIX = "http://www.example.org/generated-model/";
    private OWLIndividual owlIndividual(String name) {
        return factory.getOWLNamedIndividual(IRI.create(ELEMENT_PREFIX + name));
    }

    public ELKRelevantCounterexampleGenerator(ModelType type) {
        this.type = type;
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
    }

    @Override
    public void setSignature(Collection<OWLEntity> signature) {
        this.signature = signature;
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.ontology = OntologyFilter.filterEL(ontology);
    }

    @Override
    public Set<OWLIndividualAxiom> generateModel() {
        try {
            progressTracker.setMessage("Generating counterexample");
            progressTracker.increment();
            OWLSubClassOfAxiom subClsOf;
            OWLAxiom axiom = this.observation.iterator().next();
            if (axiom instanceof OWLSubClassOfAxiom)
                subClsOf = (OWLSubClassOfAxiom) axiom;
            else
                throw new ModelGenerationException("Observation is not an instance of SubClassOf");

            ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
            ElkReasoner reasoner = reasonerFactory.createReasoner(this.ontology);

            //in case the super class is unsatisfiable, the model type reverts to alpha
            if(!reasoner.isSatisfiable(subClsOf.getSuperClass())){
                modelTypeReverted = type != ModelType.Alpha;
                type = ModelType.Alpha;
            }else
                modelTypeReverted = false;

            ELKModelGenerator elkModelGenerator = new ELKModelGenerator(ontology, subClsOf);
            RelevantCounterExampleGenerator generator = getRelevantGenerator(type, elkModelGenerator);

            Set<Element> model = generator.generate();
            progressTracker.setMessage("Filtering the generated counterexample.");
            progressTracker.increment();

            markedIndividuals = new HashSet<>();
            markedIndividuals.add(IRI.create(ELEMENT_PREFIX + elkModelGenerator.getMapper().getLHSRepresentativeElement().getName()));
            if (type != ModelType.Alpha)
                markedIndividuals.add(IRI.create(ELEMENT_PREFIX + elkModelGenerator.getMapper().getRHSRepresentativeElement().getName()));

            //This is needed to remove normalisation concepts and other redundant elements
            RedundancyRefiner rr = new RedundancyRefiner(model, generator);
            rr.refine();

            Set<OWLIndividualAxiom> owlModel = new HashSet<>();
            for (Element element : model) {
                OWLIndividual individual = owlIndividual(element.getName());
                for (OWLClassExpression expr : element.getTypes()) {
                    if (signature.containsAll(expr.getSignature())) {
                        owlModel.add(factory.getOWLClassAssertionAxiom(expr, individual));
                    }
                }
                for (Relation relation : element.getRelations()) {
                    OWLIndividual other = owlIndividual(relation.getElement2().getName());
                    OWLObjectProperty property = relation.getRoleName();
                    if (signature.contains(property)) {
                        if (relation.isForward()) {
                            owlModel.add(factory.getOWLObjectPropertyAssertionAxiom(property, individual, other));
                        }
                        //Not needed! Every backward relation there is a forward one for the other element in that
                        // relation
//                        if (relation.isBackward()) {
//                            owlModel.add(factory.getOWLObjectPropertyAssertionAxiom(property, individual, other));
//                        }
                    }
                }
            }
            return owlModel;
        } catch (OWLOntologyCreationException ex) {
            ex.printStackTrace();
            return null;
        } catch (ModelGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a new relevant counter example generator based on the provided model type.<br>
     * {@link ModelType#Beta} is the default in case the type is not recognised
     * @param type
     * @param elkModelGenerator
     * @return
     * @throws OWLOntologyCreationException
     */
    private RelevantCounterExampleGenerator getRelevantGenerator(ModelType type, ELKModelGenerator elkModelGenerator)
            throws OWLOntologyCreationException {
        switch (type) {
            case Alpha: return new AlphaRelevantGenerator(elkModelGenerator);
            case Diff: return new DiffRelevantGenerator(elkModelGenerator);
            case FlatDiff: return new FlatDiffRelevantGenerator(elkModelGenerator);
            default: return new BetaRelevantGenerator(elkModelGenerator);
        }
    }

    @Override
    public Set<IRI> getMarkedIndividuals() {
        return markedIndividuals;
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return Stream.of(generateModel());
    }

    @Override
    public boolean supportsExplanation() {
        if (observation.size() != 1) {
            return false;
        }
        OWLAxiom axiom = observation.stream().findFirst().get();
        if (!(axiom instanceof OWLSubClassOfAxiom)) {
            return false;
        }
        OWLSubClassOfAxiom sco = (OWLSubClassOfAxiom) axiom;
        // do not use "isNamed" - it does not seem to work with the owlapi5 version
        return !sco.getSubClass().isAnonymous() && !sco.getSuperClass().isAnonymous();
    }

    @Override
    public boolean successful() {
        return true;
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker) {
        this.progressTracker = tracker;
    };
}
