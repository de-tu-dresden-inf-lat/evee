package de.tu_dresden.inf.lat.evee.protege.nonEntailment.service;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.NonEntailmentExplanationEvent;

public interface NonEntailmentExplanationListener {

    void handleEvent(NonEntailmentExplanationEvent event);

}
