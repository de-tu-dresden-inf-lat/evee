package de.tu_dresden.inf.lat.counterExample;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExampleGenerator;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 */
public class RedundancyRefiner {
    private final RefinerMapper refinerMapper;
    private final RelevantCounterExampleGenerator generator;
    private final Set<Element> modelElements;
    private final Set<Element> reachableFromBoth;

    public RedundancyRefiner(Set<Element> modelElements, RelevantCounterExampleGenerator generator) {
        this.refinerMapper = new RefinerMapper(modelElements);

        this.generator = generator;
        this.modelElements = modelElements;

        refinerMapper.computeSimulation();

        this.reachableFromBoth = getReachableFromBoth();
    }

    public void refine() throws ModelGenerationException {
        Set<Element> startElements = new HashSet<>();
        getElementFrom(this.generator.getElkModel().getMapper().getLHSRepresentativeElement(), this.modelElements).ifPresent(startElements::add);
        getElementFrom(this.generator.getElkModel().getMapper().getRHSRepresentativeElement(), this.modelElements).ifPresent(startElements::add);

        Set<Element> processed = new HashSet<>();
        Set<Relation> toRemoveFlat = new HashSet<>();

        refine(startElements, processed, toRemoveFlat);

        Set<Relation> relToRemove;
        for (Element e : this.modelElements) {
            relToRemove = new HashSet<>();
            for (Relation r : e.getRelations()) {
                if(r.isForward()){
                    if(!this.modelElements.contains(r.getElement2()))
                        relToRemove.add(r);
                }
                if(r.isBackward()){
                    Optional<Element> elmOpt = getElementFrom(r.getElement1(), this.modelElements);
                    if(!elmOpt.isPresent())
                        relToRemove.add(r);
                    else if(elmOpt.get().getRelations().stream().noneMatch(x->x.isForward() && x.getElement2().equals(r.getElement2()))){
                        relToRemove.add(r);
                    }
                }
            }
            relToRemove.forEach(e::removeRelation);

            e.removeTypes(this.generator.getElkModel().getMapper());
        }
    }

    private void refine(Set<Element> startElements, Set<Element> processed, Set<Relation>toRemoveFlat){
        Set<Relation> roleRelations;
        Map<Element, Set<Relation>> toRemove = new HashMap<>();

        for(Element startElement : startElements){
            if(processed.contains(startElement))
                continue;
            processed.add(startElement);

            for(OWLObjectProperty role : this.refinerMapper.getRoleNames()){
                roleRelations =
                        startElement.getRelations().stream().filter(rel->rel.getRoleName().equals(role) && rel.isForward())
                                .collect(Collectors.toSet());
                for (Relation r1 : roleRelations){
                    if (toRemoveFlat.contains(r1))
                        continue;
                    for (Relation r2 : roleRelations){
                        if (toRemoveFlat.contains(r1)||toRemoveFlat.contains(r2))
                            continue;
                        if (r1.equals(r2))
                            continue;

                        if(simulates(r1.getElement2(), r2.getElement2())){
                            if(simulates(r2.getElement2(), r1.getElement2())) {
                                if (this.reachableFromBoth.contains(r1.getElement2())){
                                    RefinerMapper.addToMap(startElement, r2, toRemove);
                                    toRemoveFlat.add(r2);
                                }
                                else {
                                    RefinerMapper.addToMap(startElement, r1, toRemove);
                                    toRemoveFlat.add(r1);
                                }
                            } else{
                                RefinerMapper.addToMap(startElement, r2, toRemove);
                                toRemoveFlat.add(r2);
                            }
                        }
                    }
                }
            }
        }

        toRemove.keySet().forEach(element -> toRemove.get(element).forEach(element::removeRelation));

        removeUnreachableElements(modelElements);

        Set<Element> reachableElements = getReachableElements(startElements, modelElements);
        reachableElements.removeAll(processed);
        if(!reachableElements.isEmpty())
            refine(reachableElements, processed, toRemoveFlat);
    }

    private Set<Element> getReachableElements(Set<Element> startElements, Set<Element> modelElements) {
        Set<Element> res = new HashSet<>();
        for (Element e : startElements){
            for(Relation r : e.getRelations()){
                if (r.isForward())
                    getElementFrom(r.getElement2(), modelElements).ifPresent(res::add);
            }
        }
        return res;
    }

    private boolean simulates(Element e1, Element e2) {
        for(OWLObjectProperty roleName: this.refinerMapper.getRoleNames()){
            if (!this.refinerMapper.getSim().get(roleName).get(e2).contains(e1))
                return false;
        }
        return true;
    }

    private Optional<Element> getElementFrom(Element toFind, Set<Element> elements){
        return elements.stream().filter(x -> x.equals(toFind)).findFirst();
    }
    private void removeUnreachableElements(Set<Element> modelElements){
        Set<Element> reachableElements = new HashSet<>();

        Optional<Element> lhsRepOpt =
                getElementFrom(this.generator.getElkModel().getMapper().getLHSRepresentativeElement(), modelElements);
        Optional<Element> rhsRepOpt =
                getElementFrom(this.generator.getElkModel().getMapper().getRHSRepresentativeElement(), modelElements);

        Set<Element> tmp = new HashSet<>();
        lhsRepOpt.ifPresent(tmp::add);
        rhsRepOpt.ifPresent(tmp::add);

        while (!tmp.equals(reachableElements)) {
            reachableElements.addAll(tmp);

            for (Element e : reachableElements)
                e.getRelations().stream().filter(Relation::isForward)
                    .forEach(x -> getElementFrom(x.getElement2(), modelElements).ifPresent(tmp::add));
        }

        modelElements.retainAll(reachableElements);
    }

    private Set<Element> getReachableFromBoth() {
        Optional<Element> lhsRepOpt =
                getElementFrom(this.generator.getElkModel().getMapper().getLHSRepresentativeElement(),
                        this.modelElements);

        Set<Relation> explored = new HashSet<>();
        Set<Element> reachableFromA = Sets.newHashSet();

        if(!lhsRepOpt.isPresent())
            return reachableFromA;

        reachableFromA.add(lhsRepOpt.get());
        fillReachableElementsSet(lhsRepOpt.get(), reachableFromA, explored);

        //

        Optional<Element> rhsRepOpt =
                getElementFrom(this.generator.getElkModel().getMapper().getRHSRepresentativeElement(),
                        this.modelElements);

        explored = new HashSet<>();
        Set<Element> reachableFromB = Sets.newHashSet();

        if(!rhsRepOpt.isPresent())
            return reachableFromA;

        reachableFromB.add(rhsRepOpt.get());
        fillReachableElementsSet(rhsRepOpt.get(), reachableFromB, explored);

        Set<Element> reachableFromBoth = new HashSet<>(reachableFromA);
        reachableFromBoth.retainAll(reachableFromB);

        return reachableFromBoth;
    }

    private void fillReachableElementsSet(Element element, Set<Element> reachableElements, Set<Relation> processedRelations) {
        Optional<Element> targetElementOpt;

        for (Relation r : element.getRelations()) {
            if (!r.isForward())
                continue;
            if (processedRelations.contains(r))
                continue;
            processedRelations.add(r);

            targetElementOpt = getElementFrom(r.getElement2(), this.modelElements);
            if(targetElementOpt.isPresent()){
                reachableElements.add(targetElementOpt.get());
                fillReachableElementsSet(targetElementOpt.get(), reachableElements, processedRelations);
            }
        }
    }
}
