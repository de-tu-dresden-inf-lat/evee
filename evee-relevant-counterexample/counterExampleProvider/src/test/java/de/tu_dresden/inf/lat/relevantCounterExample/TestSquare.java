package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExampleGenerator;
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
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/square1.owl")));
		square2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/square2.owl")));
	}

	@Test
	public void testSquare1() throws OWLOntologyCreationException, ModelGenerationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://square1#A")),
				factory.getOWLClass(IRI.create("http://square1#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(square1);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square1, conclusion);

		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);

		System.out.println("FULL MODEL");
		diffRelGenerator.getElkModel().getFinalizedModelElements().forEach(System.out::println);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare2() throws OWLOntologyCreationException, ModelGenerationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://square2#A")),
				factory.getOWLClass(IRI.create("http://square2#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(square2);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square2, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

//		ModelRefiner refiner = new ModelRefiner(square2);
//		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		System.out.println("---");
		typeDiffModel.forEach(System.out::println);

		assertEquals(9, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());//11
		assertEquals(2, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
