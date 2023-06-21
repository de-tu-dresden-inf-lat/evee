package de.tu_dresden.inf.lat.relevantCounterExample;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.ModelRefiner;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.BetaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.FlatDiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExample;
import de.tu_dresden.inf.lat.counterExample.tools.Segmenter;
import de.tu_dresden.inf.lat.model.data.Element;

import static org.junit.Assert.*;

public class RelevantCounterExamplesTest {

	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;
	private static OWLOntology pizzaOntology, mammalOntology, testModule, relevantA, relevantB, relevantC, relevantD,
			relevantE, relevantCycle, relevantCycle2, relevantCycle3, coreTest, ore1, ore2, ore3, redundancyTest1,
			notLoop, square1, square2;
	
	private static OWLClass owlClass(String name) {
		return factory.getOWLClass(IRI.create(name));
	}

	private static OWLObjectProperty owlProperty(String name) {
		return factory.getOWLObjectProperty(IRI.create(name));
	}
	
	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		pizzaOntology = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/pizza.owl")));
		mammalOntology = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantExampleElephant.owl")));
		testModule = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/testModule.owl")));
		relevantA = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantA.owl")));
		relevantB = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantB.owl")));
		relevantC = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantC.owl")));
		relevantD = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantD.owl")));
		relevantE = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantE.owl")));
		relevantCycle = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/relevantCycle.owl")));
		relevantCycle2 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle2.owl")));
		relevantCycle3 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle3.owl")));
		coreTest = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/coreTest.owl")));
		ore1 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_13752.owl")));
		ore2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_10742.owl")));
		ore3 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_8974.owl")));

