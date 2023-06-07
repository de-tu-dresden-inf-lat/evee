package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.ModelRefiner;
import de.tu_dresden.inf.lat.counterExample.data.ModelType;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.FlatDiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExample;
import de.tu_dresden.inf.lat.model.data.Element;

public class ExpensiveTests {

	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;

	private ELKModelGenerator model;
	private static OWLOntology ore1, ore2, ore3;
	
	private OWLClass owlClass(String name) {
		return factory.getOWLClass(IRI.create(name));
	}

	private OWLObjectProperty owlProperty(String name) {
		return factory.getOWLObjectProperty(IRI.create(name));
	}

	@BeforeClass
	public static void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ore1 = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_13752.owl"));
		ore2 = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_10742.owl"));
		ore3 = manager.loadOntologyFromOntologyDocument(
				RelevantCounterExamplesTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_8974.owl"));
	}

	@Ignore("Type extender makes it run out of memory")
	// Physical_properties ⊑ ∃related.Pipes
	// http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Physical_properties
	@Test
	public void testTrackCommon() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Physical_properties"),
				factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://www.w3.org/2004/02/skos/core#related"),
						owlClass(
								"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Pipes")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore1);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

		assertEquals(8278, model.generateFullRawCanonicalModelElements().size());
		assertEquals(3641, typeDiffModel.size());// 3643
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("disable print")
	// http://purl.obolibrary.org/obo/XAO_0000032
	@Test
	public void testFilteringEdges() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://purl.obolibrary.org/obo/XAO_0000032"),
				owlClass("http://purl.obolibrary.org/obo/XAO_0000031"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore2, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore2);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

//		assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(97, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("running out of memeory")
	// Soil_erosion ⊑ Bottom_erosion
	@Test
	public void testTrackCommonFlat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Soil_erosion"),
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Bottom_erosion"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExample flatRelGEnerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGEnerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore1);
		refiner.refine(flatRelGEnerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

//		assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3640, typeFlatModel.size());// 3642
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("Stack overflow getMaxPathLength in type extender")
	@Test
	public void testTrackCommon2() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Soil_erosion"),
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Bottom_erosion"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore1);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

//		assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3642, typeDiffModel.size());// 3642
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("bottom should be handled")
	// Helicopters ⊑ Aerial_photography
	// ore_ont_13752.owl

	// TODO some of the concepts are unsatisfiable, this should be checked
	// ore_ont_8974
	// ∃dayOfYear.⊤ ⊑ ∃continent.RELAPPROXC19
	@Test
	public void testDiffAndFlat() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory
				.getOWLSubClassOfAxiom(
						factory.getOWLObjectSomeValuesFrom(
								owlProperty(
										"http://rs.tdwg.org/ontology/voc/OccurrenceRecord#dayOfYear"),
								factory.getOWLThing()),
						factory.getOWLObjectSomeValuesFrom(
								owlProperty(
										"http://rs.tdwg.org/ontology/voc/OccurrenceRecord#continent"),
								owlClass("http://www.absoluteiri.edu/RELAPPROXC19")));

		OWLSubClassOfAxiom firstIsInBot = factory
				.getOWLSubClassOfAxiom(
						factory.getOWLObjectSomeValuesFrom(
								owlProperty(
										"http://rs.tdwg.org/ontology/voc/OccurrenceRecord#continent"),
								owlClass("http://www.absoluteiri.edu/RELAPPROXC19")),
						factory.getOWLNothing());

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore3);

		assertFalse(reasoner.isEntailed(conclusion));
		assertFalse(reasoner.isEntailed(firstIsInBot));

		model = new ELKModelGenerator(ore3, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore3);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

//		assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		refiner.refine(diffRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(3, typeFlatModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("bottom should be handled")
	// TODO some of the concepts are unsatisfiable, this should be checked
	// RELAPPROXC26 ⊑ ∃pointRadiusSpatialFit.RELAPPROXC17
	@Test
	public void testDiffAndFlat2() throws OWLOntologyCreationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.absoluteiri.edu/RELAPPROXC26"),
				factory.getOWLObjectSomeValuesFrom(
						owlProperty(
								"http://rs.tdwg.org/ontology/voc/OccurrenceRecord#pointRadiusSpatialFit"),
						owlClass("http://www.absoluteiri.edu/RELAPPROXC17")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = (ElkReasoner) reasonerFactory.createReasoner(ore3);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore3, conclusion);
		RelevantCounterExample diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		ModelRefiner refiner = new ModelRefiner(ore3);
		refiner.refine(diffRelGenerator, typeDiffModel, ModelType.Diff);

		typeDiffModel.forEach(x -> System.out.println(x));

//			assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		RelevantCounterExample flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		refiner.refine(diffRelGenerator, typeFlatModel, ModelType.FlatDiff);

		typeFlatModel.forEach(x -> System.out.println(x));

		assertEquals(3, typeFlatModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
