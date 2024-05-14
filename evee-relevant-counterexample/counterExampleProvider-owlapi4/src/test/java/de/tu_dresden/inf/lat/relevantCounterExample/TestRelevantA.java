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

public class TestRelevantA {

	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;

	private static OWLOntology relevantA;

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		relevantA = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantA.owl")));
	}

	@Test
	public void test8() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create("http://relevantA#A")),
				factory.getOWLClass(IRI.create("http://relevantA#B")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantA);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantA, conclusion);
		RelevantCounterExampleGenerator rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, rel);
		rr.refine();

		System.out.println("Diff model");
		typeDiffModel.forEach(System.out::println);

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());//15
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
