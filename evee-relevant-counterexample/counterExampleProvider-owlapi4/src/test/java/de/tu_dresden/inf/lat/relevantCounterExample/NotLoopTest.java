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
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/notLoop.owl")));

	}

	@Test
	public void testNotLoop() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://notLoop#A")),
				factory.getOWLClass(IRI.create("http://notLoop#D")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(notLoop);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(notLoop, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeFlatModel = diffRelGenerator.generate();

//		ModelRefiner refiner = new ModelRefiner(notLoop);
//		refiner.refine(diffRelGenerator, typeFlatModel, ModelType.FlatDiff);

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, diffRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(9, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
