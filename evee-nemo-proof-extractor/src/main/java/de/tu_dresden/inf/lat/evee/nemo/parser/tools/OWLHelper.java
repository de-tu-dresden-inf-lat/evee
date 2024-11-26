package de.tu_dresden.inf.lat.evee.nemo.parser.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.Set;

/**
 * @author Christian Alrabbaa
 *
 */
public class OWLHelper {

    private static final Logger logger = LogManager.getLogger(OWLHelper.class);
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
     * Return a sub-class-of axiom of the provided class expressions
     *
     * @return OWLSubClassOfAxiom
     */
    public OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass) {
        return factory.getOWLSubClassOfAxiom(subClass, superClass);
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
     * Return an OWL existential restriction
     *
     * @return OWLObjectSomeValuesFrom
     */
    public OWLObjectSomeValuesFrom getOWLExistentialRestriction(OWLObjectPropertyExpression property,
                                                                OWLClassExpression concept) {
        return factory.getOWLObjectSomeValuesFrom(property, concept);
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

}
