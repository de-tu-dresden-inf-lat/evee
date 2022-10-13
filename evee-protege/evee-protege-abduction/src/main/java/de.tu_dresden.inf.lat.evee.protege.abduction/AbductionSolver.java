package de.tu_dresden.inf.lat.evee.protege.abduction;

import java.util.Collection;
import java.util.stream.Stream;

public interface AbductionSolver<Observation, Abducible, Ontology, Hypothesis> {

    void setObservation(Observation observation);

    void setAbducibles(Collection<Abducible> abducibles);

    void setOntology(Ontology ontology);

    Stream<Hypothesis> generateHypotheses();

}
