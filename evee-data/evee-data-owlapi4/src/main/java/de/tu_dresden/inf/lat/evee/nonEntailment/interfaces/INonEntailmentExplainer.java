package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import java.util.Collection;
import java.util.stream.Stream;

public interface INonEntailmentExplainer<Observation, Symbol, Ontology, Explanation> {

    void setObservation(Observation observation);

    void setSignature(Collection<Symbol> signature);

    void setOntology(Ontology ontology);

    Stream<Explanation> generateExplanation();

    /**
     * @return True iff this explainer is able to generate an explanation for the observation, symbol and ontology
     */
    boolean supportsExplanation();

}
