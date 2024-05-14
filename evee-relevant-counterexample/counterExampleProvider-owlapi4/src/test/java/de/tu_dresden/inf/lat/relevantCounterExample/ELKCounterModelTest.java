package de.tu_dresden.inf.lat.relevantCounterExample;

import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExampleGenerator;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

import static org.junit.Assert.*;

public class ELKCounterModelTest {

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static final OWLDataFactory factory = OWLManager.getOWLDataFactory();
	private static final ToOWLTools oWLTools = ToOWLTools.getInstance();
	private ELKModelGenerator model;
	private OWLOntology ontology;

	@Ignore("Raw canonical model is computed relatively fast, instantiating relations takes very long")
	@Test
	public void Test3() throws OWLOntologyCreationException {
		ontology = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/full-galen.owl")));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/galen#TemporaryPhenomenon"),
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/galen#SubdivisionOfHeartVentricle"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);
		assertEquals(18021, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void Test2() throws OWLOntologyCreationException {

		ontology = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/elkTest2.owl")));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(oWLTools.getOWLConceptName("http://test#B"),
				oWLTools.getOWLConceptName("http://test#A"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void Test1() throws OWLOntologyCreationException {

		ontology = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(ELKCounterModelTest.class.getClassLoader().getResourceAsStream("ontologies/elkTest.owl")));

		OWLSubClassOfAxiom conclusion = oWLTools.getOWLSubClassOfAxiom(
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/pizza/elkTest.owl#D"),
				oWLTools.getOWLConceptName("http://www.co-ode.org/ontologies/pizza/elkTest.owl#A"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ontology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ontology, conclusion);

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test1() throws OWLOntologyCreationException, ModelGenerationException {
		OWLOntology pizzaOntology = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/pizza.owl")));

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(
						factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MushroomTopping")),
						factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaTopping"))),
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#SauceTopping")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(pizzaOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(pizzaOntology, conclusion);

		// make sure multiple generations will lead to the same model
		Set<Element> m1 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		Set<Element> m2 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		assertSame(m1, m2);

		RelevantCounterExampleGenerator rel = new AlphaRelevantGenerator(model);
		Set<Element> typeAModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeAModel, rel);
		rr.refine();

		assertEquals(28, m1.size());
		assertEquals(2, typeAModel.size());

		model = new ELKModelGenerator(pizzaOntology);
		assertEquals(162, model.generateFullRawCanonicalModelElements().size());//164

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
