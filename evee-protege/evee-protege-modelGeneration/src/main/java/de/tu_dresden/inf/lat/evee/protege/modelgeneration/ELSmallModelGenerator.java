package de.tu_dresden.inf.lat.evee.protege.modelgeneration;


import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ELSmallModelGenerator implements IOWLModelGenerator {
    public Map<OWLNamedIndividual, Set<OWLClassExpression>> map;
    public OWLOntology ont;
    private OWLDataFactory df;
    private boolean consistent;
    private OWLReasoner res;
    public int curName;
    private boolean makeChange;
    private boolean T1makeChange;
    private OWLReasonerFactory rf;
    private OWLClassExpression B;
    private OWLNamedIndividual a;
    private boolean subsumed;
    private OWLOntologyManager man;
    public Set<OWLAxiom> TBoxAxioms;
    public Set<OWLAxiom> ABoxAxioms;
    private Map<String,List<OWLClass>> indClassMapData;
    private int numRemoved;
    public boolean bigModel;
    private Set<OWLAxiom> model;




    public ELSmallModelGenerator() {
        this.rf = new ReasonerFactory();
        this.man = OWLManager.createOWLOntologyManager();
        this.df = man.getOWLDataFactory();
        this.a = df.getOWLNamedIndividual(IRI.create("root-Ind"));
        this.B = df.getOWLClass(IRI.create("FreshClass"));
        model = new HashSet<>();

    }

    public boolean getSubsumed() {
        return subsumed;
    }
    public boolean getConsistent() {
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
    public Set<OWLAxiom> generateModel()  {
        res = rf.createReasoner(ont);
        reset();
        getModel();
        return model;
    }
    private void reset () {
        this.consistent = true;
        this.curName = 0;
        this.makeChange = true;
        this.subsumed = false;
        this.map = new HashMap<>();
    }





    public void checkSubsumption(OWLClassExpression C,OWLClassExpression D, boolean bigModel) {
        this.reset();
        this.bigModel = bigModel;
        OWLClassAssertionAxiom axiom1 = df.getOWLClassAssertionAxiom(C, a);
        OWLSubClassOfAxiom axiom2 = df.getOWLSubClassOfAxiom(D, B);
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

        try {this.ont = normaliser.normalise();}
        catch (OWLOntologyCreationException e){// TODO Auto-generated catch block
            e.printStackTrace();}
        man.addAxiom(ont, axiom1);
        this.numRemoved = normaliser.getNumRemoved();
        this.res = rf.createReasoner(ont);
        this.getModel();

    }

    private void getModel() {
        while(this.makeChange ) {
            this.makeChange = false;
            this.A1();
            this.A2();
            this.T1();
            boolean buf = this.makeChange;
            while(this.makeChange) {
                this.makeChange = false;
                this.A2();
                this.T1();
            }
            makeChange = buf;
            if(!this.consistent) {
                break;
            }
            if (this.subsumed) {
                break;
            }
            A3();
        }
        map.entrySet().stream().forEach(e ->
                e.getValue().stream()
                        .filter(cl -> cl.isClassExpressionLiteral())
                        .map(cl -> cl.asOWLClass())
                        .forEach(cl -> model.add(df.getOWLClassAssertionAxiom(cl,e.getKey()))));
        model.addAll(ont.getABoxAxioms(Imports.INCLUDED).stream()
                .filter(ax-> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .collect(Collectors.toSet()));
    }

    private boolean findSuccessor(OWLClassExpression expr,OWLNamedIndividual passedInd) {
        OWLObjectSomeValuesFrom obj = (OWLObjectSomeValuesFrom) expr;
        boolean hasSuccessor = this.ont.getObjectPropertyAssertionAxioms(passedInd).stream()
                .filter(ax -> ax.getProperty().equals(obj.getProperty()))
                .anyMatch(ax -> this.map.get(ax.getObject()).containsAll(obj.getFiller().asConjunctSet()));

        if (!hasSuccessor) {
            Set<OWLAxiom> toAdd = new HashSet<>();
            if(!this.bigModel) {
                for (OWLNamedIndividual ind : this.ont.getIndividualsInSignature()) {
                    OWLAxiom clAs = this.df.getOWLClassAssertionAxiom(obj.getFiller(), ind);
                    OWLAxiom prAs = this.df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, ind);
                    if(this.ont.containsAxiom(clAs)==false) {
                        toAdd.add(clAs);
                    }
                    if(this.ont.containsAxiom(prAs)==false) {
                        toAdd.add(prAs);
                    }
                    this.man.addAxioms(ont, toAdd);
                    this.res.flush();
                    if (res.isConsistent() && !res.isEntailed(df.getOWLClassAssertionAxiom(B, a))) {

                        hasSuccessor = true;
                        break;
                    } else {

                        man.removeAxioms(ont, toAdd);
                        toAdd.removeAll(toAdd);
                    }
                }
            }
            if (!hasSuccessor) {

                OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("Ind-"+ curName));

                man.addAxiom(this.ont,df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, a));
                man.addAxiom(this.ont,df.getOWLClassAssertionAxiom(obj.getFiller(), a));


                curName = curName+1;
                hasSuccessor = true;
            }
        }
        else {
            hasSuccessor = false;
        }
        return hasSuccessor;
    }

    private void A3(){
        this.makeChange =  map.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .filter(expr -> expr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        .anyMatch(expr -> findSuccessor(expr, entry.getKey()))
                );
    }

    private void A2() {
        ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
                .forEach(ax -> {Set<OWLClassExpression> toAdd = new HashSet<>();
                    map.get(ax.getObject()).stream()
                            .filter(expr -> expr.getClassExpressionType() != ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                            .forEach(expr -> toAdd.add(df.getOWLObjectSomeValuesFrom((OWLObjectProperty) ax.getProperty(),expr)));
                    map.merge(ax.getSubject().asOWLNamedIndividual(), toAdd, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
                });
    }

    private void A1() {
        ont.getAxioms(AxiomType.CLASS_ASSERTION).forEach(ax -> {
            map.merge((OWLNamedIndividual) ax.getIndividual(), ax.getClassExpression().asConjunctSet(), (a, b) -> {
                a.addAll(b);
                return a;
            });
        });
    }

    private void T1() {
        T1makeChange = true;
        while (T1makeChange) {
            T1makeChange = false;
            ont.getAxioms(AxiomType.SUBCLASS_OF)
                    .forEach(ax -> {
                        map.entrySet().stream()
                                .filter(entry -> entry.getValue().containsAll(ax.getSubClass().asConjunctSet()))
                                .filter(entry -> !entry.getValue().contains(ax.getSuperClass()))
                                .forEach(entry -> {entry.getValue().add(ax.getSuperClass());
                                    if(ax.getSuperClass().isBottomEntity()) {
                                        consistent=false;
                                    }
                                    if(entry.getKey().equals(a) && ax.getSuperClass().equals(B)) {
                                        subsumed=true;
                                    }

                                    T1makeChange = true;
                                    makeChange = true;
                                });
                    });
        }
    }


}
