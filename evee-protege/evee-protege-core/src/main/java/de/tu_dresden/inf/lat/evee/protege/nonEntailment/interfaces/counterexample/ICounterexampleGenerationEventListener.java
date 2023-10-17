package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample;

public interface ICounterexampleGenerationEventListener {

    void onModelRefreshed(IGraphModelControlPanel source);

    void onModelRecomputed(IGraphModelControlPanel source);

    void onDisjointnessesAddedToOntology(IGraphModelControlPanel source);
}
