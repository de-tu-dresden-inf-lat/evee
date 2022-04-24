package de.tu_dresden.inf.lat.proofs.interfaces;

public interface IHasProgressTracker {
    default void addProgressTracker(IProgressTracker tracker) {
        System.out.println("Warning: no progress tracking is defined...");
    };
}
