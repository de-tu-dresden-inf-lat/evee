package de.tu_dresden.inf.lat.model_datastructures;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import de.tu_dresden.inf.lat.model.interfaces.IModel;
import de.tu_dresden.inf.lat.model.json.JsonModelParser;
import de.tu_dresden.inf.lat.model.json.JsonModelWriter;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class JSONTest {

	@Test
	public void JSONTest1() throws IOException {
		Element e1 = new Element("e1");
		Element e2 = new Element("e2");
		Element e3 = new Element("e3");
		Element e4 = new Element("e4");
		Element e5 = new Element("e5");
		Element e6 = new Element("e6");
		Element e7 = new Element("e7");

		OWLClass a = ToOWLTools.getInstance().getOWLConceptName("https://test#A");
		OWLClass b = ToOWLTools.getInstance().getOWLConceptName("https://test#B");
		OWLClass c = ToOWLTools.getInstance().getOWLConceptName("https://test#C");
		OWLClass d = ToOWLTools.getInstance().getOWLConceptName("https://test#D");
		OWLClass top = ToOWLTools.getInstance().getOWLTop();

		OWLObjectProperty r = ToOWLTools.getInstance().getPropertyName("https://test#r");
		OWLObjectProperty s = ToOWLTools.getInstance().getPropertyName("https://test#s");

		e1.addType(a);

		e2.addType(b);
		e2.addType(c);

		e3.addType(c);

		e4.addType(top);

		e5.addType(a);
		e5.addType(d);

		e6.addType(a);

		// e1-r->e2
		e1.addRelation(new Relation(r, e1, e2, RelationDirection.Forward));
		e2.addRelation(new Relation(r, e1, e2, RelationDirection.Backward));
		// e2-s->e1
		e2.addRelation(new Relation(s, e2, e1, RelationDirection.Forward));
		e1.addRelation(new Relation(s, e2, e1, RelationDirection.Backward));
		// e1-r->e3
		e1.addRelation(new Relation(r, e1, e3, RelationDirection.Forward));
		e3.addRelation(new Relation(r, e1, e3, RelationDirection.Backward));
		// e1<-s->e7
		e1.addRelation(new Relation(s, e1, e7, RelationDirection.Forward));
		e7.addRelation(new Relation(s, e1, e7, RelationDirection.Backward));
		// e3-r->e6
		e3.addRelation(new Relation(r, e3, e6, RelationDirection.Forward));
		e6.addRelation(new Relation(r, e3, e6, RelationDirection.Backward));
		// e6-s->e1
		e6.addRelation(new Relation(s, e6, e1, RelationDirection.Forward));
		e1.addRelation(new Relation(s, e6, e1, RelationDirection.Backward));
		// e5-r->e3
		e5.addRelation(new Relation(r, e5, e3, RelationDirection.Forward));
		e3.addRelation(new Relation(r, e5, e3, RelationDirection.Backward));
		// e3-s->e4
		e3.addRelation(new Relation(s, e3, e4, RelationDirection.Forward));
		e4.addRelation(new Relation(s, e3, e4, RelationDirection.Backward));

		new JsonModelWriter<Element>().writeToFile(Sets.newHashSet(e1, e2, e3, e4, e5, e6, e7), "testSaveModelJSON");

		System.out.println("-=-=-=-=-=-");

		IModel m = new JsonModelParser().elkModelFromFile(new File("testSaveModelJSON.json"));
		assertEquals(7, m.getFinalizedModelElements().size());
		m.getFinalizedModelElements().forEach(System.out::println);

	}

}
