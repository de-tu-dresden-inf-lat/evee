package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.service.NonEntailmentExplanationService;

public class NonEntailmentExplanationEvent {

    private final NonEntailmentExplanationService source;
    private final NonEntailmentExplanationEventType type;

    public NonEntailmentExplanationEvent(NonEntailmentExplanationService source, NonEntailmentExplanationEventType type){
        this.source = source;
        this.type = type;
    }

    public NonEntailmentExplanationService getSource(){
        return this.source;
    }

    public NonEntailmentExplanationEventType getType(){
        return this.type;
    }

    public boolean isType(NonEntailmentExplanationEventType type){
        return this.type.equals(type);
    }

}
