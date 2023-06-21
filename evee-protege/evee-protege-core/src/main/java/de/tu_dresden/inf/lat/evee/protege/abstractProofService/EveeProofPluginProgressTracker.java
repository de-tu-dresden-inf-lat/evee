package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeProofPluginProgressTracker implements IProgressTracker {


    private final EveeDynamicProofLoadingUI uiWindow;
    private long progress;
    private long maximum;
    private final Logger logger = LoggerFactory.getLogger(EveeProofPluginProgressTracker.class);

    public EveeProofPluginProgressTracker(EveeDynamicProofLoadingUI uiWindow){
        this.progress = 0;
        this.maximum = 0;
        this.uiWindow = uiWindow;
    }

    @Override
    public void setMessage(String s) {
        this.uiWindow.updateMessage(s);
    }

    @Override
    public void setProgress(long i) {
        this.progress = i;
        if (i > Integer.MAX_VALUE){
            this.uiWindow.updateProgress((int)((((double) i) / this.maximum) * Integer.MAX_VALUE));
        }
        else {
            this.uiWindow.updateProgress((int) i);
        }
    }

    @Override
    public long getProgress() {
        return this.progress;
    }

    @Override
    public void setMax(long i) {
        this.maximum = i;
        if (i > Integer.MAX_VALUE){
            this.uiWindow.setupProgressBar(Integer.MAX_VALUE);
        }
        else {
            this.uiWindow.setupProgressBar((int) i);
        }
    }

    @Override
    public void done() {
        this.uiWindow.disposeLoadingScreen();
    }

    @Override
    public void increment() {
        IProgressTracker.super.increment();
    }

}
