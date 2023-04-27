package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class AbductionSolverOntologyChangeEvent {

    private final OntologyChangeEventType type;

    public AbductionSolverOntologyChangeEvent(OntologyChangeEventType type) {
        this.type = type;
    }

    public OntologyChangeEventType getType(){
        return this.type;
    }

    public boolean isType(OntologyChangeEventType type){
        return this.type.equals(type);
    }

}
