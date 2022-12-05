package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.GreatestFixpoint;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.LeastFixpoint;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import java.util.*;

public class DLStatementConverter {

    private final ConjunctiveDLStatement statement;
    private final Map<String, FixpointConverter> fixpointConverterMap;
    private final Map<String, Integer> fixpointCurrentLevelMap;
    private final OWLOntology activeOntology;
    private final OWLDataFactory dataFactory;
    private final Queue<List<Integer>> levelList;
    private int currentMaxLevel;
    private boolean singleResultReturned;

    private final Logger logger = LoggerFactory.getLogger(DLStatementConverter.class);

    public DLStatementConverter(ConjunctiveDLStatement statement, OWLOntology activeOntology){
        this.statement = statement;
        this.fixpointConverterMap = new LinkedHashMap<>();
        this.activeOntology = activeOntology;
        this.dataFactory = this.activeOntology.getOWLOntologyManager().getOWLDataFactory();
        this.prepareFixpointMap();
        this.fixpointCurrentLevelMap = new LinkedHashMap<>();
        for (String fixpointIdentifier : this.fixpointConverterMap.keySet()){
            this.fixpointCurrentLevelMap.put(fixpointIdentifier, 0);
        }
        this.levelList = new LinkedList<>();
        if (this.fixpointConverterMap.keySet().size() > 0){
            this.createNextLevelList();
        }
        this.singleResultReturned = false;
    }

    public boolean singletonResult(){
        return this.fixpointConverterMap.keySet().size() == 0;
    }

    public Set<OWLAxiom> getNextConversion(){
        this.logger.debug("getting next conversion");
        Set<OWLAxiom> result = new HashSet<>();
//        no fixpoints in statement -> only one result
        if (this.fixpointConverterMap.keySet().size() == 0){
            this.logger.debug("no fixpoints in statement");
            if (! singleResultReturned){
                this.logger.debug("no result returned yet, generating result once");
                this.singleResultReturned = true;
                for (DLStatement innerStatement :
                        JavaConverters.setAsJavaSet((this.statement).statements())){
                    this.logger.debug("inner statement loop entered");
                    result.add(convert(innerStatement, null, null));
                }
                return result;
            }
            else {
                this.logger.debug("already returned result once, return null");
                return null;
            }
        }
        List<Integer> nextLevelList = this.levelList.poll();
//        no more results for current maxLevel
        if (nextLevelList == null){
            this.logger.debug("no new results for this level, return null");
            return null;
        }
        int index = 0;
        for (String converter : this.fixpointCurrentLevelMap.keySet()){
            this.fixpointCurrentLevelMap.put(converter, nextLevelList.get(index));
            index+= 1;
        }
        for (DLStatement innerStatement :
                JavaConverters.setAsJavaSet((this.statement).statements())){
            result.add(convert(innerStatement, null, null));
        }
        return result;
    }

    protected void setMaxLevel(int newLevel){
        this.currentMaxLevel = newLevel;
    }


    protected OWLAxiom convert(Axiom ax){
        logger.debug("convert DLStatement: " + ax);
        if (ax instanceof Subsumption){
            logger.debug("subsumption found");
            return dataFactory.getOWLSubClassOfAxiom(
                    convert(((Subsumption) ax).subsumer(), null, null),
                    convert(((Subsumption) ax).subsumee(), null, null));
        }
        if (ax instanceof ConceptEquivalence){
            logger.debug("equivalence found");
            return dataFactory.getOWLEquivalentClassesAxiom(
                    convert((((ConceptEquivalence) ax).leftConcept()), null, null),
                    convert(((ConceptEquivalence) ax).rightConcept(), null, null));
        }
//        todo: disjoint-classes-axiom and disjoint-union-axiom
//        todo: object-property-domain-axiom and object-property-range-axiom
//        todo: OWLInverseObjectPropertiesAxiom and OWLFunctionalObjectPropertyAxiom
//        todo: what to do in this case? can this even occur?
        logger.debug("nothing found");
        return null;
    }

    protected OWLAxiom convert(DLStatement dls, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("convert DLStatement: " + dls);
        if (dls instanceof Axiom){
            logger.debug("axiom found");
            return convert((Axiom) dls);
        }
        if (dls instanceof RoleAxiom){
            logger.debug("RoleAxiom found");
            return convert((RoleAxiom) dls);
        }
        if (dls instanceof Assertion){
            logger.debug("Assertion found");
            return convert((Assertion) dls, fixpointIdentifier, fixpointReplacement);
        }
        logger.debug("nothing found");
        return null;
    }

