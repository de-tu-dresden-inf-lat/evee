package de.tu_dresden.inf.lat.evee.concreteDomains.preprocess;

import de.tu_dresden.inf.lat.evee.concreteDomains.CDConstraint;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.extendedDataStructure.ExtendedOntology;
import de.tu_dresden.inf.lat.evee.concreteDomains.tools.Tools;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import scala.collection.JavaConverters;

import java.util.*;

/**
 * @author Christian Alrabbaa
 *
 */

public class RedundancyRemover{
    private RedundancyRemover() {}

    private static class LazyHolder {
        static final RedundancyRemover INSTANCE = new RedundancyRemover();
    }

    public static RedundancyRemover getInstance() {
        return LazyHolder.INSTANCE;
    }
    public <CD_CONSTRAINT extends CDConstraint> ExtendedOntology<CD_CONSTRAINT>
    makeConstraintNamesUnique(ExtendedOntology<CD_CONSTRAINT> extOnt) throws OWLOntologyCreationException {
        System.out.println("Making CD names unique per constraint...");

        OWLObjectMap m = new OWLObjectMap();
        Map<OWLClass, CD_CONSTRAINT> newExtOntMap = new HashMap<>();
        fillMaps(m, newExtOntMap, extOnt.constraintMap());

        OWLAxiomReplacer ar = new OWLAxiomReplacer(m);

        List<OWLAxiom> tmp;

        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        OWLOntology resOnt =  owlManager.createOntology();

        Set<OWLAxiom> nonLogical = extOnt.ontology().getAxioms();
        nonLogical.removeAll(extOnt.ontology().getLogicalAxioms());
        owlManager.addAxioms(resOnt, nonLogical);

        for( OWLAxiom x : extOnt.ontology().getLogicalAxioms()){
            tmp = ar.visit(Collections.singletonList(x));
            assert tmp.size() == 1;
            if(tmp.contains(null))
                owlManager.addAxiom(resOnt, x);
            else
                owlManager.addAxiom(resOnt, tmp.get(0));
        }

        return new ExtendedOntology<>(resOnt, Tools.toScalaImmutableMap(newExtOntMap));
    }

    private <CD_CONSTRAINT extends CDConstraint> void fillMaps(OWLObjectMap owlObjMap,
                                                               Map<OWLClass, CD_CONSTRAINT> newConstraintMap,
                                                              scala.collection.immutable.Map<OWLClass,CD_CONSTRAINT> constraintMap) {
        Map<CD_CONSTRAINT, Set<OWLClass>> mm = new HashMap<>();
        JavaConverters.mapAsJavaMap(constraintMap).forEach((key, value) -> Tools.addToMap(value, key, mm));
        mm.keySet().forEach(k->{
            List<OWLClass> l = new LinkedList<>(mm.get(k));
            if (l.size() > 1){
                for (int i = 1; i < l.size(); i++){
                    owlObjMap.add(l.get(i), l.get(0));
                }
            }
            newConstraintMap.put(l.get(0), k);
        });
    }
}
