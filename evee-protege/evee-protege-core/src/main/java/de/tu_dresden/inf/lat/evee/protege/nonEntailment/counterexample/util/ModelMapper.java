package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelMapper {


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
        model.stream()
                .filter(ax -> ax.isOfType(AxiomType.CLASS_ASSERTION))
                .map(ax -> (OWLClassAssertionAxiom) ax)
                .forEach(ax ->
                        individualsToClassesMap.computeIfAbsent(
                                        ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm(),
                                        k -> new HashSet<>())
                                .add(ax.getClassExpression().asOWLClass()));
        return individualsToClassesMap;
    }
}
