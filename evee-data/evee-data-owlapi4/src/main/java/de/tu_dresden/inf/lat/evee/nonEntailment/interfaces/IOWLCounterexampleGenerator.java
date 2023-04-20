package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;

public interface IOWLCounterexampleGenerator extends IOWLNonEntailmentExplainer<OWLIndividualAxiom>, IOWLModelGenerator {
    IRI getRoot();
}
