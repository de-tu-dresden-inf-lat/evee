package de.tu_dresden.inf.lat.evee.general.tools;

import de.tu_dresden.inf.lat.evee.general.interfaces.IHasProgressTracker;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;

import java.util.LinkedList;
import java.util.List;

/**
 * Convenience class to simplify handling of progress trackers
 */
public class ProgressTrackerCollection implements IProgressTracker, IHasProgressTracker {

    private final List<IProgressTracker> progressTrackerList = new LinkedList<>();

    private long progress = 0;

    @Override
    public void addProgressTracker(IProgressTracker tracker){
        progressTrackerList.add(tracker);
    }

    @Override
    public void setMessage(String message) {
        progressTrackerList.forEach(x -> x.setMessage(message));
    }

    @Override
    public void setProgress(long progress) {
        this.progress=progress;
        progressTrackerList.forEach(x -> x.setProgress(progress));
    }

    @Override
    public long getProgress() {
        return progress;
    }

    @Override
    public void setMax(long max) {
        progressTrackerList.forEach(x -> x.setMax(max));
    }

    @Override
    public void done() {
        progressTrackerList.forEach(x -> x.done());
    }

    @Override
    public void increment() {
        progress++;
        progressTrackerList.forEach(x -> x.increment()); // do not use default method in case trackers reimplement
    }
}
