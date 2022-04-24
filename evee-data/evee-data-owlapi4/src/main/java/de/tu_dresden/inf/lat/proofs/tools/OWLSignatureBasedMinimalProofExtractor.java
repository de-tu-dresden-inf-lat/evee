package de.tu_dresden.inf.lat.proofs.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.tu_dresden.inf.lat.proofs.data.Inference;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IRecursiveMeasure;

public class OWLSignatureBasedMinimalProofExtractor extends MinimalProofExtractor<OWLAxiom> {

	public OWLSignatureBasedMinimalProofExtractor(IRecursiveMeasure<OWLAxiom> measure) {
		super(measure);
	}

	public IProof<OWLAxiom> extract(IProof<OWLAxiom> proof, Collection<OWLEntity> knownSignature)
			throws ProofGenerationFailedException {
		Map<OWLAxiom, Double> currentBestProofValues = new HashMap<>();
		Map<OWLAxiom, IInference<OWLAxiom>> currentBestInferences = new HashMap<>();

		System.out.println(knownSignature);

		// all axioms that are completely in the signature get an artificial "proof" of
		// size 1
		if (knownSignature != null) {
			for (OWLAxiom ax : proof.getInferences().stream().map(IInference::getConclusion)
					.collect(Collectors.toList())) {
				if (ax.getSignature().stream().allMatch(knownSignature::contains)) {
					currentBestProofValues.put(ax, measure.leafValue(ax));
					currentBestInferences.put(ax, new Inference<>(ax, "Known", new ArrayList<>()));
					System.out.println("Known: " + ax);
				}
			}
		}

		return dijkstraWrapper(proof, currentBestProofValues, currentBestInferences);
	}

}
