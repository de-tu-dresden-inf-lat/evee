package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;
import uk.ac.man.cs.lethe.internal.dl.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.DisjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.GreatestFixpoint;
import uk.ac.man.cs.lethe.internal.dl.abduction.forgetting.LeastFixpoint;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LetheAbductionSolver implements OWLAbductionSolver, Supplier<Set<OWLAxiom>> {

    private Set<OWLAxiom> observation;
    private OWLOntology activeOntology = null;
    private OWLDataFactory dataFactory = null;
    private final OWLAbducer abducer = new OWLAbducer();
    private final List<DLStatementConverter> resultConverterList;
    private int maxLevel;
    private int currentConverterIndex;
    private boolean parametersChanged = false;

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        this.observation = new HashSet<>();
        this.resultConverterList = new ArrayList<>();
        this.maxLevel = 0;
        this.currentConverterIndex = 0;
    }

    @Override
    public void setObservation(Set<OWLAxiom> observation) {
        this.observation = observation;
        this.parametersChanged = true;
    }

    @Override
    public void setAbducibles(Collection<OWLEntity> owlEntities) {
        this.abducer.setAbducibles(new HashSet<>(owlEntities));
        this.parametersChanged = true;
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        this.activeOntology = ontology;
        this.dataFactory = this.activeOntology.getOWLOntologyManager().getOWLDataFactory();
        this.abducer.setBackgroundOntology(ontology);
        this.parametersChanged = true;
    }

    @Override
    public Stream<Set<OWLAxiom>> generateHypotheses() {
        assert (this.activeOntology != null);
        assert (this.dataFactory != null);
        if (this.parametersChanged){
            this.parametersChanged = false;
            this.maxLevel = 0;
            this.currentConverterIndex = 0;
            this.logger.debug("generating hypotheses");
            DLStatement hypotheses = this.abducer.abduce(this.observation);
            this.logger.debug("hypotheses found:\n" + hypotheses);
            this.resultConverterList.clear();
            ((DisjunctiveDLStatement) hypotheses).statements().foreach(
                    statement -> {
                        this.resultConverterList.add(new DLStatementConverter(
                                (ConjunctiveDLStatement) statement, this.activeOntology));
                        return null;
                    });
        }
        return Stream.generate(this);
    }

    @Override
    public Set<OWLAxiom> get() {
        int startIndex = this.currentConverterIndex;
        boolean checkedStartIndex = false;
        while (true){
            if (this.currentConverterIndex == this.resultConverterList.size()){
                this.maxLevel += 1;
                this.currentConverterIndex = 0;
                for (DLStatementConverter converter : this.resultConverterList){
                    if (! converter.singletonResult()){
                        converter.setMaxLevel(this.maxLevel);
                        converter.createNextLevelList();
                    }
                }
            }
            Set<OWLAxiom> result = this.resultConverterList.get(this.currentConverterIndex).getNextConversion();
            this.currentConverterIndex += 1;
            if (result == null){
//                check if we tried all statements
                if (startIndex == this.currentConverterIndex)
                {
                    if (checkedStartIndex){
                        break;
                    }
                    else {
                        checkedStartIndex = true;
                    }
                }
            }
            else{
                return result;
            }
        }
        return null;
    }

    protected void abduce(){

    }


