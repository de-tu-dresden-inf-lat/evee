package de.tu_dresden.inf.lat.evee.proofs.data.exceptions;

// for support that can only be determined during computation
public class ProofNotSupportedException extends ProofGenerationException {

    public ProofNotSupportedException(String message) {
        super(message);
    }

}
