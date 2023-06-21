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
import de.tu_dresden.inf.lat.model.data.Element;

public class TestCycle3 {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;
	private static OWLOntology relevantCycle2;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();

		relevantCycle2 = manager.loadOntologyFromOntologyDocument(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle2.owl"));

	}

	@Test
	public void testIndependantCycle() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle2#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle2#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle2, conclusion);
//		AlphaRelevantGenerator aRelgenerator = new AlphaRelevantGenerator(model);
//		BetaRelevantGenerator bRelgenerator = new BetaRelevantGenerator(model);
		DiffRelevantGenerator diffRelgenerator = new DiffRelevantGenerator(model);

//		Set<Element> typeAModel = aRelgenerator.generate();
//		Set<Element> typeBModel = bRelgenerator.generate();
		Set<Element> typeDiffModel = diffRelgenerator.generate();

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		ModelRefiner refiner = new ModelRefiner(relevantCycle2);
		refiner.refine(diffRelgenerator, typeDiffModel, ModelType.Diff);
//		refiner.refine(bRelgenerator, typeBModel, ModelType.Beta);
//		refiner.refine(aRelgenerator, typeAModel, ModelType.Alpha);

		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(5, typeDiffModel.size());

//		assertEquals(7, typeBModel.size());
//		System.out.println("Type B");
//		typeBModel.forEach(System.out::println);
//		assertEquals(3, typeAModel.size());
//		System.out.println("Type A");
//		typeAModel.forEach(System.out::println);
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
