package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.BetaRelevantGenerator;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
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

		relevantCycle2 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle2.owl")));

	}

	@Test
	public void testIndependentCycle() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle2#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle2#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle2, conclusion);
		AlphaRelevantGenerator aRelGenerator = new AlphaRelevantGenerator(model);
		BetaRelevantGenerator bRelGenerator = new BetaRelevantGenerator(model);
		DiffRelevantGenerator diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeAModel = aRelGenerator.generate();
		Set<Element> typeBModel = bRelGenerator.generate();
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		assertEquals(15, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());

		RedundancyRefiner rrA = new RedundancyRefiner(typeAModel, aRelGenerator);
		RedundancyRefiner rrB = new RedundancyRefiner(typeBModel, bRelGenerator);
		RedundancyRefiner rrD = new RedundancyRefiner(typeDiffModel, diffRelGenerator);

		rrA.refine();
		rrB.refine();
		rrD.refine();

		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(5, typeDiffModel.size());

		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());

		System.out.println("Type A");
		typeBModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
