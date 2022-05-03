package de.tu_dresden.inf.lat.evee.proofs.lethe;

import java.util.ArrayList;
import java.util.List;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProgressTracker;
import uk.ac.man.cs.lethe.internal.tools.ProgressBar;

public class LetheProgressBarAdapter implements ProgressBar {

    private List<IProgressTracker> progressTrackers = new ArrayList<>();

    private int maxVal = 0;
    private int currentVal = 0;
    private String msg = "";

    public void addProgressTracker(IProgressTracker tracker) {
        progressTrackers.add(tracker);
    }

    @Override
    public int currentValue() {
        return currentVal;
    }

    @Override
    public void currentValue_$eq(int x$1) {
        currentVal = x$1;
        for (IProgressTracker tracker : progressTrackers) {
            tracker.setProgress(currentVal);
        }
    }

    @Override
    public void finish() {
        for (IProgressTracker tracker : progressTrackers) {
            tracker.done();
        }
    }

    @Override
    public int maximum() {
        return maxVal;
    }

    @Override
    public void maximum_$eq(int x$1) {
        maxVal = x$1;
        for (IProgressTracker tracker : progressTrackers) {
            tracker.setMax(maxVal);
        }
    }

    @Override
    public String message() {
        return msg;
    }

    @Override
    public void message_$eq(String x$1) {
        msg = x$1;
    }

    @Override
    public void redraw() {
        for (IProgressTracker tracker : progressTrackers) {
            tracker.setMessage(msg);
        }
    }

    @Override
    public void setPrefix(String prefix) { }
    
}
