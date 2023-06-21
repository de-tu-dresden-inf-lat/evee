package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.counterExample.LoopTracker;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.Mapper;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;

public class loopTest {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	@BeforeClass
	public static void init() {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
	}

	@Test
	public void test1() {
		Element e = new Element("e");

		Relation r = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e, e, RelationDirection.Bidirectional);
		e.addRelation(r);

		ElkModel m = new ElkModel(Sets.newHashSet(e), new Mapper(factory.getOWLThing(), factory.getOWLThing()));

		LoopTracker tracker = new LoopTracker(m);

		tracker.getElementToLoops().entrySet().forEach(System.out::println);

		//assertEquals(tracker.getElementToLoops().entrySet().iterator().next().getValue().size(), 1);
		assertEquals(tracker.getLoopsStartingWith(e, r).size(), 1);
	}

	@Test
	public void test2() {
		Element e = new Element("e");

		Relation r = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e, e, RelationDirection.Forward);
		e.addRelation(r);

		ElkModel m = new ElkModel(Sets.newHashSet(e), new Mapper(factory.getOWLThing(), factory.getOWLThing()));

		LoopTracker tracker = new LoopTracker(m);

		tracker.getElementToLoops().entrySet().forEach(System.out::println);

		//assertEquals(tracker.getElementToLoops().entrySet().iterator().next().getValue().size(), 1);
		assertEquals(tracker.getLoopsStartingWith(e, r).size(), 1);
	}

	@Test
	public void test3() {
		Element e = new Element("e");

		Relation r = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e, e, RelationDirection.Backward);
		e.addRelation(r);

		ElkModel m = new ElkModel(Sets.newHashSet(e), new Mapper(factory.getOWLThing(), factory.getOWLThing()));

		LoopTracker tracker = new LoopTracker(m);

		tracker.getElementToLoops().entrySet().forEach(System.out::println);

		//assertEquals(tracker.getElementToLoops().entrySet().iterator().next().getValue().size(), 0);
		assertEquals(tracker.getLoopsStartingWith(e, r).size(), 0);
	}

	@Test
	public void test4() {
		Element e1 = new Element("e1");
		Element e2 = new Element("e2");

		Relation r1 = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e1, e2, RelationDirection.Forward);
		Relation s1 = new Relation(factory.getOWLObjectProperty(IRI.create("s")), e1, e2, RelationDirection.Forward);
		Relation r2 = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e2, e1, RelationDirection.Forward);
		Relation r0 = new Relation(factory.getOWLObjectProperty(IRI.create("r")), e1, e1, RelationDirection.Forward);

		e1.addRelation(r0);
		e1.addRelation(r1);
		e1.addRelation(s1);
		e2.addRelation(r2);

		ElkModel m = new ElkModel(Sets.newHashSet(e1, e2), new Mapper(factory.getOWLThing(), factory.getOWLThing()));

		LoopTracker tracker = new LoopTracker(m);

		tracker.getElementToLoops().entrySet().forEach(System.out::println);

	}

}
