package de.tu_dresden.inf.lat.proofs.interfaces;

public interface IProgressTracker {
    void setMessage(String message);
    void setProgress(long progress);
    long getProgress();
    void setMax(long max);
    void done();

    default void increment() {
        setProgress(getProgress()+1);
    }
}
