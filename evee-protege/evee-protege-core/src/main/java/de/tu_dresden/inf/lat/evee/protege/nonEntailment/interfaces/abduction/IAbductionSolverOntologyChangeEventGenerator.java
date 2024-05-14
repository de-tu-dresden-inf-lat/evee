package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction;

import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverOntologyChangeEvent;

public interface IAbductionSolverOntologyChangeEventGenerator {

    void registerOntologyChangeEventListener(IAbductionSolverOntologyChangeEventListener listener);

}
