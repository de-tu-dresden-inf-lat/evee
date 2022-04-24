import java.io.File;
import java.util.HashSet;

import de.tu_dresden.inf.lat.proofs.tools.evaluators.CorrectnessEvaluator;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.MinimalTreeProofGenerator;

import static org.junit.Assert.assertEquals;

public class DiscoveredBugsTest {

	// Issue @23
	@Test
	public void testCheesyAmerican() throws OWLOntologyCreationException, ProofGenerationException {
		System.out.println("Hello!");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager
				.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"));

		LetheProofGenerator proofGenerator = new LetheProofGenerator();

		proofGenerator.setOntology(ontology);

		IProof<OWLAxiom> proof = proofGenerator.proveSubsumption(
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#American")),
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatyPizza")));

		System.out.println(proof);

		IProofGenerator<OWLAxiom, OWLOntology> proofGenerator2 = new MinimalTreeProofGenerator(proofGenerator);

		IProof<OWLAxiom> proof2 = proofGenerator2.getProof(factory.getOWLSubClassOfAxiom(
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#American")),
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatyPizza"))));

		System.out.println("Minimized proof:");
		System.out.println("=================================");

		System.out.println(proof2);
	}

	// Issue @23
	@Test
	public void testIceCreamEquivBottom() throws OWLOntologyCreationException, ProofGenerationException {
		System.out.println("Hello!");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology ontology = manager
				.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"));

		LetheProofGenerator proofGenerator = new LetheProofGenerator();

		proofGenerator.setOntology(ontology);

		IProof<OWLAxiom> proof = proofGenerator.proveEquivalence(
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream")),
				factory.getOWLNothing());

		System.out.println(proof);

		IProofGenerator<OWLAxiom, OWLOntology> proofGenerator2 = new MinimalTreeProofGenerator(proofGenerator);

		IProof<OWLAxiom> proof2 = proofGenerator2.getProof(factory.getOWLEquivalentClassesAxiom(
				factory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream")),
				factory.getOWLNothing()));

		System.out.println("Minimized proof:");
		System.out.println("=================================");

		System.out.println(proof2);
	}

	// Issue #43
	@Test
	public void testBioportalTask124() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00124.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		System.out.println(generator.getProof(goal));
	}


	// Issue #43
	@Test
	public void testBioportalTask27() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00027.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		System.out.println(generator.getProof(goal));
	}
	// Issue #43
	@Test
	public void testBioportalTask57() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00057.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		System.out.println(generator.getProof(goal)); // needs to treat special case without inferences
	}

	// Issue #43
	@Test
	public void testBioportalTask80() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00080.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		IProof<OWLAxiom> proof = generator.getProof(goal);

		System.out.println(proof); // fix using role monotonicity rule logging

		CorrectnessEvaluator ce = new CorrectnessEvaluator();
		ce.setOntology(ontology.getAxioms());
		ce.setGoalAxiom(goal);
		assertEquals(1.0d, ce.evaluate(proof), 0.1);
	}

	// Issue #43
	@Test
	public void testBioportalTask111() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00111.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		System.out.println(generator.getProof(goal));
	}

	// Issue #43
	@Test
	public void testBioportalTask51() throws OWLOntologyCreationException, ProofGenerationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IInference<OWLAxiom> task = JsonProofParser.getInstance()
				.fromFile(new File(
						Thread.currentThread().getContextClassLoader().getResource("task00051.json").getPath()))
				.getInferences().get(0);

		IProofGenerator<OWLAxiom, OWLOntology> generator = new MinimalTreeProofGenerator<OWLAxiom, OWLOntology>(
				new LetheProofGenerator());

		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));
		generator.setOntology(ontology);

		OWLAxiom goal = task.getConclusion();

		System.out.println(generator.getProof(goal));
	}
}
