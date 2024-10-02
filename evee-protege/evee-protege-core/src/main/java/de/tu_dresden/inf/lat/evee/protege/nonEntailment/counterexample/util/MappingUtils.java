package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class MappingUtils {

    private static final OWLDataFactory df = OWLManager.getOWLDataFactory();

    public static Set<OWLSubClassOfAxiom> disjToSubclassOfAx (Set<OWLAxiom> axioms) {
        return axioms.stream()
                .map(ax -> (OWLDisjointClassesAxiom) ax)
                .flatMap(ax -> ax.asPairwiseAxioms().stream()
                        .map(pax -> {
                            OWLClassExpression first = pax.getClassExpressionsAsList().get(0);
                            OWLClassExpression second = pax.getClassExpressionsAsList().get(1);
                            return df.getOWLSubClassOfAxiom(df.getOWLObjectIntersectionOf(first, second), df.getOWLNothing());
                        })
                )
                .collect(Collectors.toSet());
    }


    public static Map<String[], Set<OWLObjectProperty>> toPairsToObjectPropertiesMap(Set<OWLIndividualAxiom> model) {
        Map<String[],Set<OWLObjectProperty>> pairsToObjectPropertiesMap = new HashMap<>();
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
                .map(ax -> (OWLObjectPropertyAssertionAxiom) ax)
                .forEach(ax -> pairsToObjectPropertiesMap.merge(
                        new String[]{ax.getSubject().asOWLNamedIndividual().getIRI().getShortForm(),
                                ax.getObject().asOWLNamedIndividual().getIRI().getShortForm()},
                        Sets.newHashSet(ax.getProperty().asOWLObjectProperty()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }));
        return pairsToObjectPropertiesMap;
    }
    public static Map<String, Set<OWLClass>> toIndividualsToClassesMap(Set<OWLIndividualAxiom> model) {
        Map<String, Set<OWLClass>> individualsToClassesMap = new HashMap<>();
        for (OWLIndividualAxiom ax:model) {
            if(ax.isOfType(AxiomType.CLASS_ASSERTION)) {
                OWLClassAssertionAxiom clAssertion = (OWLClassAssertionAxiom) ax;
                individualsToClassesMap.computeIfAbsent(
                                clAssertion.getIndividual().asOWLNamedIndividual().getIRI().getShortForm(),
                                k -> new HashSet<>())
                        .add(clAssertion.getClassExpression().asOWLClass());
            } else if(ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
                OWLObjectPropertyAssertionAxiom objAssertion = (OWLObjectPropertyAssertionAxiom) ax;
                for(OWLNamedIndividual ind: objAssertion.getIndividualsInSignature()) {
                    if(!individualsToClassesMap.containsKey(ind.getIRI().getShortForm())) {
                        individualsToClassesMap.put(ind.getIRI().getShortForm(),new HashSet<>());
                    }
                }
            }

        }

//
//        model.stream()
//                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
//                .map(ax -> (OWLClassAssertionAxiom) ax)
//                .forEach(ax ->
//                        individualsToClassesMap.computeIfAbsent(
//                                        ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm(),
//                                        k -> new HashSet<>())
//                                .add(ax.getClassExpression().asOWLClass()));
        return individualsToClassesMap;
    }
}
