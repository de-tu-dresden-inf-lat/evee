package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
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
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.BetaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.lat.model.json.JsonMapperWriter;

public class TestCycle2 {

	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator modelA, modelB, modelD;

	private static OWLOntology relevantCycle3;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantCycle3 = manager.loadOntologyFromOntologyDocument(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle3.owl"));
	}

	@Test
	public void testIndependantCycle2() throws OWLOntologyCreationException {
		System.out.println("---testIndependantCycle2----");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle2#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle2#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle3);

		assertFalse(reasoner.isEntailed(conclusion));
//
		modelA = new ELKModelGenerator(relevantCycle3, conclusion);
		modelB = new ELKModelGenerator(relevantCycle3, conclusion);
		modelD = new ELKModelGenerator(relevantCycle3, conclusion);
		AlphaRelevantGenerator aRelgenerator = new AlphaRelevantGenerator(modelA);
		BetaRelevantGenerator bRelgenerator = new BetaRelevantGenerator(modelB);
		DiffRelevantGenerator diffRelgenerator = new DiffRelevantGenerator(modelD);

		assertEquals(10, modelD.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		ModelRefiner refiner = new ModelRefiner(relevantCycle3);

		Set<Element> typeAModel = aRelgenerator.generate();
		refiner.refine(aRelgenerator, typeAModel, ModelType.Alpha);
		System.out.println("Type A");
		typeAModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());

		Set<Element> typeBModel = bRelgenerator.generate();
		refiner.refine(bRelgenerator, typeBModel, ModelType.Beta);
		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());

		Set<Element> typeDiffModel = diffRelgenerator.generate();
		refiner.refine(diffRelgenerator, typeDiffModel, ModelType.Diff);
		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(5, typeDiffModel.size());

		try {
			new JsonMapperWriter().writeToFile(modelA.getMapper(), "mapper");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}
}
