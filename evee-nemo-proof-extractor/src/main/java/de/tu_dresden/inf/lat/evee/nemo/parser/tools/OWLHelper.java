package de.tu_dresden.inf.lat.evee.nemo.parser.tools;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Alrabbaa
 *
 */
public class OWLHelper {

    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private final OWLDataFactory factory = manager.getOWLDataFactory();

    private OWLHelper() {
    }

    private static class LazyHolder {
        static OWLHelper instance = new OWLHelper();
    }

    public static OWLHelper getInstance() {
        return LazyHolder.instance;
    }

    /**
     * Returns an owl:Thing object
     *
     * @return OWLClass
     */
    public OWLClass getOWLTop() {
        return factory.getOWLThing();
    }

    /**
     * Returns an owl:Nothing object
     *
     * @return OWLClass
     */
    public OWLClass getOWLBot() {
        return factory.getOWLNothing();
    }

    /**
     * Return a sub-class-of axiom of the provided class expressions
     *
     * @return OWLSubClassOfAxiom
     */
    public OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass) {
        return factory.getOWLSubClassOfAxiom(subClass, superClass);
    }

    /**
     * Return an OWL equivalence class axiom
     *
     * @return OWLEquivalentClassesAxiom
     */
    public OWLEquivalentClassesAxiom getOWLEquivalenceAxiom(OWLClassExpression cls1, OWLClassExpression cls2) {
        return factory.getOWLEquivalentClassesAxiom(cls1, cls2);
    }

    /**
     * Return an OWL disjoint classes axiom
     *
     * @return OWLDisjointClassesAxiom
     */
    public OWLDisjointClassesAxiom getOWLDisjointAxiom(OWLClassExpression... classExpressions){
        return factory.getOWLDisjointClassesAxiom(classExpressions);
    }

    /**
     * Return an OWL domain axiom
     *
     * @return getOWLPropertyDomainAxiom
     */
    public OWLObjectPropertyDomainAxiom getOWLPropertyDomainAxiom(OWLObjectPropertyExpression prop,
                                                                  OWLClassExpression cls){
        return factory.getOWLObjectPropertyDomainAxiom(prop, cls);
    }
    
    /**
     * Returns a sub-property-chain axiom
     *
     * @return OWLSubPropertyChainOfAxiom
     */
    public OWLSubPropertyChainOfAxiom getOWLSubPropertyChainOfAxiom(List<OWLObjectPropertyExpression> expresionsList,
                                                                    OWLObjectPropertyExpression superProperty){
        return factory.getOWLSubPropertyChainOfAxiom(expresionsList,superProperty);
    }

    /**
     * Return a sub-object-property axiom of the provided object properties
     *
     * @return OWLSubObjectPropertyOfAxiom
     */
    public OWLSubObjectPropertyOfAxiom getOWLSubObjectPropertyAxiom(OWLObjectPropertyExpression subProp,
                                                                    OWLObjectPropertyExpression superProp){
        return factory.getOWLSubObjectPropertyOfAxiom(subProp,superProp);
    }

    /**
     * Return a transitive-object-property axiom of the provided object property
     *
     * @return OWLTransitiveObjectPropertyAxiom
     */
    public OWLTransitiveObjectPropertyAxiom getOWOwlTransitivePropertyAxiom(OWLObjectPropertyExpression prop){

        return factory.getOWLTransitiveObjectPropertyAxiom(prop);
    }

    /**
     * Return an OWL existential restriction
     *
     * @return OWLObjectSomeValuesFrom
     */
    public OWLObjectSomeValuesFrom getOWLExistentialRestriction(OWLObjectPropertyExpression property,
                                                                OWLClassExpression concept) {
        return factory.getOWLObjectSomeValuesFrom(property, concept);
    }

    /**
     * Return an OWL value restriction
     *
     * @return OWLObjectAllValuesFrom
     */
    public OWLObjectAllValuesFrom getOWLUniversalRestriction(OWLObjectPropertyExpression property, OWLClassExpression concept){
        return factory.getOWLObjectAllValuesFrom(property, concept);
    }

    /**
     * Return an OWL conjunction object
     *
     * @return OWLObjectIntersectionOf
     */
    public OWLObjectIntersectionOf getOWLConjunction(Set<OWLClassExpression> conjuncts) {
        return factory.getOWLObjectIntersectionOf(conjuncts);
    }

    /**
     * Return an OWL disjunction object
     *
     * @return OWLObjectUnionOf
     */
    public OWLObjectUnionOf getOWLDisjunction(Set<OWLClassExpression> disjuncts) {
        return factory.getOWLObjectUnionOf(disjuncts);
    }

    /**
     * Returns an OWL ComplementOf object
     *
     * @return OWLObjectComplementOf
     */
    public OWLObjectComplementOf getOWLComplementOf(OWLClassExpression cls) {
        return factory.getOWLObjectComplementOf(cls);
    }

    /**
     * Return an OWL exact number restriction
     *
     * @return OWLObjectExactCardinality
     */
    public OWLObjectExactCardinality getOWLNumberRestrEqual(OWLObjectPropertyExpression prop, OWLClassExpression concept, int cardinality){
        return factory.getOWLObjectExactCardinality(cardinality, prop, concept);
    }

    /**
     * Return an OWL max number restriction
     *
     * @return OWLObjectMaxCardinality
     */
    public OWLObjectMaxCardinality getOWLNumberRestrMax(OWLObjectPropertyExpression prop, OWLClassExpression concept, int cardinality){
        return factory.getOWLObjectMaxCardinality(cardinality, prop, concept);
    }

    /**
     * Return an OWL min number restriction
     *
     * @return OWLObjectMinCardinality
     */
    public OWLObjectMinCardinality getOWLNumberRestrMin(OWLObjectPropertyExpression prop, OWLClassExpression concept, int cardinality){
        return factory.getOWLObjectMinCardinality(cardinality, prop, concept);
    }

    /**
     * Return an OWL hasSelf restriction
     *
     * @return OWLObjectHasSelf
     */
    public OWLObjectHasSelf getOWLHasSelf(OWLObjectPropertyExpression prop){
        return factory.getOWLObjectHasSelf(prop);
    }


    public OWLObjectOneOf getOWLOneOf(OWLIndividual... individuals){
        return factory.getOWLObjectOneOf(individuals);
    }


    /**
     * Return an object property of the provided string
     *
     * @return OWLObjectProperty
     */
    public OWLObjectProperty getPropertyName(String string) {
        return factory.getOWLObjectProperty(IRI.create(string));
    }

    /**
     * Return a class of the provided string
     *
     * @return OWLClass
     */
    public OWLClass getOWLConceptName(String string) {
        return factory.getOWLClass(IRI.create(string));
    }

    /**
     * Return a named individual of the provided string
     *
     * @return OWLNamedIndividual
     */
    public OWLNamedIndividual getNamedIndividual(String string){
        return factory.getOWLNamedIndividual(IRI.create(string));
    }

    public Collection<OWLClassExpression> getTopLevelClassExpressions(OWLAxiom axiom) throws ProofGenerationException {
        if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
            OWLSubClassOfAxiom subClsOfAxiom = (OWLSubClassOfAxiom) axiom;
            return Arrays.asList(subClsOfAxiom.getSubClass(), subClsOfAxiom.getSuperClass());
        }
        if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
            OWLEquivalentClassesAxiom eqvClsAxiom = (OWLEquivalentClassesAxiom) axiom;
            return eqvClsAxiom.getClassExpressions();
        }
        throw new ProofGenerationException("Axiom type is not supported by this proof generator!");
    }

}
