package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

import com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.smallmodelgenerator.*;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ELCounterexampleGenerator implements IOWLCounterexampleGenerator {

    private final Logger logger = LoggerFactory.getLogger(ELCounterexampleGenerator.class);
    private final IRI ROOT_IRI = IRI.create("root-Ind");
    private OWLClassExpression subClassExpr;
    private IProgressTracker progressTracker;
    private OWLClassExpression superClassExpr;
    private OWLOntology activeOntology;
    private OWLOntology workingCopy;

    private Set<OWLAxiom> observation= new HashSet<>();
    private final OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    private final OWLNamedIndividual rootInd = df.getOWLNamedIndividual(ROOT_IRI);
    private final OWLClass freshClass = df.getOWLClass(IRI.create("FreshClass"));
    private final OWLOntologyManager man =OWLManager.createOWLOntologyManager();
    private Collection<OWLEntity> signature;
    private boolean treeModel = false;
    private OWLClassAssertionAxiom rootInSubClass;
    private OWLSubClassOfAxiom superClassInTarget;

    public ELCounterexampleGenerator() {}

    public ELCounterexampleGenerator(boolean treeModel) {
        this.treeModel = treeModel;
    }

    private OWLOntology getWorkingCopy() throws ModelGenerationException {

        Set<OWLAxiom> TBoxAxioms = activeOntology.getTBoxAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        OWLOntology workingCopy = null;
        ELNormaliser normaliser = new ELNormaliser();
        try {
            workingCopy = man.createOntology(TBoxAxioms);
            man.addAxiom(workingCopy, superClassInTarget);
            normaliser.setOntology(workingCopy);
            workingCopy = normaliser.normalise();
        } catch (OWLOntologyCreationException e) {
            throw new ModelGenerationException("Working Copy cannot be created");
        }
        man.addAxiom(workingCopy, rootInSubClass);
//        this.removedAxioms = normaliser.getNumRemoved();
        return workingCopy;
    }

    void checkClassExpressions() throws ModelGenerationException {

        if (!(this.isELExpressions(subClassExpr) && this.isELExpressions(superClassExpr))) {
            throw new ModelGenerationException("ClassExpressions are not in EL");
        }
    }

    private boolean isELExpressions(OWLClassExpression expression) {

        Set<OWLClassExpression> nestedClassExpressions = expression.getNestedClassExpressions();
        for (OWLClassExpression exp : nestedClassExpressions) {
            if (!(exp instanceof OWLObjectSomeValuesFrom
                    || exp instanceof OWLObjectIntersectionOf
                    || exp instanceof OWLClass)) {
                return false;
            }
        }
        return true;
    }

    private Set<OWLIndividualAxiom> filterAxioms(Set<OWLIndividualAxiom> axioms) {
        Set<OWLIndividualAxiom> filtered = axioms.stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom) ax)
                .filter(ax -> this.signature.containsAll(ax.getClassExpression().getSignature()))
//                .filter(ax -> !(ax.getClassExpression().toString().startsWith("<X")
//                        || ax.getClassExpression().toString().equals("<FreshClass>")))
                .collect(Collectors.toSet());
        filtered.addAll(axioms.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                        .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                        .filter(ax -> signature.contains(ax.getProperty()))
                .collect(Collectors.toSet()));
        return filtered;
    }

    @Override
    public Set<OWLIndividualAxiom> generateModel() throws ModelGenerationException {
        OWLAxiom observation = this.observation.iterator().next();
        subClassExpr = ((OWLSubClassOfAxiom) observation).getSubClass();
        superClassExpr = ((OWLSubClassOfAxiom) observation).getSuperClass();
        rootInSubClass = df.getOWLClassAssertionAxiom(subClassExpr, rootInd);
        superClassInTarget = df.getOWLSubClassOfAxiom(superClassExpr, freshClass);

        checkClassExpressions();
        logger.debug("provided observation is checked");

        workingCopy = getWorkingCopy();
        logger.debug("working copy is created");

        if(progressTracker != null) {
            progressTracker.setMessage("Generating counterexample");
            progressTracker.increment();
        }
        Set<OWLIndividualAxiom> avoidedEnt = new HashSet<>();
        avoidedEnt.add(df.getOWLClassAssertionAxiom(freshClass, rootInd));
        IOWLModelGenerator modelGenerator = new ELSmallModelGenerator(treeModel,avoidedEnt);
        modelGenerator.setOntology(workingCopy);
        Set<OWLIndividualAxiom> model = modelGenerator.generateModel();
        logger.info("model is generated");
        model = this.filterAxioms(model);
        return model;
    }

    @Override
    public void setObservation(Set<OWLAxiom> axioms) {
        this.observation = axioms;

    }

    @Override
    public void setSignature(Collection<OWLEntity> signature) {
        this.signature = signature;
    }

    @Override
    public void setOntology(OWLOntology owlOntology) {
        this.activeOntology = owlOntology;
    }

    @Override
    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        try {
            return Stream.of(generateModel());
        } catch (ModelGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsExplanation() {
        return this.observation.size() == 1 && this.observation.stream()
                .anyMatch((ax) -> ax.isOfType(AxiomType.SUBCLASS_OF));
    }


    @Override
    public Set<IRI> getMarkedIndividuals() {
        Set<IRI> markedIndividuals = new HashSet<>();
        markedIndividuals.add(ROOT_IRI);
        return markedIndividuals;
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
    public boolean ignoresPartsOfOntology() {
        return false;
    }
}