		redundancyTest1 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader()
				.getResourceAsStream("ontologies/redundancyTest1.owl")));

		notLoop = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/notLoop.owl")));

		square1 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/square1.owl")));
		square2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/square2.owl")));

	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test0() {
		Element e1 = new Element("e91");
		Element e2 = new Element("e91");

		assertEquals(e1, e2);
		assertEquals(e2, e1);
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test00() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://relevantOntologyExample#Mammal"),
				owlClass("http://relevantOntologyExample#Elephant"));

		OWLOntology modOntology = Segmenter.getSegmentAsOntology(mammalOntology,
				Sets.newHashSet((OWLEntity) conclusion.getSubClass(), (OWLEntity) conclusion.getSuperClass()),
				IRI.create(""));

		assertEquals(17, modOntology.getAxiomCount());
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test000() throws OWLOntologyCreationException {

		OWLOntology modOntology = Segmenter.getSegmentAsOntology(testModule, owlClass("http://testModule#A"),
				IRI.create(""));

		Set<OWLAxiom> remainder = Sets.difference(testModule.getAxioms(), modOntology.getAxioms());
		assertEquals(7, modOntology.getAxiomCount());
		assertEquals(2, remainder.size());

		OWLOntology modOntology2 = Segmenter.getSegmentAsOntology(testModule, factory.getOWLNothing(), IRI.create(""));
		assertEquals(modOntology2.getAxiomCount(), testModule.getAxiomCount());
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test1() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(
						owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#MushroomTopping"),
						owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaTopping")),
				owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#SauceTopping"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(pizzaOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(pizzaOntology, conclusion);

		// make sure multiple generations will lead to the same model
		Set<Element> m1 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		Set<Element> m2 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		assertSame(m1, m2);

		RelevantCounterExample aRelGenerator = new AlphaRelevantGenerator(model);
//TODO add a test for the full canonical model
//		assertEquals(162, m1.size());
		assertEquals(26, m1.size());
		assertEquals(2, aRelGenerator.generate().size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test2() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://relevantOntologyExample#Mammal"),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample aRelGenerator = new AlphaRelevantGenerator(model);
		Set<Element> typeAModel = aRelGenerator.generate();
		typeAModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(22, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(15, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeAModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test3() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://relevantOntologyExample#Mammal"),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();
		typeBModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(22, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(15, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test4() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample aRelGenerator = new AlphaRelevantGenerator(model);
		Set<Element> typeAModel = aRelGenerator.generate();
		typeAModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(24, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeAModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test5() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();
		typeBModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(24, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(9, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test6() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(22, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(19, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test6Flat() throws OWLOntologyCreationException {
		System.out.println("6flat");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> flatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(flatRelGenerator, flatModel, ModelType.FlatDiff);

		flatModel.forEach(System.out::println);

		// TODO add a test for the full canonical model
//		assertEquals(22, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(19, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, flatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test66() throws OWLOntologyCreationException {
		System.out.println("test6");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBmodel = bRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(bRelGenerator, typeBmodel, ModelType.Beta);

		typeBmodel.forEach(System.out::println);

		// TODO add a test for the full canonical model
//		assertEquals(22, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(19, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(8, typeBmodel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test55() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(bRelGenerator, typeBModel, ModelType.Beta);

		typeBModel.forEach(System.out::println);

		// TODO add a test for the full canonical model
//		assertEquals(24, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(9, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test7() throws OWLOntologyCreationException {
		System.out.println("test7");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://relevantOntologyExample#livesBy"), factory.getOWLThing()),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(25, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test7Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://relevantOntologyExample#livesBy"), factory.getOWLThing()),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> flatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(mammalOntology);
		refiner.refine(flatRelGenerator, flatModel, ModelType.FlatDiff);

		flatModel.forEach(System.out::println);
		// TODO add a test for the full canonical model
//		assertEquals(25, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, flatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test8() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantA#A"),
				owlClass("http://relevantA#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantA);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantA, conclusion);
		RelevantCounterExample rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		ModelRefiner refiner = new ModelRefiner(relevantA);
		refiner.refine(rel, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(System.out::println);

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test8Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantA#A"),
				owlClass("http://relevantA#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantA);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantA, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantA);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(System.out::println);

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test9() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantB#A"),
				owlClass("http://relevantB#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantB);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantB, conclusion);
		RelevantCounterExample rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		ModelRefiner refiner = new ModelRefiner(relevantB);
		refiner.refine(rel, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(System.out::println);

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test9Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantB#A"),
				owlClass("http://relevantB#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantB);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantB, conclusion);
		RelevantCounterExample rel = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = rel.generate();

		ModelRefiner refiner = new ModelRefiner(relevantB);
		refiner.refine(rel, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(System.out::println);

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("checked after loop tracker, 10.06")
	@Test
	public void test10() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantC#A"),
				owlClass("http://relevantC#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExample difRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = difRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantC);
		refiner.refine(difRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test10Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantC#A"),
				owlClass("http://relevantC#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantC);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void test11() throws OWLOntologyCreationException {
		System.out.println("test11");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantD#A"),
				owlClass("http://relevantD#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantD);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantD, conclusion);
		// model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantD);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void test12() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantE);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void testIndependantCycle() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle2#M"),
				owlClass("http://relevantCycle2#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle2, conclusion);
		AlphaRelevantGenerator aRelgenerator = new AlphaRelevantGenerator(model);
		BetaRelevantGenerator bRelgenerator = new BetaRelevantGenerator(model);
		DiffRelevantGenerator diffRelgenerator = new DiffRelevantGenerator(model);

		Set<Element> typeAModel = aRelgenerator.generate();
		Set<Element> typeBModel = bRelgenerator.generate();
		Set<Element> typeDiffModel = diffRelgenerator.generate();

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		ModelRefiner refiner = new ModelRefiner(relevantCycle2);
		refiner.refine(diffRelgenerator, typeDiffModel, ModelType.Diff);
		refiner.refine(bRelgenerator, typeBModel, ModelType.Beta);
		refiner.refine(aRelgenerator, typeAModel, ModelType.Alpha);

		assertEquals(5, typeDiffModel.size());
		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());
		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());
		System.out.println("Type A");
		typeAModel.forEach(System.out::println);
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void testIndependantCycle2() throws OWLOntologyCreationException {
		System.out.println("---testIndependantCycle2----");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle2#M"),
				owlClass("http://relevantCycle2#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle3);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelB = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelD = new ELKModelGenerator(relevantCycle3, conclusion);
		AlphaRelevantGenerator aRelgenerator = new AlphaRelevantGenerator(model);
		BetaRelevantGenerator bRelgenerator = new BetaRelevantGenerator(modelB);
		DiffRelevantGenerator diffRelgenerator = new DiffRelevantGenerator(modelD);

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		ModelRefiner refiner = new ModelRefiner(relevantCycle3);

		Set<Element> typeAModel = aRelgenerator.generate();
		refiner.refine(aRelgenerator, typeAModel, ModelType.Alpha);
		System.out.println("Type A");
		typeAModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());

		Set<Element> typeBModel = bRelgenerator.generate();
		refiner.refine(bRelgenerator, typeBModel, ModelType.Beta);
		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());

		Set<Element> typeDiffModel = diffRelgenerator.generate();
		refiner.refine(diffRelgenerator, typeDiffModel, ModelType.Diff);
		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(5, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void testCycle() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#M"),
				owlClass("http://relevantCycle#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantCycle);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(4, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void testCycleFlat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#M"),
				owlClass("http://relevantCycle#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);

		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantCycle);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(10, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeFlatModel.size());
		assertEquals(2, typeFlatModel.stream().filter(x -> x.equals(model.getMapper().getLHSRepresentativeElement()))
				.collect(Collectors.toList()).get(0).getRelations().size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Passed after adapting type extender on 13.06")
	@Test
	public void testCycle2() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#MM"),
				owlClass("http://relevantCycle#ZZ"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantCycle);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(9, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void testCycle2Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#MM"),
				owlClass("http://relevantCycle#ZZ"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);

		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantCycle);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(9, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void test13() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantE);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("Fixed on 13.06")
	@Test
	public void testNotLoop() throws OWLOntologyCreationException {
		System.out.println("Not loop");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://notLoop#A"),
				owlClass("http://notLoop#D"));

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

	// @Ignore("passed on 14.06")
	@Test
	public void test13Flat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(relevantE);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed 14.06")
	@Test
	public void testCoreDiff() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://coreTest#A"),
				owlClass("http://coreTest#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(coreTest);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(coreTest, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(coreTest);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void testCoreFlat() throws OWLOntologyCreationException {
		System.out.println("coreFlat");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://coreTest#A"),
				owlClass("http://coreTest#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(coreTest);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(coreTest, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(coreTest);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// @Ignore("passed on 14.06")
	@Test
	public void testRedundancyDiff() throws OWLOntologyCreationException {
		System.out.println("RedundancyDiff");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://redundancyTest1#A"),
				owlClass("http://redundancyTest1#B"));

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

	// @Ignore("passed on 14.06")
	@Test
	public void testRedundancyFlat() throws OWLOntologyCreationException {
		System.out.println("RedundancyDiff");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://redundancyTest1#A"),
				owlClass("http://redundancyTest1#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(redundancyTest1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(redundancyTest1, conclusion);
		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(redundancyTest1);
		refiner.refine(flatRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(21, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(10, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare1() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://square1#A"),
				owlClass("http://square1#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(square1);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square1, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(square1);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare2() throws OWLOntologyCreationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://square2#A"),
				owlClass("http://square2#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(square2);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square2, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(square2);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(7, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(2, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	// ∃hasLengthUnitAbbreviation.⊤ ⊑ RELAPPROXC316
	// http://www.absoluteiri.edu/RELAPPROXC316
	// ////////////////// ////////////// //@Test
//	public void test13() throws OWLOntologyCreationException {
//
//		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
//				owlProperty(
//						"http://www.w3.org/2001/di/Group/Ontologies/DeliveryContext.owl#hasLengthUnitAbbreviation"),
//				factory.getOWLThing()), owlClass("http://www.absoluteiri.edu/RELAPPROXC316"));
//
//		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
//		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(chebi);
//
//		assertFalse(reasoner.isEntailed(conclusion));
//
//		model = new ELKCounterModel(chebi, conclusion);
//		RelevantCounterExample rel = new RelevantCounterExample(model);
//		Set<Element> typeBModel = rel.extractDiffModel();
//		typeBModel.forEach(x -> System.out.println(x));
//
//		assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
//		assertEquals(3, typeBModel.size());
//		System.out.println("_-_-_-_-_-_-_-_-_-_");
//	}
}
