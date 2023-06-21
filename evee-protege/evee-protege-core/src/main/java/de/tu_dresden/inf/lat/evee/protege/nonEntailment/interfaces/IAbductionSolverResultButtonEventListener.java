package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverResultButtonEvent;

public interface IAbductionSolverResultButtonEventListener {

    void handleEvent(AbductionSolverResultButtonEvent event);

}
