package de.tu_dresden.inf.lat.proofs.proofGenerators;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tu_dresden.inf.lat.proofs.data.AbstractSignatureBasedProofGeneratorDecorator;
import de.tu_dresden.inf.lat.proofs.data.ProofGeneratorMain;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.tools.OWLSignatureBasedMinimalProofExtractor;
import de.tu_dresden.inf.lat.proofs.tools.measures.TreeSizeMeasure;

public class OWLSignatureBasedMinimalTreeProofGenerator
		extends AbstractSignatureBasedProofGeneratorDecorator<OWLEntity, OWLAxiom, OWLOntology> {

	public OWLSignatureBasedMinimalTreeProofGenerator(IProofGenerator<OWLAxiom, OWLOntology> proofGenerator) {
		super(proofGenerator);
	}

	@Override
	public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationException {
		IProof<OWLAxiom> proof = innerGenerator.getProof(axiom);
		ProofGeneratorMain.logTime("Generated");
		proof = new OWLSignatureBasedMinimalProofExtractor(new TreeSizeMeasure<OWLAxiom>()).extract(proof,
				knownSignature);
		ProofGeneratorMain.logTime("Minimized");
		return proof;
	}

	@Override
	public boolean successful() {
		return innerGenerator.successful();
	}
}
