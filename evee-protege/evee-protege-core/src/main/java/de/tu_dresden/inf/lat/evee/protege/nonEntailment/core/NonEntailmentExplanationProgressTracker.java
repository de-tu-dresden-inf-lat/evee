package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingScreenEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingScreenEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingScreenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class NonEntailmentExplanationProgressTracker implements IProgressTracker,
        IExplanationLoadingScreenEventGenerator {

    private String message;
    private long progress;
    private long maximum;
    private IExplanationLoadingScreenEventListener loadingUIListener;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationProgressTracker.class);

    public NonEntailmentExplanationProgressTracker(){
        this.message = "";
        this.progress = 0;
        this.maximum = 0;
        this.logger.debug("Tracker created");
    }


    @Override
    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Message updated: {}", message);
            this.message = message;
            ExplanationLoadingScreenEvent event =
                    ExplanationLoadingScreenEvent.createUpdateLoadingMessageEvent(
                            this.message);
            this.loadingUIListener.handleUIEvent(
                    event);
        });
    }

    @Override
    public void setProgress(long progress) {
        SwingUtilities.invokeLater(() -> {
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
                    ExplanationLoadingScreenEvent.createSetCurrentProgressEvent(
                            uiProgress));
        });

    }

    @Override
    public void registerLoadingUIListener(
            IExplanationLoadingScreenEventListener listener) {
        this.loadingUIListener = listener;
    }

    @Override
    public long getProgress() {
        return this.progress;
    }

    @Override
    public void setMax(long max) {
        SwingUtilities.invokeLater(() -> {
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
                    ExplanationLoadingScreenEvent.createSetMaxProgressEvent(
                            uiMaximum));
        });

    }

    @Override
    public void done() {
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Non entailment explanation computation completed");
            this.progress = this.maximum;
            int uiProgress;
            if (this.progress > Integer.MAX_VALUE){
                uiProgress = Integer.MAX_VALUE;
            } else{
                uiProgress = (int) this.progress;
            }
            this.loadingUIListener.handleUIEvent(
                    ExplanationLoadingScreenEvent.createDoneEvent(
                            uiProgress));
        });
    }

}
