package de.tu_dresden.inf.lat.counterExample.data;

import de.tu_dresden.inf.lat.counterExample.RefinerMapper;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 * This is a helper class to keep track of 1) what types an element should have 2) whether an element should or should
 * not retain its types and 3) whether a relation should stay in the model.
 *
 * This is intended for Diff and Flatdiff model types
 **/
public class Tracker {
    private final Map<Element, Set<Element>> typesNeededCollector;
    private final Map<Element, Set<Boolean>> typesImplied;
    private final Map<Relation, Set<Boolean>> relationExists;
    private final Set<Relation> impliedRelations;

    public Tracker() {
        this.typesNeededCollector = new HashMap<>();
        this.typesImplied = new HashMap<>();
        this.relationExists = new HashMap<>();

        this.impliedRelations = new HashSet<>();
    }

    /* Types needed collector maps an Element e to a set of Elements which their types must be kept by e*/
    public void addToTypesNeededCollector(Element e, Element eWithTypesToKeep){
        RefinerMapper.addToMap(e, eWithTypesToKeep, this.typesNeededCollector);
    }

    public Set<OWLClassExpression> getTypesToKeep(Element e){
        return this.typesNeededCollector.getOrDefault(e, Collections.emptySet()).stream()
                .map(Element::getTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /* Relation Exists maps a relation r to a set of boolean, where true (false) means that there is another (no)
    relation that justify why r should be kept*/
    public void addToRelationExists(Relation r, boolean isJustified){
        RefinerMapper.addToMap(r, isJustified, this.relationExists);
    }

    public boolean checkRelationJustified(Relation r){
        return this.relationExists.getOrDefault(r, Collections.emptySet()).contains(true);
    }

    public Map<Relation, Set<Boolean>> getRelationExists() {
        return relationExists;
    }

    /* Type implied maps an element to a boolean indicating whether the labels of the elements should be kept (true)
    or need to be filtered*/
    public void addToTypesImplied(Element e, Boolean labelsAreImplied){
        RefinerMapper.addToMap(e, labelsAreImplied, this.typesImplied);
    }

    public boolean checkTypesImplied(Element e){
        return this.typesImplied.getOrDefault(e, Collections.emptySet()).contains(true);
    }

    public Map<Element, Set<Boolean>> getTypesImplied() {
        return typesImplied;
    }

    /*If a relation a -r->e1 could be match with a relation b-r-> e2 where the labels of e1 is a subset of the labels
     of e2, then -r->e1 is an implied relation*/
    public Set<Relation> getImpliedRelations() {
        return impliedRelations;
    }

    public void addToImpliedRelations(Relation r){
        this.impliedRelations.add(r);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tracker tracker = (Tracker) o;
        return typesNeededCollector.equals(tracker.typesNeededCollector) &&
                typesImplied.equals(tracker.typesImplied) &&
                relationExists.equals(tracker.relationExists) &&
                impliedRelations.equals(tracker.impliedRelations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typesNeededCollector, typesImplied, relationExists, impliedRelations);
    }
}
