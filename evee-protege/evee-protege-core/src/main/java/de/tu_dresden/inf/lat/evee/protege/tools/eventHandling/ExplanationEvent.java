package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;


public class ExplanationEvent<Source> {

    private final Source source;
    private final ExplanationEventType type;

    public ExplanationEvent(Source source, ExplanationEventType type){
        this.source = source;
        this.type = type;
    }

    public Source getSource(){
        return this.source;
    }

    public ExplanationEventType getType(){
        return this.type;
    }

    public boolean isType(ExplanationEventType type){
        return this.type.equals(type);
    }

}
