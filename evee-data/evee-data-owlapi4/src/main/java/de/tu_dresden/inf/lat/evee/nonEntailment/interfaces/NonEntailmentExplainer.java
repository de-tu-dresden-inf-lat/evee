package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import java.util.Collection;
import java.util.stream.Stream;

public interface NonEntailmentExplainer<Observation, Abducible, Ontology, Hypothesis> {

    void setObservation(Observation observation);

    void setSignature(Collection<Abducible> abducibles);

    void setOntology(Ontology ontology);

    Stream<Hypothesis> generateHypotheses();

    boolean supportsMultiObservation();

}
