package de.tu_dresden.inf.lat.evee.protege.counterexample.EL;

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
//    boolean subsumed;
    private OWLClassExpression subClassExpr;
    private OWLClassExpression superClassExpr;
//    private int removedAxioms = 0;
    private OWLOntology activeOntology;
    private OWLOntology workingCopy;
    private final IRI root = IRI.create("root-Ind");
    private Set<OWLAxiom> observation;
    private final OWLDataFactory df;
    private final OWLOntologyManager man;
    private Collection<OWLEntity> signature;

    public ELCounterexampleGenerator() {
        this.observation = new HashSet<>();
        this.man = OWLManager.createOWLOntologyManager();
        this.df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    }

    private OWLOntology getWorkingCopy() throws ModelGenerationException {
        OWLNamedIndividual rootInd = df.getOWLNamedIndividual(root);
        OWLClass freshClass = df.getOWLClass(IRI.create("FreshClass"));
        OWLClassAssertionAxiom axiom1 = df.getOWLClassAssertionAxiom(subClassExpr, rootInd);
        OWLSubClassOfAxiom axiom2 = df.getOWLSubClassOfAxiom(superClassExpr, freshClass);
        Set<OWLAxiom> TBoxAxioms = activeOntology.getTBoxAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        OWLOntology workingCopy = null;
        ELNormaliser normaliser = new ELNormaliser();
        try {
            workingCopy = man.createOntology(TBoxAxioms);
            man.addAxiom(workingCopy, axiom2);
            normaliser.setOntology(workingCopy);
            workingCopy = normaliser.normalise();
        } catch (OWLOntologyCreationException e) {
            throw new ModelGenerationException("Working Copy cannot be created");
        }
        man.addAxiom(workingCopy, axiom1);
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
        checkClassExpressions();
        workingCopy = getWorkingCopy();
        IOWLModelGenerator modelGenerator = new ELSmallModelGenerator();
        modelGenerator.setOntology(workingCopy);
        Set<OWLIndividualAxiom> model = modelGenerator.generateModel();
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
        markedIndividuals.add(root);
        return markedIndividuals;
    }

    @Override
    public boolean successful() {
        return true;
    }

    @Override
    public boolean ignoresPartsOfOntology() {
        return false;
    }
}
