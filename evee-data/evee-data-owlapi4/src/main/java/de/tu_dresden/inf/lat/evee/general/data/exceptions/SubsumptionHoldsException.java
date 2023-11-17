package de.tu_dresden.inf.lat.evee.general.data.exceptions;

public class SubsumptionHoldsException extends Exception {
    public SubsumptionHoldsException () {
        super("Axiom is already entailed");
    }
    public SubsumptionHoldsException (String ConceptA, String ConceptB) {
        super(ConceptA + " is subsumed by " + ConceptB);
    }
    public SubsumptionHoldsException (String errorMessage) {
        super(errorMessage);
    }
}
