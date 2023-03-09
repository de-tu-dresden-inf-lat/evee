package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationCancelledException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import static org.junit.Assert.assertNotNull;

public class EveeProofGenerationThread extends Thread implements IExplanationGenerator<IProof<OWLAxiom>> {

	private final OWLAxiom entailment;
	private final IProofGenerator<OWLAxiom, OWLOntology> proofGenerator;
	private final IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<IProof<OWLAxiom>>>> explanationGenerationListener;
	private IProof<OWLAxiom> result;
	private String errorMessage = "";

	protected final Logger logger = LoggerFactory.getLogger(EveeProofGenerationThread.class);

	public EveeProofGenerationThread(OWLAxiom entailment,
									 IProofGenerator<OWLAxiom, OWLOntology> proofGenerator,
									 IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<IProof<OWLAxiom>>>> explanationGenerationListener) {
		super.setName("Evee Proof Generation Thread");
		this.entailment = entailment;
		this.proofGenerator = proofGenerator;
		this.explanationGenerationListener = explanationGenerationListener;
	}

	public void run() {
		this.logger.debug("Proof generation thread started");
		try {
			if (this.proofGenerator.supportsProof(this.entailment)) {
				this.logger.debug("Proof supported for axiom {}", this.entailment);
				this.result = proofGenerator.getProof(this.entailment);
				assertNotNull(this.result);
				if (proofGenerator.successful()){
					this.logger.debug("Proof generation completed successfully");
					this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
							this, ExplanationEventType.COMPUTATION_COMPLETE));
				}
				else{
					this.logger.debug("Proof generation cancelled, potentially suboptimal proof found");
					this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
							this, ExplanationEventType.COMPUTATION_CANCELLED));
				}
			} else {
				this.logger.debug("Proof NOT supported for axiom {}", this.entailment);
				this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
						this, ExplanationEventType.NOT_SUPPORTED));
			}
		} catch (ProofGenerationCancelledException e) {
			this.logger.debug("Proof generation cancelled, no proof found: ", e);
			this.errorMessage = "Proof generation cancelled, no proof found";
			this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.ERROR));
		} catch (ProofGenerationFailedException e) {
			this.logger.error("Proof generation failed: ", e);
			this.errorMessage = "Proof generation failed: " + e;
			this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.ERROR));
		} catch (Throwable e) {
			this.logger.error("Proof generation failed with error: ", e);
			this.errorMessage = "Error: " + e;
			this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.ERROR));
		}

	}

	@Override
	public IProof<OWLAxiom> getResult() {
		return this.result;
	}

	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}
}
