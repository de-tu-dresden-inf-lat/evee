package de.tu_dresden.inf.lat.evee.general.tools;

public class Pair<T> {
    private final T first, second;

    public Pair(T first, T second) {
        this.first=first;
        this.second=second;
    }

    public T getFirst(){
        return first;
    }

    public T getSecond(){
        return second;
    }
}
