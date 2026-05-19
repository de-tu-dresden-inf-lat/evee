package de.tu_dresden.inf.lat.relevantCounterExample;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.counterExample.RefinerMapper;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RefinerMapperTest {

    @Test
    public void test1(){
        OWLDataFactory factory = OWLManager.getOWLDataFactory();

        OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create("r"));
        OWLObjectProperty s = factory.getOWLObjectProperty(IRI.create("s"));

        Element v1  = new Element("v1");
        Element v2  = new Element("v2");
        Element v3  = new Element("v3");
        Element v4  = new Element("v4");
        Element v5  = new Element("v5");

        Relation rel1 = new Relation(r, v1, v2, RelationDirection.Forward);

        Relation rel2 = new Relation(s, v2, v5, RelationDirection.Forward);

        Relation rel3 = new Relation(s, v1, v3, RelationDirection.Forward);

        Relation rel4 = new Relation(r, v3, v5, RelationDirection.Forward);

        Relation rel5 = new Relation(r, v3, v4, RelationDirection.Forward);

        Relation rel6 = new Relation(r, v4, v3, RelationDirection.Forward);

        v1.addRelation(rel1);
        v1.addRelation(rel3);

        v2.addRelation(rel2);

        v3.addRelation(rel4);
        v3.addRelation(rel5);

        v4.addRelation(rel6);

        Set<Element> model = new HashSet<>(Arrays.asList(v1,v2,v3,v4,v5));

        RefinerMapper rm = new RefinerMapper(model);

        System.out.println("r, initial sim");
        print(r, rm.getSim());

        System.out.println("s, initial sim");
        print(s, rm.getSim());

        Assert.assertEquals(rm.getSim().get(r).keySet(), rm.getSim().get(s).keySet());

        System.out.println("r, post");
        print(r, rm.getPost());

        System.out.println("s, post");
        print(s, rm.getPost());

        Assert.assertEquals(rm.getPost().get(r).keySet(), Sets.newHashSet(v1,v3,v4));
        Assert.assertEquals(rm.getPost().get(r).get(v3), Sets.newHashSet(v5,v4));
        Assert.assertEquals(rm.getPost().get(s).keySet(), Sets.newHashSet(v1,v2));

        System.out.println("r, pre");
        print(r, rm.getPre());

        System.out.println("s, pre");
        print(s, rm.getPre());

        Assert.assertEquals(rm.getPre().get(r).keySet(), Sets.newHashSet(v2,v3,v4,v5));
        Assert.assertEquals(rm.getPre().get(s).get(v3), Sets.newHashSet(v1));
        Assert.assertEquals(rm.getPre().get(s).keySet(), Sets.newHashSet(v3,v5));

        System.out.println("r, initial remove");
        print(r, rm.getRemove());

        System.out.println("s, initial remove");
        print(s, rm.getRemove());

        Assert.assertEquals(rm.getRemove().get(r).get(v3),rm.getRemove().get(r).get(v1));
        Assert.assertEquals(rm.getRemove().get(s).get(v2),rm.getRemove().get(s).get(v1));

        rm.computeSimulation();

        System.out.println("r, sim");
        print(r, rm.getSim());

        System.out.println("s, sim");
        print(s, rm.getSim());
    }

    private void print(OWLObjectProperty role, Map<OWLObjectProperty, Map<Element, Set<Element>>> map) {
        map.get(role).forEach((key, value) -> {
            System.out.print(key.getName() + " -> {");
            value.stream().map(Element::getName).forEach(v-> System.out.print(v + ", "));
            System.out.println("}");
        });
    }
}
