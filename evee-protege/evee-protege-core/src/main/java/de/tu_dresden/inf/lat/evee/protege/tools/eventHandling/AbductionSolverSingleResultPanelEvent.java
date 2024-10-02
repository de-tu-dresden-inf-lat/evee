package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class AbductionSolverSingleResultPanelEvent {

    private final SingleResultPanelEventType type;

    public AbductionSolverSingleResultPanelEvent(SingleResultPanelEventType type){
        this.type = type;
    }

    public SingleResultPanelEventType getType(){
        return this.type;
    }

    public boolean isType(SingleResultPanelEventType type){
        return this.type.equals(type);
    }

}
