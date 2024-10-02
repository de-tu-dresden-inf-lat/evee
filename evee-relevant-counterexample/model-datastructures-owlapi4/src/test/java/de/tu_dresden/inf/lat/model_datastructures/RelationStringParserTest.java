package de.tu_dresden.inf.lat.model_datastructures;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tu_dresden.inf.lat.exceptions.ParsingException;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import de.tu_dresden.inf.lat.model.tools.RelationStringParser;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class RelationStringParserTest {
	private static final ToOWLTools oWLTools = ToOWLTools.getInstance();

	@Test
	public void test1() throws ParsingException {
		String relationsStr = "element1 <-roleName2-> element1,element1 -roleName1-> element2,element1 <-roleName3- element3";

		Relation r1 = new Relation(oWLTools.getPropertyName("roleName1"), new Element("element1"),
				new Element("element2"), RelationDirection.Forward);
		Relation r2 = new Relation(oWLTools.getPropertyName("roleName2"), new Element("element1"),
				new Element("element1"), RelationDirection.Bidirectional);
		Relation r3 = new Relation(oWLTools.getPropertyName("roleName3"), new Element("element1"),
				new Element("element3"), RelationDirection.Backward);

		List<Relation> relations = new ArrayList<>(Arrays.asList(r2, r1, r3));
		List<Relation> parsedRelations = new ArrayList<Relation>();

		for (String s : relationsStr.split(",")) {
			System.out.println(RelationStringParser.parse(s));
			parsedRelations.add(RelationStringParser.parse(s));
		}
		for (int i = 0; i < 3; i++) {
			System.out.println("*" + parsedRelations.get(i) + "*");
			System.out.println("*" + relations.get(i) + "*");
			System.out.println(parsedRelations.get(i).equals(relations.get(i)));
			System.out.println(parsedRelations.get(i).getElement2().equals(relations.get(i).getElement2()));
			System.out.println(parsedRelations.get(i).getRoleName().equals(relations.get(i).getRoleName()));
		}

		assertEquals(relations, parsedRelations);
	}

	@Test
	public void test2() throws ParsingException {
		String relationsStr = "e <-r-> e,d -r-> e, d <-r- e";

		Relation r1 = new Relation(oWLTools.getPropertyName("r"), new Element("d"), new Element("e"),
				RelationDirection.Forward);
		Relation r2 = new Relation(oWLTools.getPropertyName("r"), new Element("e"), new Element("e"),
				RelationDirection.Bidirectional);
		Relation r3 = new Relation(oWLTools.getPropertyName("r"), new Element("d"), new Element("e"),
				RelationDirection.Backward);

		List<Relation> relations = new ArrayList<>(Arrays.asList(r2, r1, r3));
		List<Relation> parsedRelations = new ArrayList<Relation>();

		for (String s : relationsStr.split(",")) {
			parsedRelations.add(RelationStringParser.parse(s));
		}
		for (int i = 0; i < 3; i++) {
			System.out.println("*" + parsedRelations.get(i) + "*");
			System.out.println("*" + relations.get(i) + "*");
			System.out.println(parsedRelations.get(i).equals(relations.get(i)));
			System.out.println(parsedRelations.get(i).getElement2().equals(relations.get(i).getElement2()));
			System.out.println(parsedRelations.get(i).getRoleName().equals(relations.get(i).getRoleName()));
		}

		assertEquals(relations, parsedRelations);
	}

	@Test
	public void test3() throws ParsingException {
		String relationsStr = "e<-has---child-> e,d-has--child->e,d<-has--child- e";

		Relation r1 = new Relation(oWLTools.getPropertyName("has--child"), new Element("d"), new Element("e"),
				RelationDirection.Forward);
		Relation r2 = new Relation(oWLTools.getPropertyName("has---child"), new Element("e"), new Element("e"),
				RelationDirection.Bidirectional);
		Relation r3 = new Relation(oWLTools.getPropertyName("has--child"), new Element("d"), new Element("e"),
				RelationDirection.Backward);

		List<Relation> relations = new ArrayList<>(Arrays.asList(r2, r1, r3));
		List<Relation> parsedRelations = new ArrayList<Relation>();

		for (String s : relationsStr.split(",")) {
			parsedRelations.add(RelationStringParser.parse(s));
		}
		for (int i = 0; i < 3; i++) {
			System.out.println("*" + parsedRelations.get(i) + "*");
			System.out.println("*" + relations.get(i) + "*");
			System.out.println(parsedRelations.get(i).equals(relations.get(i)));
			System.out.println(parsedRelations.get(i).getElement2().equals(relations.get(i).getElement2()));
			System.out.println(parsedRelations.get(i).getRoleName().equals(relations.get(i).getRoleName()));
		}

		assertEquals(relations, parsedRelations);
	}

	@Test(expected = ParsingException.class)
	public void test4() throws ParsingException {
		String relationsStr = "<--has-child--> e";
		RelationStringParser.parse(relationsStr);

	}

	@Test(expected = AssertionError.class)
	public void test5() throws ParsingException {
		String relationsStr = "e <-has-child--> e";
		assertEquals(" <-has-child- -> e", RelationStringParser.parse(relationsStr).toString());
	}

	@Test(expected = AssertionError.class)
	public void test6() throws ParsingException {
		String relationsStr = "d ---has-child-> e";
		assertEquals(" -has-child-> -- e", RelationStringParser.parse(relationsStr).toString());

	}

	@Test(expected = AssertionError.class)
	public void test7() throws ParsingException {
		String relationsStr = "d <-has-child-- e";
		assertEquals(" <-has-child- - e", RelationStringParser.parse(relationsStr).toString());

	}

	@Test(expected = ParsingException.class)
	public void test8() throws ParsingException {
		String relationsStr = "d -has-child-- e";
		RelationStringParser.parse(relationsStr);

	}

}
