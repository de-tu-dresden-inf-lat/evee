package de.tu_dresden.inf.lat.evee.proofs.proofGenerators;

import de.tu_dresden.inf.lat.evee.proofs.data.ProofGeneratorMain;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

/**
 * A decorator for IProofGenerator that additionally extracts the smallest proof
 * (w.r.t. tree size) from the output of the inner IProofGenerator.
 * 
 * @author stefborg
 *
 * @param <SENTENCE> The type of sentences (nodes) in a proof.
 * @param <ONTOLOGY> A collection of sentences.
 */

public class MinimalTreeProofGenerator<SENTENCE, ONTOLOGY> implements IProofGenerator<SENTENCE, ONTOLOGY> {

	private IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator;

	public MinimalTreeProofGenerator(IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator) {
		this.innerGenerator = innerGenerator;
	}

	@Override
	public void setOntology(ONTOLOGY ontology) {
		innerGenerator.setOntology(ontology);
	}

	@Override
	public boolean supportsProof(SENTENCE axiom) {
		return innerGenerator.supportsProof(axiom);
	}

	@Override
	public IProof<SENTENCE> getProof(SENTENCE axiom) throws ProofGenerationException {
		IProof<SENTENCE> proof = innerGenerator.getProof(axiom);
		ProofGeneratorMain.logTime("Generated");
		proof = new MinimalProofExtractor<SENTENCE>(new TreeSizeMeasure<SENTENCE>()).extract(proof);
		ProofGeneratorMain.logTime("Minimized");
		return proof;
	}

	@Override
	public void cancel() {
		innerGenerator.cancel();
	}

	@Override
	public boolean successful() {
		return innerGenerator.successful();
	}

	@Override
	public void addProgressTracker(IProgressTracker progressTracker) {
		innerGenerator.addProgressTracker(progressTracker);
	}
}
