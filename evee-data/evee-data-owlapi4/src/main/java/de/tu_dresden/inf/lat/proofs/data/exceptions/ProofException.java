package de.tu_dresden.inf.lat.proofs.data.exceptions;

public class ProofException extends Exception {
    public ProofException(String message) {
        super(message);
    }

    public static class CircularProofException extends ProofException {
        public CircularProofException(String message) {
            super(message);
        }
    }


    public static class IncompleteProofException extends ProofException {
        public IncompleteProofException(String message) {
            super(message);
        }
    }
}
