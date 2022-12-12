import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ELReasoner {
    public Map<OWLNamedIndividual, Set<OWLClassExpression>> map;
    public OWLOntology ont;
    private OWLDataFactory df;
    private boolean consistent;
    private OWLReasoner res;
    public int curName;
    private boolean makeChange;
    private boolean T1makeChange;
    private OWLReasonerFactory rf;
    private boolean subsumption;
    private OWLClassExpression B;
    private OWLNamedIndividual a;
    private boolean subsumed;
    private OWLOntologyManager man;
    public Set<OWLAxiom> TBoxAxioms;
    public Set<OWLAxiom> ABoxAxioms;
    private Object[][] conceptData;
    private Object[][]  roleData;
    private int numRemoved;
    //    public int numUses;
    //    public int numGenerated;
    public boolean bigModel;
    //    public boolean trueEntalement;
    //    private Set<OWLAxiom> axioms;
    //	public OWLClassExpression newExp;
    //	private boolean getExp;
    //    public boolean failure;
    //    public Set<OWLAxiom> additionalINf;



    public ELReasoner() {
        this.rf = new ReasonerFactory();
        this.man = OWLManager.createOWLOntologyManager();
        this.df = man.getOWLDataFactory();
        this.a = df.getOWLNamedIndividual(IRI.create("root-Ind"));
        this.B = df.getOWLClass(IRI.create("FreshClass"));

    }

    public Object[][] getConceptData() {
        return conceptData;
    }
    public boolean getSubsumed() {
        return subsumed;
    }
    public boolean getConsistent() {
        return consistent;
    }
    public Object[][] getRoleData() {
        return roleData;
    }

    public int getNumRemoved() {
        return numRemoved;
    }
    public void setOntology(OWLOntology ont) {
        this.TBoxAxioms = ont.getTBoxAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
        this.ABoxAxioms = ont.getABoxAxioms(Imports.INCLUDED).stream().collect(Collectors.toSet());
    }

    //	public void trueSub(OWLClassExpression C,OWLClassExpression D) {
//		res.flush();
//		if (res.isConsistent()) {
//		trueEntalement = res.isEntailed(df.getOWLSubClassOfAxiom(C, D));
//		}
//	}
//
//	public void trueCon() {
//		res.flush();
//		trueEntalement = res.isConsistent();
//	}
    private void reset () {

//        this.additionalINf = new HashSet<>();
        this.consistent = true;
        this.curName = 0;
        this.makeChange = true;
        this.subsumption = false;
//		this.getExp = true;
        this.subsumed = false;
//        this.numUses = 0;

        this.map = new HashMap<>();

    }

    public void checkSubsumption(OWLClassExpression C,OWLClassExpression D, boolean bigModel) {

        this.reset();
        this.subsumption = true;
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
        this.generateData();

    }




    public void getModel() {

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
    }
//    public void checkConsistensy() {
//        this.reset();
//
//        if(consistent) {
//            System.out.println("consistent. model:");
////			map.entrySet().forEach(e -> System.out.println(e));
//        } else {
//            System.out.println("inconsistent");
////			map.entrySet().forEach(e -> System.out.println(e));
//        }
//
//
//    }

//	public void getNewAx() {
//		ont.getNestedClassExpressions().stream().takeWhile(exp -> getExp).
//			filter(exp -> exp.getClassExpressionType()==ClassExpressionType.OBJECT_INTERSECTION_OF)
//			.forEach(exp -> {newExp = exp;getExp=false;});
//	}

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

                OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("Ind-"+String.valueOf(curName)));
//					a.getSignature().add(passedInd)
                man.addAxiom(this.ont,df.getOWLObjectPropertyAssertionAxiom(obj.getProperty(), passedInd, a));
                man.addAxiom(this.ont,df.getOWLClassAssertionAxiom(obj.getFiller(), a));

//					OWLDeclarationAxiom dAx = df.getOWLDeclarationAxiom(a);
//					additionalINf.add(dAx);
//					OWLAnnotationProperty prop = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
//					OWLLiteral lit = df.getOWLLiteral("SuccessorIndividual"+curName);
//					OWLAnnotation ann = df.getOWLAnnotation(prop, lit);
//					OWLEntity entity = df.getOWLEntity(EntityType.NAMED_INDIVIDUAL, a.getIRI());
//					additionalINf.add(df.getOWLAnnotationAssertionAxiom(entity.getIRI(), ann));

                curName = curName+1;
                hasSuccessor = true;
            }
        }
        else {
            hasSuccessor = false;
        }
        return hasSuccessor;
    }

    public void A3(){

        this.makeChange =  map.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .filter(expr -> expr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        .anyMatch(expr -> findSuccessor(expr, entry.getKey()))
                );
    }

    public void A2() {

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

    public void A1() {
        ont.getAxioms(AxiomType.CLASS_ASSERTION).forEach(ax -> {
            map.merge((OWLNamedIndividual) ax.getIndividual(), ax.getClassExpression().asConjunctSet(), (a, b) -> {
                a.addAll(b);
                return a;
            });
        });
    }

    public void T1() {
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


//	private Object[][] getArray(Map<Object,Object> map) {
//
//		Object[] keys = map.keySet().toArray();
//		Object[] values = map.values().toArray();
//		Object[][] matrix = {keys,values};
//		int column = 2;
//		int row = keys.length;
//		Object[][] transpose = new Object[row][column];
//        for(int i = 0; i < column; i++) {
//            for (int j = 0; j < row; j++) {
//                transpose[j][i] = matrix[i][j];
//            }
//        }
//		return transpose;
//	}

    public void generateData() {

        List<List<Object>> conceptList = new ArrayList<>();
        List<List<Object>> roleList = new ArrayList<>();

        map.entrySet().stream().forEach(e ->
                e.getValue().stream().filter(c -> c.isClassExpressionLiteral())
                        .filter(cl ->!( cl.toString().startsWith("<X")|| cl.toString().equals("<FreshClass>")))
                        .forEach(c -> conceptList.add(Arrays.asList(e.getKey().toString().substring(1, e.getKey().toString().length() - 1), c))));
        this.conceptData = conceptList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);

        ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION).forEach(a ->
                roleList.add(Arrays.asList( a.getSubject().toString().substring(1, a.getSubject().toString().length() - 1),a.getProperty(), a.getObject().toString().substring(1, a.getObject().toString().length() - 1))));

        this.roleData = roleList.stream()
                .map(l -> l.stream().toArray(Object[]::new))
                .toArray(Object[][]::new);
    }

}
