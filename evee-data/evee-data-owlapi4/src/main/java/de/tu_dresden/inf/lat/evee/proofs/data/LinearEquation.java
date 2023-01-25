package de.tu_dresden.inf.lat.evee.proofs.data;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.ITerm;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IConstraint;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Christian Alrabbaa
 */
public class LinearEquation implements IConstraint {
    private final List<ITerm> rHS, lHS;

    public LinearEquation(List<ITerm> rHS, List<ITerm> lHS){
        this.rHS= rHS;
        this.lHS = lHS;
    }

    @Override
    public String toString() {
        return this.format(this.lHS) + "= " + this.format(this.rHS);
    }

    private String format(List<ITerm> lHS) {
        StringJoiner sb = new StringJoiner(" ");
        boolean first = true;

        for(ITerm t:lHS){
            if (t instanceof Variable)
                sb.add(this.format(((Variable)t).getCoefficient(),first) + ((Variable)t).getVariableName());

            else if (t instanceof Constant)
                sb.add(this.format(((Constant)t).getValue(),first));

            else
                sb.add("UNKNOWN TERM");

            first = false;
        }

        return sb.toString();
    }

    private String format(int value, boolean ignorePositive) {
    if (value < 0 )
        return "- " + Math.abs(value);
    else if(!ignorePositive)
        return "+ " + value;
    return Integer.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinearEquation that = (LinearEquation) o;
        return Objects.equals(rHS, that.rHS) &&
                Objects.equals(lHS, that.lHS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rHS, lHS);
    }
}
