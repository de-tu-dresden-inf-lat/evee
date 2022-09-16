package de.tu_dresden.inf.lat.evee.protege.abduction;

public interface AbductionGenerator<Observation, Abducibles, Ontology, Hypothesis> {

    void setObservations(Observation observation);

    void setSignature (Abducibles abducibles);

    void setOntology(Ontology ontology);

    Hypothesis generateAbductions();

}
