package de.tu_dresden.inf.lat.proofGenerator;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tu_dresden.inf.lat.proofGenerators.specializedGenerators.ESPGMinimalSize;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.tools.OWLTools;

public class DiscoveredElkBugsTests {

	// Issue #40
	@Test
	public void testPizzaBug() {
		try {

			OWLOntology ontology = OWLManager.createOWLOntologyManager()
					.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"));
			OWLAxiom axiom = OWLTools.odf.getOWLSubClassOfAxiom(
					OWLTools.odf.getOWLClass(
							IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent2")),
					OWLTools.odf.getOWLObjectIntersectionOf(
							OWLTools.odf
									.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza")),
							OWLTools.odf.getOWLObjectAllValuesFrom(
									OWLTools.odf.getOWLObjectProperty(
											IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping")),
									OWLTools.odf.getOWLObjectUnionOf(
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseTopping")),
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#FruitTopping")),
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#HerbSpiceTopping")),
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#NutTopping")),
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#SauceTopping")),
											OWLTools.odf.getOWLClass(IRI.create(
													"http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping"))))));

			IProofGenerator<OWLAxiom, OWLOntology> proofGenerator = new ESPGMinimalSize();
			proofGenerator.setOntology(ontology);
			System.out.println(proofGenerator.getProof(axiom));

		} catch (ProofGenerationFailedException e) {
			// expected outcome, because the axiom is not supported by ELK
			assert true;
		} catch (OWLOntologyCreationException | ProofGenerationException ex) {
			System.err.println(ex);
			assert false;
		}
	}

	// Issue #40
	@Test
	public void testPizzaBug2() {
		try {

			OWLOntology ontology = OWLManager.createOWLOntologyManager()
					.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"));
			OWLAxiom axiom = OWLTools.odf.getOWLEquivalentClassesAxiom(
					OWLTools.odf.getOWLClass(
							IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent1")),
					OWLTools.odf.getOWLClass(
							IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetarianPizzaEquivalent2")));

			IProofGenerator<OWLAxiom, OWLOntology> proofGenerator = new ESPGMinimalSize();
			proofGenerator.setOntology(ontology);
			System.out.println(proofGenerator.getProof(axiom));

		} catch (ProofGenerationFailedException e) {
			// expected outcome, because the proof is not supported by ELK
			assert true;
		} catch (OWLOntologyCreationException | ProofGenerationException ex) {
			System.err.println(ex);
			assert false;
		}
	}

}
