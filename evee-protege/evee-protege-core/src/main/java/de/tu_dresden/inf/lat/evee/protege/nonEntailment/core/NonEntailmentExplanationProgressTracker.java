package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IExplanationLoadingUIListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationLoadingUIEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

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
        SwingUtilities.invokeLater(() -> {
            this.logger.debug("Message updated: {}", message);
            this.message = message;
            ExplanationLoadingUIEvent event =
                    ExplanationLoadingUIEvent.createUpdateLoadingMessageEvent(
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
                    ExplanationLoadingUIEvent.createSetCurrentProgressEvent(
                            uiProgress));
        });

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
                    ExplanationLoadingUIEvent.createSetMaxProgressEvent(
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
                    ExplanationLoadingUIEvent.createDoneEvent(
                            uiProgress));
        });
    }

}
