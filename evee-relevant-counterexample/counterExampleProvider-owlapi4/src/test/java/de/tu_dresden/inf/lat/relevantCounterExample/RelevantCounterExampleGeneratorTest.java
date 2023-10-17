package de.tu_dresden.inf.lat.relevantCounterExample;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.AlphaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.BetaRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.FlatDiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExampleGenerator;
import de.tu_dresden.inf.lat.counterExample.tools.Segmenter;
import de.tu_dresden.inf.lat.model.data.Element;

import static org.junit.Assert.*;

public class RelevantCounterExampleGeneratorTest {

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
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/pizza.owl")));
		mammalOntology = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantExampleElephant.owl")));
		testModule = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/testModule.owl")));
		relevantA = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantA.owl")));
		relevantB = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantB.owl")));
		relevantC = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantC.owl")));
		relevantD = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantD.owl")));
		relevantE = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantE.owl")));
		relevantCycle = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/relevantCycle.owl")));
		relevantCycle2 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle2.owl")));
		relevantCycle3 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/relevantCycle3.owl")));
		coreTest = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/coreTest.owl")));
		ore1 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_13752.owl")));
		ore2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_10742.owl")));
		ore3 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_8974.owl")));

		redundancyTest1 = manager.loadOntologyFromOntologyDocument(Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader()
				.getResourceAsStream("ontologies/redundancyTest1.owl")));

		notLoop = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/notLoop.owl")));

		square1 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/square1.owl")));
		square2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/square2.owl")));

	}

	@Test
	public void test0() {
		Element e1 = new Element("e91");
		Element e2 = new Element("e91");

		assertEquals(e1, e2);
		assertEquals(e2, e1);
	}

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

	@Test
	public void test1() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(
						owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#MushroomTopping"),
						owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaTopping")),
				owlClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#SauceTopping"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(pizzaOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(pizzaOntology, conclusion);

		// make sure multiple generations will lead to the same model
		Set<Element> m1 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		Set<Element> m2 = model.generateFullRelevantCanonicalModel().getFinalizedModelElements();
		assertSame(m1, m2);

		RelevantCounterExampleGenerator aRelGenerator = new AlphaRelevantGenerator(model);
		Set<Element> typeAModel = aRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeAModel, aRelGenerator);
		rr.refine();

		assertEquals(28, m1.size());
		assertEquals(2, typeAModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test2() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://relevantOntologyExample#Mammal"),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator aRelGenerator = new AlphaRelevantGenerator(model);
		Set<Element> typeAModel = aRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeAModel, aRelGenerator);
		rr.refine();

		typeAModel.forEach(System.out::println);

		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeAModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test3() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://relevantOntologyExample#Mammal"),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeBModel, bRelGenerator);
		rr.refine();

		typeBModel.forEach(System.out::println);

		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test4() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator aRelGenerator = new AlphaRelevantGenerator(model);

		Set<Element> typeAModel = aRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeAModel, aRelGenerator);
		rr.refine();

		typeAModel.forEach(System.out::println);

		assertEquals(24, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeAModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}


	@Test
	public void test5() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeBModel, bRelGenerator);
		rr.refine();

		typeBModel.forEach(System.out::println);

		assertEquals(24, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(9, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}


	@Test
	public void test6() throws OWLOntologyCreationException, ModelGenerationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(27, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}


	@Test
	public void test6Flat() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("6flat");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> flatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(flatModel, flatRelGenerator);
		rr.refine();

		flatModel.forEach(System.out::println);


		assertEquals(27, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, flatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}


	@Test
	public void test66() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("test66");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(owlClass("http://relevantOntologyExample#Mammal"),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeBModel, bRelGenerator);
		rr.refine();

		typeBModel.forEach(System.out::println);

		assertEquals(27, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(8, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test55() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(
				owlProperty("http://relevantOntologyExample#livesBy"),
				factory.getOWLObjectSomeValuesFrom(owlProperty("http://relevantOntologyExample#has"),
						owlClass("http://relevantOntologyExample#Skin"))),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator bRelGenerator = new BetaRelevantGenerator(model);
		Set<Element> typeBModel = bRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeBModel, bRelGenerator);
		rr.refine();

		typeBModel.forEach(System.out::println);

		assertEquals(24, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(9, typeBModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		assertEquals(31,
				new ELKModelGenerator(mammalOntology,
						ToOWLTools.getInstance().getOWLSubClassOfAxiom(ToOWLTools.getInstance().getOWLBot(),
								ToOWLTools.getInstance().getOWLTop())).generateFullRawCanonicalModelElements().size());

	}

	@Test
	public void test7() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("test7");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://relevantOntologyExample#livesBy"), factory.getOWLThing()),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(29, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(7, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		assertEquals(31,
				new ELKModelGenerator(mammalOntology,
						ToOWLTools.getInstance().getOWLSubClassOfAxiom(ToOWLTools.getInstance().getOWLBot(),
								ToOWLTools.getInstance().getOWLTop())).generateFullRawCanonicalModelElements().size());
	}

	@Test
	public void test7Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				factory.getOWLObjectIntersectionOf(factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://relevantOntologyExample#livesBy"), factory.getOWLThing()),
						owlClass("http://relevantOntologyExample#Female")),
				owlClass("http://relevantOntologyExample#Elephant"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(mammalOntology);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(mammalOntology, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> flatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(flatModel, flatRelGenerator);
		rr.refine();

		flatModel.forEach(System.out::println);

		assertEquals(29, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, flatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test8() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantA#A"),
				owlClass("http://relevantA#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantA);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantA, conclusion);
		RelevantCounterExampleGenerator rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, rel);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(15, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test8Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantA#A"),
				owlClass("http://relevantA#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantA);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantA, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(15, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test9() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantB#A"),
				owlClass("http://relevantB#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantB);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantB, conclusion);
		RelevantCounterExampleGenerator rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, rel);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test9Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantB#A"),
				owlClass("http://relevantB#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantB);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantB, conclusion);
		RelevantCounterExampleGenerator rel = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, rel);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test10() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantC#A"),
				owlClass("http://relevantC#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test10Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantC#A"),
				owlClass("http://relevantC#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantC);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantC, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(17, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test11() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("test11");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantD#A"),
				owlClass("http://relevantD#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantD);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantD, conclusion);
		// model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test12() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testIndependentCycle() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle2#M"),
				owlClass("http://relevantCycle2#Z"));

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

	@Test
	public void testIndependentCycle2() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("---testIndependentCycle2----");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle2#M"),
				owlClass("http://relevantCycle2#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle3);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelB = new ELKModelGenerator(relevantCycle3, conclusion);
		ELKModelGenerator modelD = new ELKModelGenerator(relevantCycle3, conclusion);
		AlphaRelevantGenerator aRelGenerator = new AlphaRelevantGenerator(model);
		BetaRelevantGenerator bRelGenerator = new BetaRelevantGenerator(modelB);
		DiffRelevantGenerator diffRelGenerator = new DiffRelevantGenerator(modelD);

		assertEquals(13, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());

		Set<Element> typeAModel = aRelGenerator.generate();
		RedundancyRefiner rrA = new RedundancyRefiner(typeAModel, aRelGenerator);
		rrA.refine();

		System.out.println("Type A");
		typeAModel.forEach(System.out::println);
		assertEquals(3, typeAModel.size());

		Set<Element> typeBModel = bRelGenerator.generate();
		RedundancyRefiner rrB = new RedundancyRefiner(typeBModel, bRelGenerator);
		rrB.refine();

		System.out.println("Type B");
		typeBModel.forEach(System.out::println);
		assertEquals(7, typeBModel.size());

		Set<Element> typeDiffModel = diffRelGenerator.generate();
		RedundancyRefiner rrDiff = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rrDiff.refine();

		System.out.println("Diff");
		typeDiffModel.forEach(System.out::println);
		assertEquals(5, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCycle() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#M"),
				owlClass("http://relevantCycle#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
//		model.generateFullRelevantCanonicalModel().getFinalizedModelElements().forEach(System.out::println);
		assertEquals(4, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCycleFlat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#M"),
				owlClass("http://relevantCycle#Z"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);

		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeFlatModel.size());
		assertEquals(2, typeFlatModel.stream().filter(x -> x.equals(model.getMapper().getLHSRepresentativeElement()))
				.collect(Collectors.toList()).get(0).getRelations().size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCycle2() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#MM"),
				owlClass("http://relevantCycle#ZZ"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);

		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(12, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCycle2Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantCycle#MM"),
				owlClass("http://relevantCycle#ZZ"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(relevantCycle);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantCycle, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);

		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(12, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test13() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testNotLoop() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("Not loop");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://notLoop#A"),
				owlClass("http://notLoop#D"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(notLoop);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(notLoop, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeFlatModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, diffRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(9, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void test13Flat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://relevantE#A"),
				owlClass("http://relevantE#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(relevantE);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(relevantE, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCoreDiff() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://coreTest#A"),
				owlClass("http://coreTest#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(coreTest);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(coreTest, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(6, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testCoreFlat() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("coreFlat");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://coreTest#A"),
				owlClass("http://coreTest#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(coreTest);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(coreTest, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(16, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(5, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testRedundancyDiff() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("RedundancyDiff");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://redundancyTest1#A"),
				owlClass("http://redundancyTest1#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(redundancyTest1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(redundancyTest1, conclusion);
		RelevantCounterExampleGenerator rel = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = rel.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, rel);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(10, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testRedundancyFlat() throws OWLOntologyCreationException, ModelGenerationException {
		System.out.println("RedundancyDiff");
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://redundancyTest1#A"),
				owlClass("http://redundancyTest1#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(redundancyTest1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(redundancyTest1, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(22, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(10, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare1() throws OWLOntologyCreationException, ModelGenerationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://square1#A"),
				owlClass("http://square1#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(square1);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square1, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(4, typeDiffModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Test
	public void testSquare2() throws OWLOntologyCreationException, ModelGenerationException {
		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(owlClass("http://square2#A"),
				owlClass("http://square2#B"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(square2);

		assertFalse(reasoner.isEntailed(conclusion));

		ELKModelGenerator model = new ELKModelGenerator(square2, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(11, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
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
