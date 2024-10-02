package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction;

import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.AbductionSolverSingleResultPanelEvent;

public interface IAbductionSolverSingleResultPanelEventGenerator {

    void registerSingleResultPanelEventListener(IAbductionSolverSingleResultPanelEventListener listener);

}
