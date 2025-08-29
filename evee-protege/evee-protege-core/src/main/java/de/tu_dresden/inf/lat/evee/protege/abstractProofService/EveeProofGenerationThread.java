package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationCancelledException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationFailedException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofNotSupportedException;
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
		logger.info("Proof generation thread started");
		try {
			if (!proofGenerator.supportsProof(entailment)) {
				logger.info("Proof NOT supported for axiom {}", entailment);
				explanationGenerationListener.handleEvent(new ExplanationEvent<>(
						this, ExplanationEventType.NOT_SUPPORTED));
				return;
			}

			logger.info("Proof supported for axiom {}", entailment);
			result = proofGenerator.getProof(entailment);
			assertNotNull(result);

			if (!proofGenerator.successful()){
				logger.info("Proof generation cancelled, potentially suboptimal proof found");
				explanationGenerationListener.handleEvent(new ExplanationEvent<>(
						this, ExplanationEventType.COMPUTATION_CANCELLED));
				return;
			}

			logger.info("Proof generation completed successfully");
			explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.COMPUTATION_COMPLETE));
			

		} catch(ProofNotSupportedException e){
			logger.info("Proof NOT supported for axiom {}. Axiom could not be derived", entailment);
			explanationGenerationListener.handleEvent(new ExplanationEvent<>(
						this, ExplanationEventType.NOT_SUPPORTED));
		} catch (ProofGenerationCancelledException e) {
			logger.info("Proof generation cancelled, no proof found: ", e);
			errorMessage = "Proof generation cancelled, no proof found";
			explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.ERROR));
		} catch (ProofGenerationFailedException e) {
			logger.error("Proof generation failed: ", e);
			errorMessage = "Proof generation failed: " + e;
			explanationGenerationListener.handleEvent(new ExplanationEvent<>(
					this, ExplanationEventType.ERROR));
		} catch (Throwable e) {
			logger.error("Proof generation failed with error: ", e);
			errorMessage = "Error: " + e;
			explanationGenerationListener.handleEvent(new ExplanationEvent<>(
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
