package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Collectors;

public class OWLObjectSorter {

    private OWLReasoner res;
    private final OWLDataFactory df;

    OWLObjectSorter(OWLReasoner res, OWLDataFactory df) {
        this.res = res;
        this.df = df;
    }




    public <T extends OWLNamedObject> List<T> sortOWLObjectList(List<T> objectList) {
        List<T> finalList = new ArrayList<>();
        List<T> sorted = new ArrayList<>();
        List<T> sortedOut = objectList;
        int i = 0;

        while (!sortedOut.isEmpty() && i < 10) {
            if (sorted.isEmpty()) {
                sorted = sortedOut;
            }
            List<List<T>> sortedAndSortedOut = compareOWLObjects(sorted);
            sorted = sortedAndSortedOut.get(0);
            sortedOut = sortedAndSortedOut.get(1);
            if (sorted.isEmpty()) {
                finalList.addAll(sortedOut);
                break;
            }
            if (sortedOut.isEmpty()) {
                finalList.addAll(sorted);
                break;
            }
            finalList.addAll(sortedOut);
            i = i + 1;
        }
        Collections.reverse(finalList);
        return finalList;
    }

    private<M extends OWLNamedObject> OWLAxiom getInclusion(M a, M b) {
        if (!a.getObjectPropertiesInSignature().isEmpty()) {
            return df.getOWLSubObjectPropertyOfAxiom((OWLObjectProperty) a, (OWLObjectProperty) b);
        } else {
            return df.getOWLSubClassOfAxiom((OWLClass) a, (OWLClass) b);
        }
    }



    private <T extends OWLNamedObject> List<List<T>> compareOWLObjects(List<T> objectList) {
        Set<T> subsumed = new HashSet<>();
        Set<T> subsumers = new HashSet<>();
        List<List<T>> returnList = new ArrayList<>();

        objectList.stream().forEach(expr1 -> objectList.stream().filter(expr2 -> !expr1.equals(expr2))
                .filter(expr2 -> res.isEntailed(getInclusion(expr1, expr2))
                        && !res.isEntailed(getInclusion(expr2, expr1 )))
                .forEach(expr2 -> {
                    subsumed.add(expr1);
                    subsumers.add(expr2);
                }));

        List<T> moreExact = subsumed.stream().collect(Collectors.toList());
        objectList.removeAll(subsumers);
        objectList.removeAll(subsumed);
        moreExact.addAll(objectList);
        returnList.add(moreExact);
        subsumers.removeAll(subsumed);
        returnList.add(subsumers.stream().collect(Collectors.toList()));
        return returnList;
    }
}
