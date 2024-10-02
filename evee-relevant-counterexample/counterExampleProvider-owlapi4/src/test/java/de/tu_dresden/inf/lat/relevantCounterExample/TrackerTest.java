package de.tu_dresden.inf.lat.relevantCounterExample;

import de.tu_dresden.inf.lat.counterExample.data.Tracker;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

public class TrackerTest {
    static OWLOntologyManager manager;
    static OWLDataFactory factory;

    private static OWLClass owlClass(String name) { return factory.getOWLClass(IRI.create(name));    }
    private static OWLObjectProperty owlProperty(String name) { return factory.getOWLObjectProperty(IRI.create(name)); }
    @BeforeClass
    public static void init() throws OWLOntologyCreationException {
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
    }
    @Test
    public void test1(){
        Tracker t = new Tracker();

        Element e1 = new Element("e1");
        e1.addType(owlClass("A"));

        Element e2 = new Element("e2");
        e2.addType(owlClass("B"));

        Element e3 = new Element("e3");
        e3.addType(owlClass("Top"));

        Relation rf1 = new Relation(owlProperty("r"), e1,e2, RelationDirection.Forward);
        Relation rf2 = new Relation(owlProperty("r"), e1,e2, RelationDirection.Backward);

        Relation sf1 = new Relation(owlProperty("s"), e1,e3, RelationDirection.Forward);
        Relation sf2 = new Relation(owlProperty("s"), e1,e3, RelationDirection.Backward);

        e1.addRelation(rf1);
        e2.addRelation(rf2);

        e1.addRelation(sf1);
        e3.addRelation(sf2);

        t.addToRelationExists(rf1, true);
        t.addToRelationExists(rf2, true);

        Assert.assertTrue(t.checkRelationJustified(rf1));
        Assert.assertEquals(t.checkRelationJustified(rf1), t.checkRelationJustified(rf2));

        t.addToTypesNeededCollector(e1, e2);
        t.addToTypesNeededCollector(e1, e3);

        Set<OWLClassExpression> types = new HashSet<>();
        types.addAll(e2.getTypes());
        types.addAll(e3.getTypes());

        Assert.assertEquals(t.getTypesToKeep(e1), types);

        t.addToTypesImplied(e1, false);
        t.addToTypesImplied(e1,true);
        System.out.println(t.getTypesImplied().get(e1));

        Assert.assertTrue(t.checkTypesImplied(e1));
    }
}
