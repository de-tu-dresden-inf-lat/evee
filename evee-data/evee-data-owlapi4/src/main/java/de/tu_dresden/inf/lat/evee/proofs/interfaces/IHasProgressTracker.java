package de.tu_dresden.inf.lat.evee.proofs.interfaces;

public interface IHasProgressTracker {
    default void addProgressTracker(IProgressTracker tracker) {
        System.out.println("Warning: no progress tracking is defined...");
    };
}
