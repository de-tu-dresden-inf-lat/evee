package de.tu_dresden.inf.lat.evee.proofGenerators.specializedGenerators;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tu_dresden.inf.lat.evee.data.ProofType;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;

/**
 * @author Christian Alrabbaa
 *
 */
public class ESPGMinimalDepth extends ELKSpecializedProofGenerator {

	public ESPGMinimalDepth() {
		super();
	}

	@Override
	public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationFailedException {
		return this.generator.getTreeProof(axiom, ProofType.MinimalDepth, null);
	}

}
