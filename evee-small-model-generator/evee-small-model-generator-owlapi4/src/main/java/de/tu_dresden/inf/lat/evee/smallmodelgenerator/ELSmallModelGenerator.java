package de.tu_dresden.inf.lat.evee.smallmodelgenerator;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import java.util.*;
import java.util.stream.Collectors;
/**
 * ELSmallModelGenerator is a class responsible for generating small models for the Description Logic EL
 * ontologies and can perform subsumption checks. To reduce the number of individual names in the model,
 * the generator uses the existing individual names when creating successors for individual names belonging to existential restrictions.
 * This class ignores axioms more expressive than those supported by EL
 */
public class ELSmallModelGenerator implements IOWLModelGenerator {
    private Map<OWLNamedIndividual, Set<OWLClassExpression>> map;
    private OWLOntology ont;
    public int curName;
    public Set<OWLAxiom> TBoxAxioms;
    private boolean canonicalModel;
    private final OWLDataFactory df;
    private boolean consistent;
    private OWLReasoner res;
    private boolean makesChange;
    private boolean SubsumptionRuleMakesChange;
    private final OWLReasonerFactory rf;
    private final OWLClassExpression targetConcept;
    private final OWLNamedIndividual rootInd;
    private boolean subsumed;
    private final OWLOntologyManager man;
    private int numRemoved;
    private final Set<OWLIndividualAxiom> model;


    public ELSmallModelGenerator() {
        this.rf = new ReasonerFactory();
        this.man = OWLManager.createOWLOntologyManager();
        this.df = man.getOWLDataFactory();
        this.rootInd = df.getOWLNamedIndividual(IRI.create("root-Ind"));
        this.targetConcept = df.getOWLClass(IRI.create("FreshClass"));
        model = new HashSet<>();

    }


    public ELSmallModelGenerator(boolean canonicalModel) {
        this.canonicalModel = canonicalModel;
        this.rf = new ReasonerFactory();
        this.man = OWLManager.createOWLOntologyManager();
        this.df = man.getOWLDataFactory();
        this.rootInd = df.getOWLNamedIndividual(IRI.create("root-Ind"));
        this.targetConcept = df.getOWLClass(IRI.create("FreshClass"));
        model = new HashSet<>();

    }
    public boolean isSubsumed() {
        return subsumed;
    }

    public boolean isConsistent() {
        return consistent;
    }


    public int getNumRemoved() {
        return numRemoved;
    }

