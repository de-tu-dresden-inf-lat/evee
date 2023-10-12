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

public class TestRelevantC {
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;

	private static OWLOntology relevantC;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantC = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantC.owl")));
	}

	@Test
	public void test10() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantC#A")),
				factory.getOWLClass(IRI.create("http://relevantC#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		typeDiffModel.forEach(System.out::println);
		System.out.println("***");

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
