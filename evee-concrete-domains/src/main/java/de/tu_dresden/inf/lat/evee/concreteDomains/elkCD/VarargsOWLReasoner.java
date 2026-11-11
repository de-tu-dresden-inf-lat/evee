package de.tu_dresden.inf.lat.evee.concreteDomains.elkCD;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Declares precomputeInferences as a Java varargs method. A Scala class that overrides it does not declare it as such,
 * so that Java code could not call it with several arguments on the type of that class, only on OWLReasoner.
 */
public abstract class VarargsOWLReasoner implements OWLReasoner {

    @Override
    public final void precomputeInferences(InferenceType... inferenceTypes) {
        precompute(inferenceTypes);
    }

    /** precomputes the given inferences, as precomputeInferences */
    protected abstract void precompute(InferenceType[] inferenceTypes);
}
