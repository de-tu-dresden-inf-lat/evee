package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.ModelRefiner;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExample;
import de.tu_dresden.inf.lat.model.data.Element;

public class TestSquare {

	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private static OWLOntology square1, square2;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		square1 = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/square1.owl"));
		square2 = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/square2.owl"));
	}

	// @Test
	public void testSquare1() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://square1#A")),
				factory.getOWLClass(IRI.create("http://square1#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(square1);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square1, conclusion);

		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);

		System.out.println("FULL MODEL");
		diffRelGenerator.getElkModel().getFinalizedModelElements().forEach(System.out::println);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(square1);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare2() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://square2#A")),
				factory.getOWLClass(IRI.create("http://square2#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(square2);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square2, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(square2);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(7, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(2, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
