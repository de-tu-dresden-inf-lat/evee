package de.tu_dresden.inf.lat.evee.proofs.data;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.ITerm;

import java.util.Objects;

/**
 * @author Christian Alrabbaa
 */
public class Constant implements ITerm {
    private final int value;

    public Constant(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant = (Constant) o;
        return value == constant.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
