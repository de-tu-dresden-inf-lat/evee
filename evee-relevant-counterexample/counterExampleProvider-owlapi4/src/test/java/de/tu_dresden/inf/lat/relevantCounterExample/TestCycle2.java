package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.model.json.JsonMapperWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.BetaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.model.data.Element;

public class TestCycle2 {

	private static OWLDataFactory factory;

	private static OWLOntology relevantCycle3;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantCycle3 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle3.owl")));
	}

	@Test
	public void testIndependentCycle2() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("---testIndependantCycle2----");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantCycle2#M")),
				factory.getOWLClass(IRI.create("http://relevantCycle2#Z")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle3);

		assertFalse(reasoner.isEntailed(conclusion));
//
		ELKModelGenerator modelA = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelB = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelD = new ELKModelGenerator(relevantCycle3, conclusion);
		AlphaRelevantGenerator aRelGenerator = new AlphaRelevantGenerator(modelA);
		BetaRelevantGenerator bRelGenerator = new BetaRelevantGenerator(modelB);
		DiffRelevantGenerator diffRelGenerator = new DiffRelevantGenerator(modelD);

		assertEquals(12, modelD.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());//13

		Set<Element> typeAModel = aRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeAModel, aRelGenerator);
		rr.refine();
		System.out.println("Type A");
		typeAModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());

		Set<Element> typeBModel = bRelGenerator.generate();

		rr = new RedundancyRefiner(typeBModel, bRelGenerator);
		rr.refine();
		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();
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
