package de.tu_dresden.inf.lat.evee.proofGenerators;

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

import de.tu_dresden.inf.lat.evee.data.ProofType;
import de.tu_dresden.inf.lat.evee.data.RecursiveInference;
import de.tu_dresden.inf.lat.evee.data.RecursiveProof;
import de.tu_dresden.inf.lat.evee.proofs.data.Inference;
import de.tu_dresden.inf.lat.evee.proofs.data.Proof;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ReasonerNotSupportedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.TreeProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.MinimalHypergraphProofExtractor;

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

			String ruleName = inf.getName().equals("Property Domain Transaltion") ? "Property Domain Translation" :
					inf.getName();
			IInference<OWLAxiom> newInf = new Inference<OWLAxiom>(conclusionAxiom, ruleName, inf.getPremises());
			if (!myProof.getInferences().contains(newInf))
				myProof.addInference(newInf);

			for (OWLAxiom x : inf.getPremises()) {
				if (!explored.contains(x)) {
					fillDerivationStructure(myProof, x, proof);
				}
			}
		}
	}

	@Override
	public boolean supportsProof(OWLAxiom axiom) {
		return reasoner.isEntailed(axiom); // since this is elk, this should do the job
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
