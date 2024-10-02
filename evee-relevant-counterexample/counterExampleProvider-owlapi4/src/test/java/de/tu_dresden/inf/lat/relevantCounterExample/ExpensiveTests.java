package de.tu_dresden.inf.lat.relevantCounterExample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Objects;
import java.util.Set;

import de.tu_dresden.inf.lat.counterExample.RedundancyRefiner;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.counterExample.ELKModelGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.DiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.FlatDiffRelevantGenerator;
import de.tu_dresden.inf.lat.counterExample.relevantExamplesGenerators.RelevantCounterExampleGenerator;
import de.tu_dresden.inf.lat.model.data.Element;

public class ExpensiveTests {

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
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ore1 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_13752.owl")));
		ore2 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_10742.owl")));
		ore3 = manager.loadOntologyFromOntologyDocument(
				Objects.requireNonNull(RelevantCounterExampleGeneratorTest.class.getClassLoader().getResourceAsStream("ontologies/ore_ont_8974.owl")));
	}

	@Ignore("It works now")
	// Physical_properties ⊑ ∃related.Pipes
	// http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Physical_properties
	@Test
	public void testTrackCommon() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Physical_properties"),
				factory.getOWLObjectSomeValuesFrom(
						owlProperty("http://www.w3.org/2004/02/skos/core#related"),
						owlClass(
								"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Pipes")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8280, model.generateFullRawCanonicalModelElements().size());
		assertEquals(3643, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("It works and it is fast")
	// http://purl.obolibrary.org/obo/XAO_0000032
	@Test
	public void testFilteringEdges() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://purl.obolibrary.org/obo/XAO_0000032"),
				owlClass("http://purl.obolibrary.org/obo/XAO_0000031"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore2);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore2, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(232, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(97, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("it works now")
	// Soil_erosion ⊑ Bottom_erosion
	@Test
	public void testTrackCommonFlat() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Soil_erosion"),
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Bottom_erosion"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(8281, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3642, typeFlatModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("It works")
	@Test
	public void testTrackCommon2() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Soil_erosion"),
				owlClass(
						"http://www.ontologydesignpatterns.org/ont/fao/asfa/asfanttbox.owl#Bottom_erosion"));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore1);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore1, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		assertEquals(8281, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3642, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("bottom should be handled")
	// Helicopters ⊑ Aerial_photography
	// ore_ont_13752.owl

	// TODO some of the concepts are unsatisfiable, this should be checked
	// ore_ont_8974
	// ∃dayOfYear.⊤ ⊑ ∃continent.RELAPPROXC19
	@Test
	public void testDiffAndFlat() throws OWLOntologyCreationException, ModelGenerationException {

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
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore3);

		assertFalse(reasoner.isEntailed(conclusion));
		assertFalse(reasoner.isEntailed(firstIsInBot));

		model = new ELKModelGenerator(ore3, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

		//assertEquals(6, model.generateFullRelevantCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(3, typeFlatModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

	@Ignore("bottom should be handled")
	// TODO some of the concepts are unsatisfiable, this should be checked
	// RELAPPROXC26 ⊑ ∃pointRadiusSpatialFit.RELAPPROXC17
	@Test
	public void testDiffAndFlat2() throws OWLOntologyCreationException, ModelGenerationException {

		OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(
				owlClass("http://www.absoluteiri.edu/RELAPPROXC26"),
				factory.getOWLObjectSomeValuesFrom(
						owlProperty(
								"http://rs.tdwg.org/ontology/voc/OccurrenceRecord#pointRadiusSpatialFit"),
						owlClass("http://www.absoluteiri.edu/RELAPPROXC17")));

		ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
		ElkReasoner reasoner = reasonerFactory.createReasoner(ore3);

		assertFalse(reasoner.isEntailed(conclusion));

		model = new ELKModelGenerator(ore3, conclusion);
		RelevantCounterExampleGenerator diffRelGenerator = new DiffRelevantGenerator(model);
		Set<Element> typeDiffModel = diffRelGenerator.generate();

		RedundancyRefiner rr = new RedundancyRefiner(typeDiffModel, diffRelGenerator);
		rr.refine();

		typeDiffModel.forEach(System.out::println);

//			assertEquals(6, model.generateCanonicalModel().getFinalizedModelElements().size());
		assertEquals(3, typeDiffModel.size());
		System.out.println("_-_-_-_-_-_-_-_-_-_");

		RelevantCounterExampleGenerator flatRelGenerator = new FlatDiffRelevantGenerator(model);
		Set<Element> typeFlatModel = flatRelGenerator.generate();

		rr = new RedundancyRefiner(typeFlatModel, flatRelGenerator);
		rr.refine();

		typeFlatModel.forEach(System.out::println);

		assertEquals(3, typeFlatModel.size());

		System.out.println("_-_-_-_-_-_-_-_-_-_");
	}

}