    protected OWLAxiom convert(RoleAxiom ra){
        logger.debug("Convert RoleAxiom: " + ra);
        if (ra instanceof RoleSubsumption){
            logger.debug("role subsumption found");
            return dataFactory.getOWLSubObjectPropertyOfAxiom(
                    convert(((RoleSubsumption) ra).subsumer()),
                    convert(((RoleSubsumption) ra).subsumee()));
        }
        if (ra instanceof TransitiveRoleAxiom){
            logger.debug("transitive role axiom found");
            return dataFactory.getOWLTransitiveObjectPropertyAxiom(
                    convert(((TransitiveRoleAxiom) ra).role()));
        }
//        todo: OWLInverseObjectPropertiesAxiom and OWLFunctionalObjectPropertyAxiom
//        todo: FunctionalRoleAxiom?
//        todo: what to do in this case? can this even occur?
        logger.debug("nothing found");
        return null;
    }

    protected OWLAxiom convert(Assertion a, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert Assertion: " + a);
        if (a instanceof ConceptAssertion){
            logger.debug("concept assertion found");
            return dataFactory.getOWLClassAssertionAxiom(
                    convert(((ConceptAssertion) a).concept(), fixpointIdentifier, fixpointReplacement),
                    convert(((ConceptAssertion) a).individual()));
        }
        else{
            logger.debug("role assertion found");
            return dataFactory.getOWLObjectPropertyAssertionAxiom(
                    convert(((RoleAssertion) a).role()),
                    convert(((RoleAssertion) a).individual1()),
                    convert(((RoleAssertion) a).individual2()));
        }
//        todo: DisjunctiveConceptAssertion??
//        logger.debug("nothing found");
//        return null;
    }

    protected OWLClassExpression convert(Concept c, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("convert Concept: " + c);
        if (c.equals(TopConcept$.MODULE$)){
            logger.debug("top-concept found");
            return dataFactory.getOWLThing();
        }
        if (c.equals(BottomConcept$.MODULE$)){
            logger.debug("bottom-concept found");
            return dataFactory.getOWLNothing();
        }
//        todo: split conceptVariable
        if (c instanceof BaseConcept){
            logger.debug("base concept found");
            if (((BaseConcept) c).name().equals(fixpointIdentifier)){
                return fixpointReplacement;
            }
            else{
                return dataFactory.getOWLClass(IRI.create(((BaseConcept) c).name()));
            }
        }
        if (c instanceof ConceptComplement){
            logger.debug("concept complement found");
//            todo: correct behaviour?
            return dataFactory.getOWLObjectComplementOf(convert(
                    ((ConceptComplement) c).concept(),fixpointIdentifier, fixpointReplacement));
        }
        if (c instanceof ConceptConjunction){
            logger.debug("concept conjunction found");
            return convert((ConceptConjunction) c, fixpointIdentifier, fixpointReplacement);
        }
        if (c instanceof ConceptDisjunction){
            logger.debug("concept disjunction found");
            return convert((ConceptDisjunction) c, fixpointIdentifier, fixpointReplacement);
        }
        if (c instanceof RoleRestriction){
            logger.debug("role restriction found");
            return convert((RoleRestriction) c, fixpointIdentifier, fixpointReplacement);
        }
        if (c instanceof MinNumberRestriction){
            logger.debug("minimal number restriction found");
            return convert((MinNumberRestriction) c, fixpointIdentifier, fixpointReplacement);
        }
        if (c instanceof MaxNumberRestriction){
            logger.debug("maximum number restriction found");
            return convert((MaxNumberRestriction) c, fixpointIdentifier, fixpointReplacement);
        }
        if (c instanceof LeastFixpoint){
            logger.debug("least fixpoint found");
            fixpointIdentifier = ((LeastFixpoint) c).variable().name();
            FixpointConverter converter = this.fixpointConverterMap.get(fixpointIdentifier);
            return converter.convert(this.fixpointCurrentLevelMap.get(fixpointIdentifier));
        }
        if (c instanceof GreatestFixpoint){
            logger.debug("gratest fixtpoint found");
            fixpointIdentifier = ((GreatestFixpoint) c).variable().name();
            FixpointConverter converter = this.fixpointConverterMap.get(fixpointIdentifier);
            return converter.convert(this.fixpointCurrentLevelMap.get(fixpointIdentifier));
        }
        else {
            logger.debug("nominalset found");
            HashSet<OWLIndividual> individuals = new HashSet<>();
            for (Individual i : JavaConverters.setAsJavaSet(((NominalSet) c).nominals())){
                individuals.add(convert(i));
            }
            return dataFactory.getOWLObjectOneOf(individuals);
        }
//        todo: what to do in this case? can this even occur?
//        logger.debug("nothing found");
//        return null;
    }

