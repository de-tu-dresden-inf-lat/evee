package de.tu_dresden.inf.lat.proofGenerators;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.liveontologies.puli.DynamicProof;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.proofs.ElkOwlInference;
import org.semanticweb.elk.owlapi.proofs.ElkOwlProof;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Lists;

import de.tu_dresden.inf.lat.data.ProofType;
import de.tu_dresden.inf.lat.data.RecursiveInference;
import de.tu_dresden.inf.lat.data.RecursiveProof;
import de.tu_dresden.inf.lat.proofs.data.Inference;
import de.tu_dresden.inf.lat.proofs.data.Proof;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ReasonerNotSupportedException;
import de.tu_dresden.inf.lat.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.TreeProofGenerator;
import de.tu_dresden.inf.lat.proofs.tools.MinimalHypergraphProofExtractor;
import de.tu_dresden.inf.lat.tools.AxiomChecker;

/**
 * @author Christian Alrabbaa
 *
 */
public class ELKProofGenerator implements IProofGenerator<OWLAxiom, OWLOntology> {
	// TODO remove the recursive proofs OR adjust the generation of the recursive
	// ones to minimal acyclic

	private Set<OWLAxiom> explored;
	private static final ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();

	private ElkReasoner reasoner;

	/**
	 * Create a proof generator without fixing the ontology.
	 */
	public ELKProofGenerator() {

	}

	/**
	 * Create a proof generator for the given ontology.
	 * 
	 * @param ontology The ontology for which proofs will be generated
	 */
	public ELKProofGenerator(OWLOntology ontology) {
		setOntology(ontology);
	}

