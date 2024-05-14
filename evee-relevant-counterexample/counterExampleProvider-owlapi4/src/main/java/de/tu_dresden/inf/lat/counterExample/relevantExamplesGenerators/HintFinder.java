package de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Alrabbaa
 *
 */

public class HintFinder {

    public static Set<Element> getHint(Set<Element> model, RelevantCounterExampleGenerator generator){
        Element lhs = getElementFrom(generator.getElkModel().getMapper().getLHSRepresentativeElement(), model);
        Element rhs = getElementFrom(generator.getElkModel().getMapper().getRHSRepresentativeElement(), model);

        Set<Element> hint = new HashSet<>();
        Set<Relation> explored = new HashSet<>();

        getHint(rhs, lhs.getRelations(), explored, model, hint);

        return hint;
    }

    private static void getHint(Element rhs, Set<Relation> relations, Set<Relation> explored,
                                        Set<Element> model, Set<Element> hint) {
        for(Relation rhsRel : rhs.getRelations()){
            if(explored.contains(rhsRel))
                continue;
            explored.add(rhsRel);

            Set<Relation> matches =
                    relations.stream().filter(x->rhsRel.getRoleName().equals(x.getRoleName())).collect(Collectors.toSet());
            if(!matches.isEmpty()) {
                Optional<Relation> types =
                        matches.stream().filter(x->rhsRel.getElement2().getTypes().containsAll(x.getElement2().getTypes())).findFirst();
                if (types.isPresent()){
                    for (Relation match : matches){
                        getHint(getElementFrom(rhsRel.getElement2(), model),
                                getElementFrom(match.getElement2(), model).getRelations(),
                                explored, model, hint);
                    }
                }else
                    addHint(rhsRel,hint);
            }else
                addHint(rhsRel,hint);
        }
    }

    private static void addHint(Relation rhsRel, Set<Element> hint) {
        Element nE1 = new Element(rhsRel.getElement1().getName());
        Element nE2 = new Element(rhsRel.getElement2().getName());
        Relation nRF = new Relation(rhsRel.getRoleName(), nE1,nE2, RelationDirection.Forward);
        Relation nRB = new Relation(rhsRel.getRoleName(), nE2,nE1, RelationDirection.Backward);

        nE1.addRelation(nRF);
        nE2.addRelation(nRB);

        hint.add(nE1);
        hint.add(nE2);
    }

    private static Element getElementFrom(Element toFind, Set<Element> elements) {
        return elements.stream().filter(x -> x.equals(toFind)).collect(Collectors.toList()).get(0);
    }
}