    protected OWLClassExpression convert(RoleRestriction rr, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        if (rr instanceof ExistentialRoleRestriction){
            logger.debug("existential role restriction found");
            return convert((ExistentialRoleRestriction) rr, fixpointIdentifier, fixpointReplacement);
        }
        if (rr instanceof UniversalRoleRestriction){
            logger.debug("universal role restriction found");
            return convert((UniversalRoleRestriction) rr, fixpointIdentifier, fixpointReplacement);
        }
        logger.debug("nothing found");
        return null;
    }

    protected OWLObjectIntersectionOf convert(ConceptConjunction cc, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert ConceptConjunction: " + cc);
        HashSet<OWLClassExpression> conjuncts = new HashSet<>();
        for (Concept c : JavaConverters.setAsJavaSet(cc.conjuncts())){
            logger.debug("concept in conjunction found");
            conjuncts.add(convert(c, fixpointIdentifier, fixpointReplacement));
        }
        logger.debug("returning conjuncts: " + conjuncts);
        return dataFactory.getOWLObjectIntersectionOf(conjuncts);
    }

    protected OWLObjectUnionOf convert(ConceptDisjunction cd, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert ConceptDisjunction: " + cd);
        HashSet<OWLClassExpression> elements = new HashSet<>();
        for (Concept c : JavaConverters.setAsJavaSet(cd.disjuncts())){
            logger.debug("concept in disjunction found");
            elements.add(convert(c, fixpointIdentifier, fixpointReplacement));
        }
        logger.debug("returning disjuncts: " + elements);
        return dataFactory.getOWLObjectUnionOf(elements);
    }

    protected OWLObjectSomeValuesFrom convert(ExistentialRoleRestriction err, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert ExistentialRoleRestriction: " + err);
        return dataFactory.getOWLObjectSomeValuesFrom(
                convert(err.role()), convert(err.filler(), fixpointIdentifier, fixpointReplacement));
    }

    protected OWLObjectAllValuesFrom convert(UniversalRoleRestriction urr, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert UniversalRoleRestriction: " + urr);
        return dataFactory.getOWLObjectAllValuesFrom(
                convert(urr.role()), convert(urr.filler(), fixpointIdentifier, fixpointReplacement));
    }

    protected OWLObjectMinCardinality convert(MinNumberRestriction mnr, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert MinNumberRestriction: " + mnr);
        return dataFactory.getOWLObjectMinCardinality(
                mnr.number(), convert(mnr.role()), convert(mnr.filler(), fixpointIdentifier, fixpointReplacement));
    }

    protected OWLObjectMaxCardinality convert(MaxNumberRestriction mnr, String fixpointIdentifier, OWLClassExpression fixpointReplacement){
        logger.debug("Convert MaxNumberRestriction: " + mnr);
        return dataFactory.getOWLObjectMaxCardinality(
                mnr.number(), convert(mnr.role()), convert(mnr.filler(), fixpointIdentifier, fixpointReplacement));
    }

//    todo: how to check this case? do we even need this explicitly?
//    public OWLObjectExactCardinality convert(){
//
//    }

    protected OWLObjectPropertyExpression convert(Role r){
        logger.debug("Convert Role: " + r);
        if (r.equals(TopRole$.MODULE$)){
            logger.debug("TopRole found");
            return dataFactory.getOWLTopObjectProperty();
        }
        if (r instanceof BaseRole){
            logger.debug("BaseRole found");
            return dataFactory.getOWLObjectProperty(IRI.create(((BaseRole) r).name()));
        }
        else {
//            todo: correct behaviour?
            logger.debug("InverseRole found");
            return dataFactory.getOWLObjectInverseOf(
                    dataFactory.getOWLObjectProperty(
                            IRI.create(r.toString())));
        }
//        todo: RoleConjunction, RoleDisjunction?
    }

