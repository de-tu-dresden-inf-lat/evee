package de.tu_dresden.inf.lat.evee.proofs.interfaces;

public interface IIsCancellable {
    default void cancel() {
        System.out.println("Warning: cancel() is not implemented...");
    };

    // return true if the last computation was successful
    boolean successful();
}
