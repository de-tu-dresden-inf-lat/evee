package de.tu_dresden.inf.lat.evee.protege.tools.eventHandling;

public class ExplanationLoadingScreenEvent {

    private final ExplanationLoadingUIEventType eventType;
    private final String message;
    private final int progressValue;
    private final int progressMaximum;

    public ExplanationLoadingScreenEvent(
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

    public static ExplanationLoadingScreenEvent createGeneralEvent(
            ExplanationLoadingUIEventType type){
        return new ExplanationLoadingScreenEvent(type,
                "", 0, 0);
    }

    public static ExplanationLoadingScreenEvent createUpdateLoadingMessageEvent(String message){
        return new ExplanationLoadingScreenEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_MESSAGE,
                message, 0, 0);
    }

    public static ExplanationLoadingScreenEvent createSetMaxProgressEvent(int maxProgress){
        return new ExplanationLoadingScreenEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_MAXIMUM,
                "", 0, maxProgress);
    }

    public static ExplanationLoadingScreenEvent createSetCurrentProgressEvent(int currentProgress){
        return new ExplanationLoadingScreenEvent(
                ExplanationLoadingUIEventType.UPDATE_LOADING_PROGRESS,
                "", currentProgress, 0);
    }

    public static ExplanationLoadingScreenEvent createDoneEvent(int progressMaximum){
        return new ExplanationLoadingScreenEvent(
                ExplanationLoadingUIEventType.EXPLANATION_GENERATION_FINISHED,
                "", progressMaximum, 0);
    }

    public static ExplanationLoadingScreenEvent createCancellationEvent(){
        return new ExplanationLoadingScreenEvent(
                ExplanationLoadingUIEventType.EXPLANATION_GENERATION_CANCELLED,
                "", 0, 0);
    }

}
