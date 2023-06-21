package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class AbductionSolverResultButtonEvent {

    private final ResultButtonEventType type;

    public AbductionSolverResultButtonEvent(ResultButtonEventType type){
        this.type = type;
    }

    public ResultButtonEventType getType(){
        return this.type;
    }

    public boolean isType(ResultButtonEventType type){
        return this.type.equals(type);
    }

}