//    public Set<OWLAxiom> beginConversion(DLStatement statement){
//        this.logger.debug("beginning Conversion on statement: " + statement);
//        HashSet<OWLAxiom> result = new HashSet<>();
//        if (statement instanceof ConjunctiveDLStatement){
//            this.logger.debug("ConjunctiveDLStatement found");
//            for (DLStatement innerStatement :
//                    JavaConverters.setAsJavaSet(((ConjunctiveDLStatement) statement).statements())){
//                result.add(convert(innerStatement));
//            }
//        }
//        if (statement instanceof TBox){
//            this.logger.debug("TBox found");
//            for (Axiom axiom : JavaConverters.setAsJavaSet(((TBox) statement).axioms())){
//                this.logger.debug("converting Axiom from TBox: " + axiom);
//                result.add(convert(axiom));
//            }
//        }
//        if (statement instanceof ABox){
//            this.logger.debug("ABox found");
//            for (Assertion assertion : JavaConverters.setAsJavaSet(((ABox) statement).assertions())){
//                this.logger.debug("converting Assertion from ABox: " + assertion);
//                result.add(convert(assertion));
//            }
//        }
//        if (statement instanceof RBox){
//            this.logger.debug("RBox found");
//            for (RoleAxiom roleAxiom : JavaConverters.setAsJavaSet(((RBox) statement).axioms())){
//                this.logger.debug("converting RoleAxiom from RBox: " + roleAxiom);
//                result.add(convert(roleAxiom));
//            }
//        }
//        else {
//            this.logger.debug("No A/T/RBox and no ConjunctiveDLStatement found");
////            result.add(convert(statement));
//        }
//        return result;
//    }

//    public OWLAxiom convert(Expression e){
//        this.logger.debug("Convert Expression: " + e);
//        if (e instanceof Concept){
//            this.logger.debug("concept found");
//            return convert((Concept) e);
//        }
//        if (e instanceof Role){
//            this.logger.debug("role found");
//            return convert((Role) e);
//        }
//        if (e instanceof DLStatement){
//            this.logger.debug("dlstatement found");
//            return convert((DLStatement) e);
//        }
//        if (e instanceof Individual){
//            return convert((Individual) e);
//        }
//        this.logger.debug("nothing found");
//        return null;
//    }