    public void setOntology(OWLOntology ont) {
        Set<OWLAxiom> Axioms = ont.getAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        OWLOntology ontology = null;
        try {
            ontology = man.createOntology(Axioms);
        } catch (OWLOntologyCreationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.ont = ontology;
    }

    @Override
    public Set<IRI> getMarkedIndividuals() {
        Set<IRI> markedInds = new HashSet<>();
        markedInds.add(rootInd.getIRI());
        return markedInds;
    }

    @Override
    public Set<OWLIndividualAxiom> generateModel() throws ModelGenerationException {
        res = rf.createReasoner(ont);
        reset();
        if (!res.isConsistent()) {
            throw new ModelGenerationException("Inconsistent ontology");
        } else {
            getModel();
            return model;
        }
    }

    private void reset() {
        this.consistent = true;
        this.curName = 0;
        this.makesChange = true;
        this.subsumed = false;
        this.map = new HashMap<>();
    }


    public void checkSubsumption(OWLClassExpression C, OWLClassExpression D, boolean bigModel) {
        this.reset();
        this.canonicalModel = bigModel;
        OWLClassAssertionAxiom axiom1 = df.getOWLClassAssertionAxiom(C, rootInd);
        OWLSubClassOfAxiom axiom2 = df.getOWLSubClassOfAxiom(D, targetConcept);
        this.ont = null;
        try {
            ont = man.createOntology(this.TBoxAxioms);
        } catch (OWLOntologyCreationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        man.addAxiom(this.ont, axiom2);

        ELNormaliser normaliser = new ELNormaliser();
        normaliser.setOntology(this.ont);
        try {
            this.ont = normaliser.normalise();
        } catch (OWLOntologyCreationException e) {// TODO Auto-generated catch block
            e.printStackTrace();
        }
        man.addAxiom(ont, axiom1);
        this.numRemoved = normaliser.getNumRemoved();
        this.res = rf.createReasoner(ont);
        this.getModel();

    }

    private void getModel() {
        while (makesChange) {
            makesChange = false;
            applyConjunctionRule();
            applyAddExRestrictionRule();
            applySubsumptionRule();
            boolean buf = makesChange;
            while (makesChange) {
                makesChange = false;
                applyAddExRestrictionRule();
                applySubsumptionRule();
            }
            makesChange = buf;
            if (!consistent ||subsumed ) {
                break;
            }
            applyUnrollExRestrictionRule();
        }
        map.entrySet().stream().forEach(e ->
                e.getValue().stream()
                        .filter(cl -> cl.isClassExpressionLiteral())
                        .map(cl -> cl.asOWLClass())
                        .forEach(cl -> model.add(df.getOWLClassAssertionAxiom(cl, e.getKey()))));
        model.addAll(ont.getABoxAxioms(Imports.INCLUDED).stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                        .map(ax -> (OWLIndividualAxiom) ax)
                .collect(Collectors.toSet()));
    }

    private boolean findSuccessor(OWLClassExpression expr, OWLNamedIndividual passedInd) {
        OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) expr;
        boolean hasSuccessor = this.ont.getObjectPropertyAssertionAxioms(passedInd).stream()
                .filter(ax -> ax.getProperty().equals(obj.getProperty()))
                .anyMatch(ax -> this.map.get(ax.getObject()).containsAll(obj.getFiller().asConjunctSet()));

        if (!hasSuccessor) {
            Set<OWLAxiom> toAdd = new HashSet<>();
            if (!this.canonicalModel) {
                for (OWLNamedIndividual ind : this.ont.getIndividualsInSignature()) {
                    OWLAxiom clAs = this.df.getOWLClassAssertionAxiom(obj.getFiller(), ind);
                    OWLAxiom prAs = this.df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, ind);
                    if (!this.ont.containsAxiom(clAs)) {
                        toAdd.add(clAs);
                    }
                    if (!this.ont.containsAxiom(prAs)) {
                        toAdd.add(prAs);
                    }
                    this.man.addAxioms(ont, toAdd);
                    this.res.flush();
                    if (res.isConsistent() && !res.isEntailed(df.getOWLClassAssertionAxiom(targetConcept, rootInd))) {
                        hasSuccessor = true;
                        break;
                    } else {
                        man.removeAxioms(ont, toAdd);
                        toAdd.removeAll(toAdd);
                    }
                }
            }
            if (!hasSuccessor) {
                OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("Ind-" + curName));
                man.addAxiom(this.ont, df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, a));
                man.addAxiom(this.ont, df.getOWLClassAssertionAxiom(obj.getFiller(), a));
                curName = curName + 1;
                hasSuccessor = true;
            }
        } else {
            hasSuccessor = false;
        }
        return hasSuccessor;
    }

    private void applyUnrollExRestrictionRule() {
        this.makesChange = map.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .filter(expr -> expr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        .anyMatch(expr -> findSuccessor(expr, entry.getKey()))
                );
    }

    private void applyAddExRestrictionRule() {
        ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
                .forEach(ax -> {
                    Set<OWLClassExpression> toAdd = new HashSet<>();
                    map.get(ax.getObject()).stream()
                            .filter(expr -> expr.getClassExpressionType() != ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                            .forEach(expr -> toAdd.add(df.getOWLObjectSomeValuesFrom(ax.getProperty(), expr)));
                    map.merge(ax.getSubject().asOWLNamedIndividual(), toAdd, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
                });
    }

    private void applyConjunctionRule() {
        ont.getAxioms(AxiomType.CLASS_ASSERTION).forEach(ax -> {
            map.merge((OWLNamedIndividual) ax.getIndividual(), ax.getClassExpression().asConjunctSet(), (a, b) -> {
                a.addAll(b);
                return a;
            });
        });
    }

    private void applySubsumptionRule() {
        SubsumptionRuleMakesChange = true;
        while (SubsumptionRuleMakesChange) {
            SubsumptionRuleMakesChange = false;
            ont.getAxioms(AxiomType.SUBCLASS_OF)
                    .forEach(ax -> {
                        map.entrySet().stream()
                                .filter(entry -> entry.getValue().containsAll(ax.getSubClass().asConjunctSet()))
                                .filter(entry -> !entry.getValue().contains(ax.getSuperClass()))
                                .forEach(entry -> {
                                    entry.getValue().add(ax.getSuperClass());
                                    if (ax.getSuperClass().isBottomEntity()) {
                                        consistent = false;
                                    }
                                    if (entry.getKey().equals(rootInd) && ax.getSuperClass().equals(targetConcept)) {
                                        subsumed = true;
                                    }
                                    SubsumptionRuleMakesChange = true;
                                    makesChange = true;
                                });
                    });
        }
    }
}
