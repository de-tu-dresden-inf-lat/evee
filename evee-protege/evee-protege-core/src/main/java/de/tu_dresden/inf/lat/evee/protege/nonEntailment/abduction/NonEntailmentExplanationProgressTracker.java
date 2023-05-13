package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonEntailmentExplanationProgressTracker implements IProgressTracker,
        IExplanationLoadingUIEventGenerator {

    private String message;
    private long progress;
    private long maximum;
    private IExplanationLoadingUIListener loadingUIListener;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationProgressTracker.class);

    public NonEntailmentExplanationProgressTracker(){
        this.message = "";
        this.progress = 0;
        this.maximum = 0;
        this.logger.debug("Tracker created");
    }


    @Override
    public void setMessage(String message) {
        this.logger.debug("Message updated: {}", message);
        this.message = message;
        this.loadingUIListener.handleUIEvent(
                new ExplanationLoadingUIEvent(
                        ExplanationLoadingUIEventType
                                .UPDATE_LOADING_MESSAGE,
                        this.message, false));
//        this.loadingUIListener.handleUIEvent(
//                new ExplanationLoadingUIEvent(
//                        ExplanationLoadingUIEventType.UPDATE_LOADING_MESSAGE,
//                        this, this.message));
    }

    @Override
    public void setProgress(long progress) {
        this.logger.debug("Progress updated: {}", progress);
        this.progress = progress;
        int uiProgress;
        if (progress > Integer.MAX_VALUE){
            uiProgress = ((int)((((double) progress) / this.maximum) * Integer.MAX_VALUE));
        }
        else {
            uiProgress = (int) progress;
        }
        this.loadingUIListener.handleUIEvent(
                new ExplanationLoadingUIEvent(
                        ExplanationLoadingUIEventType
                                .UPDATE_LOADING_PROGRESS,
                        uiProgress, false));
//        this.loadingUIListener.handleUIEvent(
//                new ExplanationLoadingUIEvent(
//                        ExplanationLoadingUIEventType.UPDATE_LOADING_PROGRESS,
//                        this.message));
    }

    @Override
    public void registerLoadingUIListener(
            IExplanationLoadingUIListener listener) {
        this.loadingUIListener = listener;
    }

    @Override
    public long getProgress() {
        return this.progress;
    }

    @Override
    public void setMax(long max) {
        this.logger.debug("Maximum updated: {}", max);
        this.maximum = max;
        int uiMaximum;
        if (max > Integer.MAX_VALUE){
            uiMaximum = Integer.MAX_VALUE;
        }
        else {
            uiMaximum = (int) max;
        }
        this.loadingUIListener.handleUIEvent(
                new ExplanationLoadingUIEvent(
                ExplanationLoadingUIEventType
                        .UPDATE_LOADING_MAXIMUM,
                uiMaximum, true));
    }

    @Override
    public void done() {
        this.logger.debug("Non entailment explanation computation completed");
        this.progress = this.maximum;
        int uiProgress;
        if (this.progress > Integer.MAX_VALUE){
            uiProgress = Integer.MAX_VALUE;
        } else{
            uiProgress = (int) this.progress;
         }
        this.loadingUIListener.handleUIEvent(
                new ExplanationLoadingUIEvent(
                        ExplanationLoadingUIEventType
                                .EXPLANATION_GENERATION_FINISHED,
                        uiProgress, false));
    }

}