	/**
	 *
	 * REMARK: Added by Patrick (made something else more practical)
	 *
	 * @param reasoner Reasoner to be used for proofs
	 */
	public ELKProofGenerator(ElkReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public void setOntology(OWLOntology ont) {
		this.reasoner = reasonerFactory.createReasoner(ont);
	}

//	@Override
	public void setReasoner(OWLReasoner reasoner) throws ReasonerNotSupportedException {
		if (!(reasoner instanceof ElkReasoner))
			throw new ReasonerNotSupportedException("Expected Reasoner of type ElkReasoner");

		this.reasoner = (ElkReasoner) reasoner;
	}

	public ElkReasoner getReasoner() {
		return reasoner;
	}

//	/**
//	 * Extract all proofs provided by elk for the input conclusion, and return the
//	 * result as a list of objects
//	 * <p>
//	 * Note: The result of this function is used to generate an output file in Json
//	 * format
//	 * <p>
//	 * 
//	 * @param conclusionAxiom
//	 * @param proof
//	 * @return {@code List<Object>}
//	 */
//	private static List<Object> getProofsAsJson(OWLAxiom conclusionAxiom, DynamicProof<ElkOwlInference> proof) {
//
//		// get all inferences that led to the conclusion axiom
//		Collection<? extends ElkOwlInference> inferences = proof.getInferences(conclusionAxiom);
//
//		List<Object> proofs = new LinkedList<>();
//
//		for (ElkOwlInference inference : inferences) {
//			explored.add(conclusionAxiom);
//			List<? extends OWLAxiom> premAxioms = inference.getPremises();
//
//			Map<Map<OWLAxiom, String>, Object> inferenceDetails = new HashMap<>();
//			Map<OWLAxiom, String> inferenceName = new HashMap<>();
//
//			List<Object> values = new LinkedList<>();
//
//			for (OWLAxiom x : premAxioms) {
//				if (!explored.contains(x)) {
//					values.addAll(getProofsAsJson(x, proof));
//				}
//			}
//			inferenceName.put(conclusionAxiom, inference.getName());
//			inferenceDetails.put(inferenceName, values);
//
//			proofs.add(inferenceDetails);
//
//		}
//		return proofs;
//	}

	public void resetExploredAxiomsSet() {
		this.explored = new HashSet<>();
	}

	/**
	 * Extract all proofs provided by elk for the input conclusion, and return the
	 * result as a collection of {@link IProof}
	 * <p>
	 * Note: The result of this function is used to generate an output file in Json
	 * format
	 * <p>
	 * 
	 * @param conclusionAxiom
	 * @param proof
	 * @return {@code List<Object>}
	 */
	@SuppressWarnings("unchecked")
	public Collection<IProof<OWLAxiom>> getRecursiveProofs(OWLAxiom conclusionAxiom,
			DynamicProof<ElkOwlInference> proof) {

		// get all inferences that led to the conclusion axiom

		Collection<? extends ElkOwlInference> inferences = proof.getInferences(conclusionAxiom);

		List<IProof<OWLAxiom>> proofs = new LinkedList<>();

		for (ElkOwlInference inference : inferences) {
			explored.add(conclusionAxiom);
			List<? extends OWLAxiom> premAxioms = inference.getPremises();

			RecursiveInference<OWLAxiom> rInf;

			List<IInference<OWLAxiom>> values = new LinkedList<>();

			for (OWLAxiom x : premAxioms) {
				if (!explored.contains(x)) {
					getRecursiveProofs(x, proof).stream().map(IProof::getInferences).forEach(inf -> values.addAll(inf));
				}
			}

			List<RecursiveInference<OWLAxiom>> premise = new LinkedList<>();
			values.stream().map(RecursiveInference.class::cast).forEach(x -> premise.add(x));
			rInf = new RecursiveInference<OWLAxiom>(conclusionAxiom, inference.getName(), premise);

			RecursiveProof<OWLAxiom> myProof = new RecursiveProof<OWLAxiom>(conclusionAxiom);
			myProof.addInference(rInf);
			proofs.add(myProof);

		}
		return proofs;
	}

	/**
	 * Returns a collection of tree proofs of type {@link ProofType}
	 * 
	 * @param proofs
	 * @param type
	 * @return
	 */
//	public Collection<IProof<OWLAxiom>> getTreeProofs(Collection<IProof<OWLAxiom>> proofs, ProofType type) {
//
//		Set<IProof<OWLAxiom>> result = new HashSet<>();
//		proofs.forEach(p -> {
//			result.add(getTreeProof(p, type, null));
//		});
//		return result;
//	}

	/**
	 * Return a collection of signature-based condensed proofs
	 * 
	 * @param proofs
	 * @param signature
	 * @return
	 */
//	public Collection<IProof<OWLAxiom>> getTreeProofs(Collection<IProof<OWLAxiom>> proofs,
//			Collection<OWLEntity> signature) {
//
//		Set<IProof<OWLAxiom>> result = new HashSet<>();
//		proofs.forEach(p -> {
//			result.add(getTreeProof(p, ProofType.CondensedTree, signature));
//		});
//		return result;
//	}

	/**
	 * Extract a tree proof of type {@link ProofType}
	 *
	 * @param type
	 * @param signature
	 * @return
	 */
	public IProof<OWLAxiom> getTreeProof(OWLAxiom axiom, ProofType type, Collection<OWLEntity> signature)
			throws ProofGenerationFailedException {

		IProof<OWLAxiom> derivationStructure = getProof(axiom);

		if (type == ProofType.TreeUnravellingOFMinimalSizeGraph)
			return TreeProofGenerator.getTreeUnravelOfMinHypProof(derivationStructure);

		//

		if (type == ProofType.MinimalTreeSize)
			return TreeProofGenerator.getMinimalTreeSizeProof(derivationStructure);

		if (type == ProofType.CondensedMinimalTreeSize) {
			if (signature == null)
				throw new NullPointerException("Signature is missing!");
			return TreeProofGenerator.getCondensedMinimalTreeSizeProof(derivationStructure, signature);
		}

		//

		if (type == ProofType.MinimalWeightedTreeSize)
			return TreeProofGenerator.getMinimalWeightedTreeSizeProof(derivationStructure);

		if (type == ProofType.CondensedMinimalWeightedTreeSize) {
			if (signature == null)
				throw new NullPointerException("Signature is missing!");
			return TreeProofGenerator.getCondensedMinimalWeightedTreeSizeProof(derivationStructure, signature);
		}

		//

		if (type == ProofType.MinimalDepth)
			return TreeProofGenerator.getMinimalDepthProof(derivationStructure);

		if (type == ProofType.CondensedMinimalDepth) {
			if (signature == null)
				throw new NullPointerException("Signature is missing!");
			return TreeProofGenerator.getCondensedMinimalDepthProof(derivationStructure, signature);
		}

		assert false : "Should not be here!";
		return null;
	}

	/**
	 * Return a minimal acyclic hypergraph proof
	 * 
	 * @param conclusionAxiom
	 * @param proof
	 * @return
	 */
	public IProof<OWLAxiom> getMinimalHyperProof(OWLAxiom conclusionAxiom, DynamicProof<ElkOwlInference> proof) {

		Proof<OWLAxiom> myProof = new Proof<OWLAxiom>(conclusionAxiom);

		fillDerivationStructure(myProof, conclusionAxiom, proof);

		return MinimalHypergraphProofExtractor.makeUnique(myProof);
	}

	// private static int sameBranch = 0;

	/**
	 * Extract all proofs provided by elk for the input conclusion. Proofs are
	 * stored in the provided list of proofs
	 * <p>
	 * Note: The result of this function is used to generate an output file in
	 * human-readable-DL format
	 * <p>
	 * 
	 * @param myProofs
	 * @param myProof
	 * @param conclusionAxiom
	 * @param proof
	 */
//	private void createFlatProofs(List<IProof> myProofs, Proof myProof, OWLAxiom conclusionAxiom,
//			DynamicProof<ElkOwlInference> proof) {
//
//		Collection<? extends ElkOwlInference> inferences = proof.getInferences(conclusionAxiom);
//		List<ElkOwlInference> inferencesList = Lists.newLinkedList(inferences);
//
//		for (int i = 0; i < inferencesList.size(); i++) {
//			explored.add(conclusionAxiom);
//			ElkOwlInference inf = inferencesList.get(i);
//
//			Proof p = new Proof(conclusionAxiom);
//			p.addInferences(myProof.getInferences());
//			myProof.addInference(new Inference(conclusionAxiom, inf.getName(), inf.getPremises()));
//
//			sameBranch += inf.getPremises().size();
//			for (OWLAxiom x : inf.getPremises()) {
//				if (!explored.contains(x)) {
//					createFlatProofs(myProofs, myProof, x, proof);
//				}
//				sameBranch--;
//			}
//
//			if (sameBranch == 0) {
//				myProofs.add(myProof);
//				myProof = new Proof(conclusionAxiom);
//			}
//		}
//	}

	/**
	 * Generate a derivation structure that contains all proofs the provided
	 * conclusion axiom
	 * 
	 * @param conclusionAxiom
	 * @param proof
	 * @return
	 */
	private IProof<OWLAxiom> getDerivationStructure(OWLAxiom conclusionAxiom, DynamicProof<ElkOwlInference> proof) {

		Proof<OWLAxiom> myProof = new Proof<OWLAxiom>(conclusionAxiom);
		fillDerivationStructure(myProof, conclusionAxiom, proof);

		return myProof;
	}

	/**
	 * Extract a sub derivation structure provided by elk rooted in the input
	 * conclusion. The result is accumulated in one proof object
	 * 
	 * @param myProof
	 * @param conclusionAxiom
	 * @param proof
	 */
	private void fillDerivationStructure(Proof<OWLAxiom> myProof, OWLAxiom conclusionAxiom,
			DynamicProof<ElkOwlInference> proof) {

		Collection<? extends ElkOwlInference> inferences = proof.getInferences(conclusionAxiom);
		List<ElkOwlInference> inferencesList = Lists.newLinkedList(inferences);

		for (int i = 0; i < inferencesList.size(); i++) {
			explored.add(conclusionAxiom);

			ElkOwlInference inf = inferencesList.get(i);

			IInference<OWLAxiom> newInf = new Inference<OWLAxiom>(conclusionAxiom, inf.getName(), inf.getPremises());
			if (!myProof.getInferences().contains(newInf))
				myProof.addInference(newInf);

			for (OWLAxiom x : inf.getPremises()) {
				if (!explored.contains(x)) {
					fillDerivationStructure(myProof, x, proof);
				}
			}
		}
	}

//	@Override
//	public IProof<OWLAxiom> proveSubsumption(OWLClass lhs, OWLClass rhs) {
//		OWLAxiom conclusionAxiom = oWLTools.getOWLSubClassOfAxiom(lhs, rhs);
//
//		explored = new HashSet<>();
//
//		DynamicProof<ElkOwlInference> proof = ElkOwlProof.create(reasoner, conclusionAxiom);
//
//		// return getFlatProofs(conclusionAxiom, proof).iterator().next();
//		return getDerivationStructure(conclusionAxiom, proof);
//	}
//
//	public IProof<OWLAxiom> proveSubsumption(OWLClassExpression lhs, OWLClassExpression rhs) {
//		OWLAxiom conclusionAxiom = oWLTools.getOWLSubClassOfAxiom(lhs, rhs);
//
//		explored = new HashSet<>();
//
//		DynamicProof<ElkOwlInference> proof = ElkOwlProof.create(reasoner, conclusionAxiom);
//
//		return getDerivationStructure(conclusionAxiom, proof);
//	}

//	@Override
//	public IProof<OWLAxiom> proveEquivalence(OWLClass lhs, OWLClass rhs) {
//		OWLAxiom conclusionAxiom = oWLTools.getOWLEquivalenceAxiom(lhs, rhs);
//
//		explored = new HashSet<>();
//
//		DynamicProof<ElkOwlInference> proof = ElkOwlProof.create(reasoner, conclusionAxiom);
//
//		// return getFlatProofs(conclusionAxiom, proof).iterator().next();
//		return getDerivationStructure(conclusionAxiom, proof);
//	}
//
//	public IProof<OWLAxiom> proveEquivalence(OWLClassExpression lhs, OWLClassExpression rhs) {
//		OWLAxiom conclusionAxiom = oWLTools.getOWLEquivalenceAxiom(lhs, rhs);
//
//		explored = new HashSet<>();
//
//		DynamicProof<ElkOwlInference> proof = ElkOwlProof.create(reasoner, conclusionAxiom);
//
//		return getDerivationStructure(conclusionAxiom, proof);
//	}

	@Override
	public boolean supportsProof(OWLAxiom axiom) {

		return reasoner.isEntailed(axiom); // since this is elk, this should do the job
		//return AxiomChecker.isInEL(axiom);
	}

	@Override
	public IProof<OWLAxiom> getProof(OWLAxiom axiom) throws ProofGenerationFailedException {
		this.resetExploredAxiomsSet();

		DynamicProof<ElkOwlInference> proof = ElkOwlProof.create(reasoner, axiom);

		if (proof.getInferences(axiom).isEmpty()) {
			throw new ProofGenerationFailedException("No proof for the entailment found by ELK");
		}

		return getDerivationStructure(axiom, proof);
	}

	@Override
	public void cancel() {
		// do nothing, assuming that ELK is fast enough in most cases
	}

	@Override
	public boolean successful() {
		// return true, because we let ELK run through to the end
		return true;
	}
}
