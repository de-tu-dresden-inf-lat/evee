package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExample;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class ELKCounterModelTest {

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static final OWLDataFactory factory = OWLManager.getOWLDataFactory();
	private static final ToOWLTools oWLTools = ToOWLTools.getInstance();
	private ELKModelGenerator model;
	private OWLOntology ontology;

	@Ignore("Too large")
	@Test
	public void Test3() throws OWLOntologyCreationException, IOException {
		ontology = manager.loadOntologyFromOntologyDocument(
				ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/full-galen.owl"));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/galen#TemporaryPhenomenon"),
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/galen#SubdivisionOfHeartVentricle"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);

//		model.explainConclusion();
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void Test2() throws OWLOntologyCreationException, IOException {

		ontology = manager.loadOntologyFromOntologyDocument(
				ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/elkTest2.owl"));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(oWLTools.getOWLConceptName("http://test#B"),
				oWLTools.getOWLConceptName("http://test#A"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);

//		model.explainConclusion();
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Test
	public void Test1() throws OWLOntologyCreationException, IOException {

		ontology = manager.loadOntologyFromOntologyDocument(
				ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/elkTest.owl"));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/pizza/elkTest.owl#D"),
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/pizza/elkTest.owl#A"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);

//		model.explainConclusion();
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// TODO
	@Ignore("ignored to build")
	@Test
	public void test1() throws OWLOntologyCreationException {
		OWLOntology pizzaOntology = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/pizza.owl"));

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(
						factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MushroomTopping")),
						factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaTopping"))),
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#SauceTopping")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(pizzaOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(pizzaOntology, conclusion);

		// make sure multiple generations will lead to the same model
		Set<Element> m1 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		Set<Element> m2 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		assertTrue(m1 == m2);

		RelevantCounterExample rel = new AlphaRelevantGenerator(model);
		assertEquals(26, m1.size());
		assertEquals(2, rel.generate().size());

		model = new ELKModelGenerator(pizzaOntology);
		assertEquals(161, model.generateFullRawCanonicalModelElements().size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
