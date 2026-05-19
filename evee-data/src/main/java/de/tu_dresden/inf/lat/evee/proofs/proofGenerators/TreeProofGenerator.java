package de.tu_dresden.inf.lat.evee.proofs.proofGenerators;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalHypergraphProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.OWLSignatureBasedMinimalProofExtractor;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.DepthMeasure;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.OWLAxiomSizeWeightedTreeSizeMeasure;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.TreeSizeMeasure;

/**
 * @author Christian Alrabbaa
 *
 *         TODO: this should be restructured to either use the proof generator
 *         interface(s), or have a different name
 *
 */
public class TreeProofGenerator {

	/**
	 * Return a condensed proof based on the provided signature that is minimal in
	 * weighted tree size, where the weight is the axiom size
	 * 
	 * @param derivationStructure
	 * @param signature
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static IProof<OWLAxiom> getCondensedMinimalWeightedTreeSizeProof(IProof<OWLAxiom> derivationStructure,
			Collection<OWLEntity> signature) throws ProofGenerationFailedException {
		return new OWLSignatureBasedMinimalProofExtractor(new OWLAxiomSizeWeightedTreeSizeMeasure())
				.extract(derivationStructure, signature);
	}

	/**
	 * Return a proof that is minimal in weighted tree size, where the weight is the
	 * axiom size
	 * 
	 * @param derivationStructure
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static IProof<OWLAxiom> getMinimalWeightedTreeSizeProof(IProof<OWLAxiom> derivationStructure)
			throws ProofGenerationFailedException {
		return new MinimalProofExtractor<OWLAxiom>(new OWLAxiomSizeWeightedTreeSizeMeasure())
				.extract(derivationStructure);
	}

	/**
	 * Return a condensed proof based on the provided signature that is minimal in
	 * depth
	 * 
	 * @param derivationStructure
	 * @param signature
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static IProof<OWLAxiom> getCondensedMinimalDepthProof(IProof<OWLAxiom> derivationStructure,
			Collection<OWLEntity> signature) throws ProofGenerationFailedException {
		return new OWLSignatureBasedMinimalProofExtractor(new DepthMeasure<OWLAxiom>()).extract(derivationStructure,
				signature);
	}

	/**
	 * Return a proof that is minimal in depth
	 * 
	 * @param <S>
	 * @param derivationStructure
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static <S> IProof<S> getMinimalDepthProof(IProof<S> derivationStructure)
			throws ProofGenerationFailedException {
		return new MinimalProofExtractor<S>(new DepthMeasure<S>()).extract(derivationStructure);
	}

	/**
	 * Return a condensed proof based on the provided signature that is minimal in
	 * tree size
	 * 
	 * @param derivationStructure
	 * @param signature
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static IProof<OWLAxiom> getCondensedMinimalTreeSizeProof(IProof<OWLAxiom> derivationStructure,
			Collection<OWLEntity> signature) throws ProofGenerationFailedException {
		return new OWLSignatureBasedMinimalProofExtractor(new TreeSizeMeasure<OWLAxiom>()).extract(derivationStructure,
				signature);
	}

	/**
	 * Return a proof that is minimal in tree size
	 * 
	 * @param <S>
	 * @param derivationStructure
	 * @return
	 * @throws ProofGenerationFailedException
	 */
	public static <S> IProof<S> getMinimalTreeSizeProof(IProof<S> derivationStructure)
			throws ProofGenerationFailedException {
		return new MinimalProofExtractor<S>(new TreeSizeMeasure<S>()).extract(derivationStructure);
	}

	/**
	 * Return the tree unravelling of a size minimal hypergraph proof
	 * 
	 * @param <S>
	 * @param derivationStructure
	 * @return
	 */
	public static <S> IProof<S> getTreeUnravelOfMinHypProof(IProof<S> derivationStructure) {
		return generateTreeOfMinimalHypergraph(derivationStructure);
	}

	/**
	 * For the given proof, extract the minimal acyclic proof and return the
	 * corresponding tree proof
	 * 
	 * @param proof
	 * @return
	 */
	private static <S> IProof<S> generateTreeOfMinimalHypergraph(IProof<S> proof) {

		IProof<S> minimal = MinimalHypergraphProofExtractor.makeUnique(proof);

		IProof<S> tree = new Proof<>(minimal.getFinalConclusion());

		populateTree(tree, minimal.getFinalConclusion(), minimal);

		return tree;
	}

	/**
	 * add inferences to the provided tree proof
	 * 
	 * @param tree
	 * @param conclusionAxiom
	 * @param minimalProof
	 */
	private static <S> void populateTree(IProof<S> tree, S conclusionAxiom, IProof<S> minimalProof) {

		minimalProof.getInferences(conclusionAxiom).forEach(inference -> {

			tree.addInference(inference);

			inference.getPremises().forEach(premise -> {

				populateTree(tree, premise, minimalProof);
			});
		});

	}
}
