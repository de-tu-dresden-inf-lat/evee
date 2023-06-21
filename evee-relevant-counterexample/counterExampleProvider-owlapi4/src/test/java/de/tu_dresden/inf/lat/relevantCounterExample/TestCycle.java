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

public class TestCycle {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;

	private static OWLOntology relevantCycle;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantCycle = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantCycle.owl"));
	}

	@Test
	public void testCycle() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantCycle);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(4, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
