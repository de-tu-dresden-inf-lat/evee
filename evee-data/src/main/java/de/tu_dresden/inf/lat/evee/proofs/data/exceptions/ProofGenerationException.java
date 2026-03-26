package de.tu_dresden.inf.lat.evee.proofs.data.exceptions;

public class ProofGenerationException extends Exception {

	public ProofGenerationException(String message) {
		super(message);
	}

	public ProofGenerationException(Throwable cause){
		super(cause);
	}

}
