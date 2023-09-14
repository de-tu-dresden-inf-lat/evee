package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.general.interfaces.IFilterable;
import de.tu_dresden.inf.lat.evee.general.interfaces.IHasProgressTracker;
import de.tu_dresden.inf.lat.evee.general.interfaces.IIsCancellable;

import java.util.Collection;
import java.util.stream.Stream;

public interface INonEntailmentExplainer<Observation, Symbol, Ontology, Explanation>
        extends IIsCancellable, IHasProgressTracker, IFilterable {

    void setObservation(Observation observation);

    void setSignature(Collection<Symbol> signature);

    void setOntology(Ontology ontology);

    Stream<Explanation> generateExplanations();

    /**
     * @return True iff this explainer is able to generate an explanation for the observation, signature and ontology
     */
    boolean supportsExplanation();


}
