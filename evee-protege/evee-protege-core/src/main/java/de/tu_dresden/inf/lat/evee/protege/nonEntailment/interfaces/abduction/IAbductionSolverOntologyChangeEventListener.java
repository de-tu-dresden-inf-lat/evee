package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction;


import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverOntologyChangeEvent;

public interface IAbductionSolverOntologyChangeEventListener {

    void handleEvent(AbductionSolverOntologyChangeEvent event);

}
