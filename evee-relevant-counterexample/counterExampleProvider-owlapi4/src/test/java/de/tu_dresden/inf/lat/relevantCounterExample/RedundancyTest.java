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

public class RedundancyTest {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;

	private static OWLOntology redundancyTest1;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		redundancyTest1 = manager.loadOntologyFromOntologyDocument(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/redundancyTest1.owl"));
	}

	@Test
	public void testRedundancyDiff() throws OWLOntologyCreationException {
		System.out.println("RedundancyDiff");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://redundancyTest1#A")),
				factory.getOWLClass(IRI.create("http://redundancyTest1#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(redundancyTest1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(redundancyTest1, conclusion);
		RelevantCounterExample rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		ModelRefiner refiner = new ModelRefiner(redundancyTest1);
		refiner.refine(rel, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(21, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(10, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}