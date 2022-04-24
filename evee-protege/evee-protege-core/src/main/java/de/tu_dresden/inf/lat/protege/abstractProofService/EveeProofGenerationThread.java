package de.tu_dresden.inf.lat.protege.abstractProofService;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationCancelledException;
import de.tu_dresden.inf.lat.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.proofs.interfaces.IProofGenerator;

public class EveeProofGenerationThread extends Thread {

	protected OWLAxiom entailment;
	protected OWLOntology ontology;
	protected OWLReasoner reasoner;
	protected IProofGenerator<OWLAxiom, OWLOntology> proofGenerator;
	protected EveeDynamicProofAdapter proofAdapter;
	protected final Logger logger = LoggerFactory.getLogger(EveeProofGenerationThread.class);

	public EveeProofGenerationThread(OWLAxiom entailment, OWLOntology ontology, OWLReasoner reasoner,
									 IProofGenerator<OWLAxiom, OWLOntology> proofGenerator, EveeDynamicProofAdapter proofAdapter) {
		super.setName("Evee Proof Generation Thread");
		this.entailment = entailment;
		this.ontology = ontology;
		this.reasoner = reasoner;
		this.proofGenerator = proofGenerator;
		this.proofAdapter = proofAdapter;
	}

	public void run() {
		this.logger.debug("Proof generation thread started");
		try {
// todo: for testing purposes only!
//            Thread.sleep(10000);
			if (this.proofGenerator.supportsProof(this.entailment)) {
				this.logger.debug("Proof supported for axiom {}", this.entailment);
				IProof<OWLAxiom> proof = proofGenerator.getProof(this.entailment);
				assert (proof != null);
				if (proofGenerator.successful()){
					this.logger.debug("Proof generation completed successfully");
					this.proofAdapter.proofGenerationSuccess(proof);
				}
				else{
					this.logger.debug("Proof generation cancelled, potentially suboptimal proof found");
					this.proofAdapter.proofGenerationCancelled(proof);
				}
			} else {
				this.logger.debug("Proof NOT supported for axiom {}", this.entailment);
				this.proofAdapter.proofNotSupported();
			}
		} catch (ProofGenerationCancelledException e) {
			this.logger.error("Proof generation cancelled, no proof found: ", e);
			this.proofAdapter.proofGenerationError("Proof generation cancelled, no proof found");
		} catch (ProofGenerationFailedException e) {
			this.logger.error("Proof generation failed: ", e);
			this.proofAdapter.proofGenerationError("Proof generation failed: " + e);
		} catch (Throwable e) {
			this.logger.error("Proof generation failed with error: ", e);
			this.proofAdapter.proofGenerationError("Error: " + e);
		}

	}
}
