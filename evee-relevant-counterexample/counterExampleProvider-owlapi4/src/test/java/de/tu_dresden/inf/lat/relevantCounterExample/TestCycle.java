package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
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

public class TestCycle {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;

	private static OWLOntology relevantCycle, simple, simple2, testCM;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantCycle = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantCycle.owl")));
		simple = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/simple.owl")));
		simple2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream(
						"ontologies/simple2.owl")));
		testCM = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream(
						"ontologies/testCM.owl")));
	}

	@Test
	public void testCycle() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

//		ModelRefiner refiner = new ModelRefiner(relevantCycle);
//		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);
		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		System.out.println("Diff model");
		typeDiffModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(4, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCycleSimple() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://simple#A")),
				factory.getOWLClass(IRI.create("http://simple#F")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(simple);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(simple, conclusion);
		RelevantCounterExampleGenerator alphaRelGenerator = new AlphaRelevantGenerator(model);

		Set<Element> typeAlphaModel = alphaRelGenerator.generate();

//		ModelRefiner refiner = new ModelRefiner(simple);
//		refiner.refine(alphaRelGenerator, typeAlphaModel, ModelType.Alpha);
		RedundancyRefiner rr = new RedundancyRefiner(typeAlphaModel, alphaRelGenerator);
		rr.refine();


		typeAlphaModel.forEach(System.out::println);

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(3, typeAlphaModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}


	@Test
	public void testCycleSimple2() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://simple2" +
						"#A")),factory.getOWLClass(IRI.create("http://simple2#F")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(simple2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(simple2, conclusion);
		RelevantCounterExampleGenerator alphaRelGenerator = new AlphaRelevantGenerator(model);

		Set<Element> typeAlphaModel = alphaRelGenerator.generate();

//		ModelRefiner refiner = new ModelRefiner(simple2);
//		refiner.refine(alphaRelGenerator, typeAlphaModel, ModelType.Alpha);
		RedundancyRefiner rr = new RedundancyRefiner(typeAlphaModel, alphaRelGenerator);
		rr.refine();

		typeAlphaModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(4, typeAlphaModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCM() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://testCM#A")),
				factory.getOWLClass(IRI.create("http://testCM#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(testCM);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(testCM, conclusion);
		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