//    public OWLAxiom convert(Axiom ax){
//        this.logger.debug("convert DLStatement: " + ax);
//        if (ax instanceof Subsumption){
//            this.logger.debug("subsumption found");
//            return dataFactory.getOWLSubClassOfAxiom(
//                    convert(((Subsumption) ax).subsumer()),
//                    convert(((Subsumption) ax).subsumee()));
//        }
//        if (ax instanceof ConceptEquivalence){
//            this.logger.debug("equivalence found");
//            return dataFactory.getOWLEquivalentClassesAxiom(
//                    convert((((ConceptEquivalence) ax).leftConcept())),
//                    convert(((ConceptEquivalence) ax).rightConcept()));
//        }
////        todo: disjoint-classes-axiom and disjoint-union-axiom
////        todo: object-property-domain-axiom and object-property-range-axiom
////        todo: OWLInverseObjectPropertiesAxiom and OWLFunctionalObjectPropertyAxiom
////        todo: what to do in this case? can this even occur?
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLAxiom convert(DLStatement dls){
//        this.logger.debug("convert DLStatement: " + dls);
//        if (dls instanceof Axiom){
//            this.logger.debug("axiom found");
//            return convert((Axiom) dls);
//        }
////        if (dls instanceof TBox){
////
////        }
//        if (dls instanceof RoleAxiom){
//            this.logger.debug("RoleAxiom found");
//            return convert((RoleAxiom) dls);
//        }
////        if (dls instanceof RBox){
////
////        }
//        if (dls instanceof Assertion){
//            this.logger.debug("Assertion found");
//            return convert((Assertion) dls);
//        }
////        if (dls instanceof ABox){
////
////        }
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLAxiom convert(RoleAxiom ra){
//        this.logger.debug("Convert RoleAxiom: " + ra);
//        if (ra instanceof RoleSubsumption){
//            this.logger.debug("role subsumption found");
//            return dataFactory.getOWLSubObjectPropertyOfAxiom(
//                    convert(((RoleSubsumption) ra).subsumer()),
//                    convert(((RoleSubsumption) ra).subsumee()));
//        }
//        if (ra instanceof TransitiveRoleAxiom){
//            this.logger.debug("transitive role axiom found");
//            return dataFactory.getOWLTransitiveObjectPropertyAxiom(
//                    convert(((TransitiveRoleAxiom) ra).role()));
//        }
////        todo: OWLInverseObjectPropertiesAxiom and OWLFunctionalObjectPropertyAxiom
////        todo: FunctionalRoleAxiom?
////        todo: what to do in this case? can this even occur?
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLAxiom convert(Assertion a){
//        this.logger.debug("Convert Assertion: " + a);
//        if (a instanceof ConceptAssertion){
//            this.logger.debug("concept assertion found");
//            return dataFactory.getOWLClassAssertionAxiom(
//                    convert(((ConceptAssertion) a).concept()),
//                    convert(((ConceptAssertion) a).individual()));
//        }
//        if (a instanceof RoleAssertion){
//            this.logger.debug("role assertion found");
//            return dataFactory.getOWLObjectPropertyAssertionAxiom(
//                    convert(((RoleAssertion) a).role()),
//                    convert(((RoleAssertion) a).individual1()),
//                    convert(((RoleAssertion) a).individual2()));
//        }
////        todo: DisjunctiveConceptAssertion
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLClassExpression convert(Concept c){
//        this.logger.debug("convert Concept: " + c);
//        if (c.equals(TopConcept$.MODULE$)){
//            this.logger.debug("top-concept found");
//            return dataFactory.getOWLThing();
//        }
//        if (c.equals(BottomConcept$.MODULE$)){
//            this.logger.debug("bottom-concept found");
//            return dataFactory.getOWLNothing();
//        }
////        todo: split conceptVariable
//        if (c instanceof BaseConcept){
//            this.logger.debug("base concept found");
//            return dataFactory.getOWLClass(IRI.create(((BaseConcept) c).name()));
//        }
//        if (c instanceof ConceptComplement){
//            this.logger.debug("concept complement found");
////            todo: correct behaviour?
//            return dataFactory.getOWLObjectComplementOf(convert(((ConceptComplement) c).concept()));
//        }
//        if (c instanceof ConceptConjunction){
//            this.logger.debug("concept conjunction found");
//            return convert((ConceptConjunction) c);
//        }
//        if (c instanceof ConceptDisjunction){
//            this.logger.debug("concept disjunction found");
//            return convert((ConceptDisjunction) c);
//        }
//        if (c instanceof RoleRestriction){
//            this.logger.debug("role restriction found");
//            return convert((RoleRestriction) c);
//        }
//        if (c instanceof MinNumberRestriction){
//            this.logger.debug("minimal number restriction found");
//            return convert((MinNumberRestriction) c);
//        }
//        if (c instanceof MaxNumberRestriction){
//            this.logger.debug("maximum number restriction found");
//            return convert((MaxNumberRestriction) c);
//        }
//        if (c instanceof LeastFixpoint){
//            this.logger.debug("least fixpoint found");
//            return convert(((LeastFixpoint) c).concept());
//        }
//        if (c instanceof GreatestFixpoint){
//            this.logger.debug("gratest fixtpoint found");
//            return convert(((GreatestFixpoint)c).concept());
//        }
//        if (c instanceof NominalSet){
//            this.logger.debug("nominalset found");
//            HashSet<OWLIndividual> individuals = new HashSet<>();
//            for (Individual i : JavaConverters.setAsJavaSet(((NominalSet) c).nominals())){
//                individuals.add(convert(i));
//            }
//            return dataFactory.getOWLObjectOneOf(individuals);
//        }
////        todo: what to do in this case? can this even occur?
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLClassExpression convert(RoleRestriction rr){
//        if (rr instanceof ExistentialRoleRestriction){
//            this.logger.debug("existential role restriction found");
//            return convert((ExistentialRoleRestriction) rr);
//        }
//        if (rr instanceof UniversalRoleRestriction){
//            this.logger.debug("universal role restriction found");
//            return convert((UniversalRoleRestriction) rr);
//        }
//        this.logger.debug("nothing found");
//        return null;
//    }
//
//    public OWLObjectIntersectionOf convert(ConceptConjunction cc){
//        this.logger.debug("Convert ConceptConjunction: " + cc);
//        HashSet<OWLClassExpression> conjuncts = new HashSet<>();
//        for (Concept c : JavaConverters.setAsJavaSet(cc.conjuncts())){
//            this.logger.debug("concept in conjunction found");
//            conjuncts.add(convert(c));
//        }
//        this.logger.debug("returning conjuncts: " + conjuncts);
//        return dataFactory.getOWLObjectIntersectionOf(conjuncts);
//    }
//
//    public OWLObjectUnionOf convert(ConceptDisjunction cd){
//        this.logger.debug("Convert ConceptDisjunction: " + cd);
//        HashSet<OWLClassExpression> elements = new HashSet<>();
//        for (Concept c : JavaConverters.setAsJavaSet(cd.disjuncts())){
//            this.logger.debug("concept in disjunction found");
//            elements.add(convert(c));
//        }
//        this.logger.debug("returning disjuncts: " + elements);
//        return dataFactory.getOWLObjectUnionOf(elements);
//    }
//
//    public OWLObjectSomeValuesFrom convert(ExistentialRoleRestriction err){
//        this.logger.debug("Convert ExistentialRoleRestriction: " + err);
//        return dataFactory.getOWLObjectSomeValuesFrom(
//                convert(err.role()), convert(err.filler()));
//    }
//
//    public OWLObjectAllValuesFrom convert(UniversalRoleRestriction urr){
//        this.logger.debug("Convert UniversalRoleRestriction: " + urr);
//        return dataFactory.getOWLObjectAllValuesFrom(
//                convert(urr.role()), convert(urr.filler()));
//    }
//
//    public OWLObjectMinCardinality convert(MinNumberRestriction mnr){
//        this.logger.debug("Convert MinNumberRestriction: " + mnr);
//        return dataFactory.getOWLObjectMinCardinality(
//                mnr.number(), convert(mnr.role()), convert(mnr.filler()));
//    }
//
//    public OWLObjectMaxCardinality convert(MaxNumberRestriction mnr){
//        this.logger.debug("Convert MaxNumberRestriction: " + mnr);
//        return dataFactory.getOWLObjectMaxCardinality(
//                mnr.number(), convert(mnr.role()), convert(mnr.filler()));
//    }

//    todo: how to check this case? do we even need this explicitly?
//    public OWLObjectExactCardinality convert(){
//
//    }

//    public OWLObjectPropertyExpression convert(Role r){
//        this.logger.debug("Convert Role: " + r);
//        if (r.equals(TopRole$.MODULE$)){
//            this.logger.debug("TopRole found");
//            return dataFactory.getOWLTopObjectProperty();
//        }
//        if (r instanceof BaseRole){
//            this.logger.debug("BaseRole found");
//            return dataFactory.getOWLObjectProperty(IRI.create(((BaseRole) r).name()));
//        }
//        else {
////            todo: correct behaviour?
//            this.logger.debug("InverseRole found");
//            return dataFactory.getOWLObjectInverseOf(
//                    dataFactory.getOWLObjectProperty(
//                            IRI.create(r.toString())));
//        }
////        todo: RoleConjunction, RoleDisjunction?
//    }

//    public OWLIndividual convert(Individual i){
//        this.logger.debug("Convert individual: " + i);
//        Set<OWLNamedIndividual> individualsInSignature = activeOntology.getOWLModelManager().getActiveOntology().getIndividualsInSignature();
//        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(IRI.create(i.name()));
//        if (individualsInSignature.contains(individual)){
//            this.logger.debug("named individual found");
//            return individual;
//        }
//        else {
//            this.logger.debug("anonymous individual found");
//            return dataFactory.getOWLAnonymousIndividual(i.name());
//        }
//    }

}
