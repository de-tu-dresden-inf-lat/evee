package de.tu_dresden.inf.lat.evee.proofs.data;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.ITerm;

import java.util.Objects;

/**
 * @author Christian Alrabbaa
 */
public class Variable implements ITerm{
    private final String variableName;
    private final int coefficient;

    public Variable(String variableName, int coefficient) {
        this.variableName = variableName;
        this.coefficient = coefficient;
    }

    public String getVariableName() {
        return variableName;
    }

    public int getCoefficient() {
        return coefficient;
    }

    @Override
    public String toString() {
        return this.coefficient + this.variableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return coefficient == variable.coefficient &&
                Objects.equals(variableName, variable.variableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, coefficient);
    }
}
