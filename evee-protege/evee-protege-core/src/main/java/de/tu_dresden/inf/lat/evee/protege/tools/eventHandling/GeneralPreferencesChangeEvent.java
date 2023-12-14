package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class GeneralPreferencesChangeEvent {

    private final GeneralPreferencesChangeEventType type;

    public GeneralPreferencesChangeEvent(GeneralPreferencesChangeEventType type){
        this.type = type;
    }

    public GeneralPreferencesChangeEventType getType(){
        return this.type;
    }

    public boolean isType(GeneralPreferencesChangeEventType type){
        return this.type.equals(type);
    }

}
