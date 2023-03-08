package de.tu_dresden.inf.lat.evee.proofs.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GeneralTools {

    public static <I> Set<Set<I>> unions(Collection<Set<Set<I>>> sets){
        if(sets.isEmpty())
            return Collections.emptySet();
        else if (sets.size()==1) {
            return sets.iterator().next();
        } else {
            Set<Set<I>> first = sets.iterator().next();
            sets.remove(first);
            Set<Set<I>> others = unions(sets);
            Set<Set<I>> result = new HashSet<>();
            first.forEach(fromFirst -> {
                others.forEach( fromOther -> {
                    Set<I> union = new HashSet<>(fromFirst);
                    union.addAll(fromOther);
                    result.add(union);
                });
            });
            return result;
        }
    }
}
