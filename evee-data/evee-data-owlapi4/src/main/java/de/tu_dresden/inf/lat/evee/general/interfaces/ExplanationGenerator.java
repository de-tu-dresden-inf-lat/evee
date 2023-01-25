package de.tu_dresden.inf.lat.evee.general.interfaces;

public interface ExplanationGenerator<Result> {

    Result getResult();

    String getErrorMessage();

}
