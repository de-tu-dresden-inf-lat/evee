package de.tu_dresden.inf.lat.evee.proofs.interfaces;

import java.util.Collection;
import java.util.stream.Stream;

public interface NonEntailmentExplainer<Observation, Abducible, Ontology, Hypothesis> {

    void setObservation(Observation observation);

    void setAbducibles(Collection<Abducible> abducibles);

    void setOntology(Ontology ontology);

    Stream<Hypothesis> generateHypotheses();

}