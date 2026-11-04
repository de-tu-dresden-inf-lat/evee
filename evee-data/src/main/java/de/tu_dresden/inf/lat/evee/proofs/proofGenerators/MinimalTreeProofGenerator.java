package de.tu_dresden.inf.lat.evee.proofs.proofGenerators;

import de.tu_dresden.inf.lat.evee.proofs.data.ProofGeneratorMain;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IRecursiveMeasure;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A decorator for IProofGenerator that additionally extracts the smallest proof
 * (w.r.t. tree size, or another recursive measure) from the output of the inner
 * IProofGenerator.
 * 
 * @author stefborg
 *
 * @param <SENTENCE> The type of sentences (nodes) in a proof.
 * @param <ONTOLOGY> A collection of sentences.
 */

public class MinimalTreeProofGenerator<SENTENCE, ONTOLOGY> implements IProofGenerator<SENTENCE, ONTOLOGY> {

	private static final Logger logger = LoggerFactory.getLogger(MinimalTreeProofGenerator.class);

	private IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator;
	private IRecursiveMeasure<SENTENCE> measure;

	public MinimalTreeProofGenerator(IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator) {
		this(innerGenerator, new TreeSizeMeasure<SENTENCE>());
	}

	/**
	 * Extracts the proof that is smallest w.r.t. the given measure, e.g.
	 * DepthMeasure or OWLAxiomSizeWeightedTreeSizeMeasure.
	 */
	public MinimalTreeProofGenerator(IProofGenerator<SENTENCE, ONTOLOGY> innerGenerator,
			IRecursiveMeasure<SENTENCE> measure) {
		this.innerGenerator = innerGenerator;
		this.measure = measure;
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
		long start = System.nanoTime();
		IProof<SENTENCE> proof = innerGenerator.getProof(axiom);
		ProofGeneratorMain.logTime("Generated");
		long generated = System.nanoTime();
		IProof<SENTENCE> minimal = new MinimalProofExtractor<SENTENCE>(measure).extract(proof);
		ProofGeneratorMain.logTime("Minimized");
		if (logger.isDebugEnabled())
			logger.debug("Minimal proof w.r.t. {}: {} of {} inferences; {} ms to generate the derivation structure, {} ms to minimize",
					measure.getClass().getSimpleName(), minimal.getInferences().size(), proof.getInferences().size(),
					(generated - start) / 1000000, (System.nanoTime() - generated) / 1000000);
		return minimal;
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
