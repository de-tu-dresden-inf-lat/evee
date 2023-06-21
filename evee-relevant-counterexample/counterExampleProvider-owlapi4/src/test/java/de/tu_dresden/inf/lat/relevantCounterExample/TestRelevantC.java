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
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantC.owl"));
	}

	@Test
	public void test10() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantC#A")),
				factory.getOWLClass(IRI.create("http://relevantC#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExample difRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = difRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantC);
		refiner.refine(difRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		difRelGenerator.getElkModel().getMapper().getRestrictionMapper().getClass2Restriction().entrySet()
				.forEach(System.out::println);

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
