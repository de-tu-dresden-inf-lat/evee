package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class ExplanationLoadingUIEvent {

    private final ExplanationLoadingUIEventType eventType;
    private final String message;
    private final int progressValue;
    private final int progressMaximum;

    public ExplanationLoadingUIEvent(
            ExplanationLoadingUIEventType type,
            String message,
            int progressValue,
            int progressMaximum){
        this.eventType = type;
        this.message = message;
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

    public int getProgressValue(){
        return this.progressValue;
    }

    public int getMaximum(){
        return this.progressMaximum;
    }

    public static ExplanationLoadingUIEvent createGeneralEvent(
            ExplanationLoadingUIEventType type){
        return new ExplanationLoadingUIEvent(type,
                "", 0, 0);
    }

    public static ExplanationLoadingUIEvent createUpdateLoadingMessageEvent(String message){
        return new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_MESSAGE,
                message, 0, 0);
    }

    public static ExplanationLoadingUIEvent createSetMaxProgressEvent(int maxProgress){
        return new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_MAXIMUM,
                "", 0, maxProgress);
    }

    public static ExplanationLoadingUIEvent createSetCurrentProgressEvent(int currentProgress){
        return new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_PROGRESS,
                "", currentProgress, 0);
    }

    public static ExplanationLoadingUIEvent createDoneEvent(int progressMaximum){
        return new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType.EXPLANATION_GENERATION_FINISHED,
                "", progressMaximum, 0);
    }

    public static ExplanationLoadingUIEvent createCancellationEvent(){
        return new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType.EXPLANATION_GENERATION_CANCELLED,
                "", 0, 0);
    }

}
