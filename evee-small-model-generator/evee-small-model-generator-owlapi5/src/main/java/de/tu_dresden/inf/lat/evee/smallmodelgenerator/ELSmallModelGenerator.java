package de.tu_dresden.inf.lat.evee.smallmodelgenerator;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
/**
 * ELSmallModelGenerator is a class responsible for generating small models for the Description Logic EL
 * ontologies and can perform subsumption checks. To reduce the number of individual names in the model,
 * the generator uses the existing individual names when creating successors for individual names belonging to existential restrictions.
 * This class ignores axioms more expressive than those supported by EL
 */
public class ELSmallModelGenerator implements IOWLModelGenerator {
    private final Logger logger = LoggerFactory.getLogger(ELSmallModelGenerator.class);
    private final OWLReasonerFactory rf= new ReasonerFactory();
    private final OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private final OWLDataFactory df= man.getOWLDataFactory();
    private Set<OWLIndividualAxiom> model = new HashSet<>();
    private Set<OWLIndividualAxiom> avoidedEntailments = new HashSet<>();
    private Map<OWLNamedIndividual, Set<OWLClassExpression>> indClassExprMap;
    private boolean avoidedEntHolds;
    private OWLOntology ont;
    public int indNum;
    private boolean treeModel;
    private int maxDepth = 10;
    private Map<OWLNamedIndividual, Integer> depthMap = new HashMap<>();
    private boolean consistent;
    private OWLReasoner res;
    private boolean makesChange;
    private boolean SubsumptionRuleMakesChange;

    public void avoidEntailment(OWLIndividualAxiom entailment) {
        avoidedEntailments.add(entailment);
    }

    public Set<OWLIndividualAxiom> getAvoidedEntailments() {
        return avoidedEntailments;
    }

    public ELSmallModelGenerator(boolean treeModel) {
        this.treeModel = treeModel;

    }

    public ELSmallModelGenerator(boolean treeModel, Set<OWLIndividualAxiom> avoidedEntailments) {
        this.avoidedEntailments = avoidedEntailments;
        this.treeModel = treeModel;
    }

    public ELSmallModelGenerator(boolean treeModel,
                                 Set<OWLIndividualAxiom> avoidedEntailments,
                                 int maxDepth) {
        this.maxDepth = maxDepth;
        this.avoidedEntailments = avoidedEntailments;
        this.treeModel = treeModel;
    }


    public boolean isAvoidedEntHolds() {
        return avoidedEntHolds;
    }

    public boolean isConsistent() {
        return consistent;
    }



    public void setOntology(OWLOntology ont) {
        Set<OWLAxiom> Axioms = ont.getAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        OWLOntology ontology = null;
        try {
            ontology = man.createOntology(Axioms);
        } catch (OWLOntologyCreationException e1) {
            e1.printStackTrace();
        }
        this.ont = ontology;
        initializeDepthMap();
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

    @Override
    public Set<IRI> getMarkedIndividuals() {
        return new HashSet<>();
    }

    private void reset() {
        this.consistent = true;
        this.indNum = 0;
        this.makesChange = true;
        this.avoidedEntHolds = false;
        this.indClassExprMap = new HashMap<>();
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
            if (!consistent || avoidedEntHolds) {
                break;
            }
            applyUnrollExRestrictionRule();
        }
        indClassExprMap.entrySet().stream().forEach(e ->
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

        if(depthMap.get(passedInd)>= maxDepth) {
            return false;
        }
        int curDepth = depthMap.get(passedInd);
        OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) expr;
        boolean hasSuccessor = this.ont.getObjectPropertyAssertionAxioms(passedInd).stream()
                .filter(ax -> ax.getProperty().equals(obj.getProperty()))
                .anyMatch(ax -> this.indClassExprMap.get(ax.getObject()).containsAll(obj.getFiller().asConjunctSet()));

        if (!hasSuccessor) {
            Set<OWLAxiom> toAdd = new HashSet<>();
            if (!this.treeModel) {
                for (OWLNamedIndividual ind : this.ont.getIndividualsInSignature()) {
                    OWLAxiom clAs = this.df.getOWLClassAssertionAxiom(obj.getFiller(), ind);
                    OWLAxiom prAs = this.df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, ind);
                    if (!this.ont.containsAxiom(clAs)) {
                        toAdd.add(clAs);
                    }
                    if (!this.ont.containsAxiom(prAs)) {
                        toAdd.add(prAs);
                    }
                    man.addAxioms(ont, toAdd);
                    res.flush();
                    boolean isConsisted = res.isConsistent();
                    boolean avoidedEntHolds = false;
                    if(isConsisted) {
                        for(OWLIndividualAxiom ent:avoidedEntailments) {
                            if (res.isEntailed(ent)) {
                                avoidedEntHolds = true;
                            }
                        }
                    }
                    if (isConsisted && !avoidedEntHolds) {
                        hasSuccessor = true;
                        break;
                    } else {
                        man.removeAxioms(ont, toAdd);
                        toAdd.removeAll(toAdd);
                    }
                }
            }
            if (!hasSuccessor) {
                OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("Ind-" + indNum));
                depthMap.put(a, curDepth+1);
                man.addAxiom(this.ont, df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, a));
                man.addAxiom(this.ont, df.getOWLClassAssertionAxiom(obj.getFiller(), a));
                indNum = indNum + 1;
                hasSuccessor = true;
            }
        } else {
            hasSuccessor = false;
        }
        return hasSuccessor;
    }

    private void applyUnrollExRestrictionRule() {
        this.makesChange = indClassExprMap.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .filter(expr -> expr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        .anyMatch(expr -> findSuccessor(expr, entry.getKey()))
                );
    }

    private void applyAddExRestrictionRule() {
        logger.debug("addExRestrictionRule is applied");
        ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
                .forEach(ax -> {
                    Set<OWLClassExpression> toAdd = new HashSet<>();
                    indClassExprMap.get(ax.getObject()).stream()
                            .filter(expr -> expr.getClassExpressionType() != ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                            .forEach(expr -> toAdd.add(df.getOWLObjectSomeValuesFrom(ax.getProperty(), expr)));
                    indClassExprMap.merge(ax.getSubject().asOWLNamedIndividual(), toAdd, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
                });
    }

    private void applyConjunctionRule() {
        ont.getAxioms(AxiomType.CLASS_ASSERTION).forEach(ax -> {
            indClassExprMap.merge((OWLNamedIndividual) ax.getIndividual(), ax.getClassExpression().asConjunctSet(), (a, b) -> {
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
                        indClassExprMap.entrySet().stream()
                                .filter(entry -> entry.getValue().containsAll(ax.getSubClass().asConjunctSet()))
                                .filter(entry -> !entry.getValue().contains(ax.getSuperClass()))
                                .forEach(entry -> {
                                    entry.getValue().add(ax.getSuperClass());
                                    if (ax.getSuperClass().isBottomEntity()) {
                                        consistent = false;
                                    }
//                                    if (entry.getKey().equals(rootInd) && ax.getSuperClass().equals(targetConcept)) {
//                                        avoidedEntHolds = true;
//                                    }
                                    SubsumptionRuleMakesChange = true;
                                    makesChange = true;
                                });
                    });
        }
    }
    private void initializeDepthMap() {
        ont.getIndividualsInSignature().forEach(ind -> depthMap.put(ind,0));
    }

}
