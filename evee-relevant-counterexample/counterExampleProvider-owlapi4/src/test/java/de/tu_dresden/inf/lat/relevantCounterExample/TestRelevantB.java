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

public class TestRelevantB {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;
	private ELKModelGenerator model;
	private static OWLOntology relevantB;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();

		relevantB = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantB.owl"));

	}

	@Test
	public void test9() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantB#A")),
				factory.getOWLClass(IRI.create("http://relevantB#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantB);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantB, conclusion);
		RelevantCounterExample rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		ModelRefiner refiner = new ModelRefiner(relevantB);
		refiner.refine(rel, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
