package de.tu_dresden.inf.lat.evee.protege.abduction;

import java.util.Collection;

// todo: file only added to keep compilability when uploading work in progress to GIT; instead of AbductionGenerator, we actually need to use AbductionSolver
public interface AbductionGenerator <Observation, Abducible, Ontology, Hypothesis> {

    void setObservation(Observation observation);

    void setAbducibles(Collection<Abducible> abducibles);

    void setOntology(Ontology ontology);

    Hypothesis generateHypotheses();

}
