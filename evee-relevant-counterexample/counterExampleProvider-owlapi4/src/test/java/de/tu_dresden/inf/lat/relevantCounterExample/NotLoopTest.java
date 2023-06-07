package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;
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

public class NotLoopTest {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;
	private static OWLOntology notLoop;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();

		notLoop = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/notLoop.owl")));

	}

	@Test
	public void testNotLoop() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://notLoop#A")),
				factory.getOWLClass(IRI.create("http://notLoop#D")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(notLoop);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(notLoop, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeFlatModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(notLoop);
		refiner.refine(diffRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(System.out::println);

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
