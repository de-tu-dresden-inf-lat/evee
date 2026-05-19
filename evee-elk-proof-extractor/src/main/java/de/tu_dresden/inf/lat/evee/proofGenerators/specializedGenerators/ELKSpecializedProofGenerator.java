package de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tu_dresden.inf.lat.evee.proofGenerators.ELKProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

/**
 * @author Christian Alrabbaa
 *
 *         Abstraction of every proof service for the Protege plug-in
 */
public abstract class ELKSpecializedProofGenerator implements IProofGenerator<OWLAxiom, OWLOntology> {

	protected final ELKProofGenerator generator;

	public ELKSpecializedProofGenerator() {
		this.generator = new ELKProofGenerator();
	}

	@Override
	public void setOntology(OWLOntology ontology) {
		this.generator.setOntology(ontology);

	}

	@Override
	public boolean supportsProof(OWLAxiom axiom) {
		return generator.supportsProof(axiom);
	}

	@Override
	public void cancel() {
		generator.cancel();
	}

	@Override
	public boolean successful() {
		return generator.successful();
	}
}
