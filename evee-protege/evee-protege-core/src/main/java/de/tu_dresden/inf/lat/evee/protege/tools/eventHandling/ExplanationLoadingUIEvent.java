package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class ExplanationLoadingUIEvent {

    private ExplanationLoadingUIEventType eventType;
    private String message;
    private String errorMessage;
    private int progressValue;
    private int progressMaximum;

    public ExplanationLoadingUIEvent(
            ExplanationLoadingUIEventType eventType){
        new ExplanationLoadingUIEvent(eventType,
                "", "",
                0, 0);
    }

    public ExplanationLoadingUIEvent(
            ExplanationLoadingUIEventType eventType,
            String message, boolean errorMessage){
        if (errorMessage){
            new ExplanationLoadingUIEvent(
                    eventType, "", message,
                    0, 0);
        } else{
            new ExplanationLoadingUIEvent(
                    eventType, message, "",
                    0, 0);
        }

    }

    public ExplanationLoadingUIEvent(
            ExplanationLoadingUIEventType eventType,
            int newValue, boolean maximumValue){
        if (maximumValue){
            new ExplanationLoadingUIEvent(
                    eventType, "", "",
                    0, newValue);
        } else{
            new ExplanationLoadingUIEvent(
                    eventType,"", ""
                    , newValue, 0);
        }

    }

    public ExplanationLoadingUIEvent(
            ExplanationLoadingUIEventType type,
            String message,
            String errorMessage,
            int progressValue,
            int progressMaximum){
        this.eventType = type;
        this.message = message;
        this.errorMessage = errorMessage;
        this.progressValue = progressValue;
        this.progressMaximum = progressMaximum;
    }

    public ExplanationLoadingUIEventType getType(){
        return this.eventType;
    }

    public boolean isType(ExplanationLoadingUIEventType type){
        return this.eventType.equals(type);
    }

    public String getMessage(){
        return this.message;
    }

    public String getErrorMessage(){
        return this.errorMessage;
    }

    public int getProgressValue(){
        return this.progressValue;
    }

    public int getMaximum(){
        return this.progressMaximum;
    }

}
