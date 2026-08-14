package de.tu_dresden.inf.lat.evee.concreteDomains.tools;

import  de.tu_dresden.inf.lat.evee.data.names.PropertyName;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;

import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 */

public class Tools {

    /**
     * Return a set of concept names that appear in the input axiom at nesting level 0
     * @param axiom
     * @return
     */
    public static Set<OWLClass> getConceptNamesAtLevelZero(OWLSubClassOfAxiom axiom){
    Set<OWLClassExpression> bothSides = Sets.newHashSet(axiom.getSubClass(), axiom.getSuperClass());
    Set<OWLClass> res = new HashSet<>();

    bothSides.forEach(s-> res.addAll(getConceptNamesAtLevelZero(s)));

    return res;
    }

    /**
     * Return a set of concept names that appear in the input concept at nesting level 0
     * @param clsExp
     * @return
     */
    public static Set<OWLClass> getConceptNamesAtLevelZero(OWLClassExpression clsExp){
        Set<OWLClass> res = new HashSet<>();
        Set<OWLClassExpression> expressions = new HashSet<>();

        if(clsExp instanceof OWLObjectUnionOf)
            expressions.addAll(clsExp.asDisjunctSet());
        else
            expressions.addAll(clsExp.asConjunctSet());

        expressions.forEach(conj-> {
            if (conj instanceof OWLClass)
                res.add((OWLClass) conj);
        });

        return res;
    }
    /**
     * Return a set of all sub-concepts that appear in the input axiom
     * @param axiom
     * @return
     */
    public static Set<OWLClass> getConceptNamesInAxiom(OWLSubClassOfAxiom axiom){
        Set<OWLClassExpression> res = new HashSet<>();

        res.addAll(getSubConcepts(axiom.getSubClass()));
        res.addAll(getSubConcepts(axiom.getSuperClass()));

        return res.stream().filter(x->x instanceof OWLClass).map(OWLClass.class::cast).collect(Collectors.toSet());
    }

    /**
     * Return a set of all sub-concepts of the provided concept expression
     *
     * @param conjunct
     * @return
     */
    public static Set<? extends OWLClassExpression> getSubConcepts(OWLClassExpression conjunct) {

        Set<OWLClassExpression> res = new HashSet<>();

        getSubConcepts(conjunct, res);

        return res;
    }

    /**
     * Recursively iterate over all sub-concepts of the provided expression and
     * store them in the provided set
     *
     * @param expression
     * @param expressions
     */
    private static void getSubConcepts(OWLClassExpression expression, Set<OWLClassExpression> expressions) {
        expressions.add(expression);

        expression.asConjunctSet().forEach(conjunct -> {

            if (conjunct instanceof OWLClass)
                expressions.add(conjunct);

            else if (conjunct instanceof OWLObjectSomeValuesFrom) {
                expressions.add(conjunct);
                getSubConcepts(((OWLObjectSomeValuesFrom) conjunct).getFiller(), expressions);
            }
        });
    }

    public static <T,K> void addToMap(T key, K value, Map<T, Set<K>> map) {
        if(!map.containsKey(key))
            map.put(key,new HashSet<>(Collections.singletonList(value)));
        else
            map.get(key).add(value);
    }

    public static <T,K> scala.collection.immutable.Map<T,K> toScalaImmutableMap(Map<T,K> map){
        List<Tuple2<T, K>> tuples = new LinkedList<>();
        map.forEach((key, value) -> tuples.add(new Tuple2<>(key, value)));
        Seq<Tuple2<T, K>> seq = JavaConverters.asScalaBuffer(tuples).toSeq();
        return (scala.collection.immutable.Map<T, K>) scala.collection.immutable.Map$.MODULE$.apply(seq);
    }

//    public static String formatDouble (Double d){
//        return String.format("%,.2f", d);
//    }

    public static boolean areAssertionsActivated(){
        boolean assertionActivated = false;
        assert assertionActivated = true;
        return assertionActivated;
    }

    public static boolean isZ3CheckEnabled(){
    return System.getProperty(PropertyName.enableZ3Checks.getProperty()) != null &&
            System.getProperty(PropertyName.enableZ3Checks.getProperty()).equalsIgnoreCase("true");
    }
}