    protected OWLIndividual convert(Individual i){
        logger.debug("Convert individual: " + i);
        Set<OWLNamedIndividual> individualsInSignature = this.activeOntology.getIndividualsInSignature();
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(IRI.create(i.name()));
        if (individualsInSignature.contains(individual)){
            logger.debug("named individual found");
            return individual;
        }
        else {
            logger.debug("anonymous individual found");
            return dataFactory.getOWLAnonymousIndividual(i.name());
        }
    }

    protected void prepareFixpointMap(){
        for (DLStatement innerStatement : JavaConverters.setAsJavaSet(( this.statement).statements())){
            this.findFixpoint(innerStatement);
        }
    }

    protected void findFixpoint(DLStatement dls){
        if (dls instanceof ConceptAssertion){
            findFixpoint(((ConceptAssertion) dls).concept());
        }
        else if (dls instanceof Axiom){
            findFixpoint(((Axiom) dls));
        }
    }

    protected void findFixpoint(Axiom a){
        if (a instanceof Subsumption){
            this.findFixpoint(((Subsumption) a).subsumee() );
            this.findFixpoint(((Subsumption) a).subsumer());
        }
        else if (a instanceof ConceptEquivalence){
            this.findFixpoint(((ConceptEquivalence) a).leftConcept());
            this.findFixpoint(((ConceptEquivalence) a).rightConcept());
        }
    }

    protected void findFixpoint(Concept c){
        if (c instanceof LeastFixpoint){
            LeastFixpoint lfp = (LeastFixpoint) c;
            FixpointConverter converter = new FixpointConverter(
                    lfp.variable().name(), lfp.concept(),
                    dataFactory.getOWLNothing(), this);
            this.fixpointConverterMap.put(lfp.variable().name(), converter);
        }
        else if (c instanceof  GreatestFixpoint){
            GreatestFixpoint gfp = (GreatestFixpoint) c;
            FixpointConverter converter = new FixpointConverter(
                    gfp.variable().name(), gfp.concept(),
                    dataFactory.getOWLThing(), this);
            this.fixpointConverterMap.put(gfp.variable().name(), converter);
        }
        else if (c instanceof ConceptComplement){
            this.findFixpoint(((ConceptComplement) c).concept());
        }
        else if (c instanceof ConceptConjunction){
            for (Concept conj : JavaConverters.setAsJavaSet(((ConceptConjunction) c).conjuncts())){
                this.findFixpoint(conj);
            }
        }
        else if (c instanceof ConceptDisjunction){
            for (Concept disj : JavaConverters.setAsJavaSet(((ConceptDisjunction) c).disjuncts())){
                this.findFixpoint(disj);
            }
        }
        else if (c instanceof RoleRestriction){
            this.findFixpoint(((RoleRestriction) c).filler());
        }
        else if (c instanceof MaxNumberRestriction){
            this.findFixpoint(((MaxNumberRestriction) c).filler());
        }
        else if (c instanceof MinNumberRestriction){
            this.findFixpoint(((MinNumberRestriction) c).filler());
        }
    }

    /**
     * We save in this.levelList a list of lists of integers. Each integer represents the level of a fixpoint-statement.
     * A list of integers represents the level to which each fixpoint should be unraveled for a single solution.
     * We save in this.levelList only those lists which contain at least one element of the current maximum level up to
     * which we unravel a fixpoint.
     */
    protected void createNextLevelList(){
        this.levelList.clear();
        List<List<Integer>> results = this.recursivelyCreateLevelList(
                this.fixpointCurrentLevelMap.keySet().size());
        for (List<Integer> singleResult : results){
            if (singleResult.contains(this.currentMaxLevel)){
                this.levelList.add(singleResult);
            }
        }
    }

    private List<List<Integer>> recursivelyCreateLevelList(Integer position){
        ArrayList<List<Integer>> resultList = new ArrayList<>();
        if (position == 1){
            for (int level = 0; level <= currentMaxLevel; level++){
                ArrayList<Integer> singleResult = new ArrayList<>();
                singleResult.add(level);
                resultList.add(singleResult);
            }
        }
        else {
            List<List<Integer>> nextRecursionResult =
                    this.recursivelyCreateLevelList(position -1);
            for (List<Integer> singleResult : nextRecursionResult){
                for (int level = 0; level <= currentMaxLevel; level++){
                    List<Integer> newSingleResult = new ArrayList<>(singleResult);
                    newSingleResult.add(level);
                    resultList.add(newSingleResult);
                }
            }
        }
        return resultList;
    }

}
