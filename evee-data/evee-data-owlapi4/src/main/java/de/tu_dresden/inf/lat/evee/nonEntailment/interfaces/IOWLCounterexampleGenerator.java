package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;

import java.util.Set;

public interface IOWLCounterexampleGenerator extends IOWLNonEntailmentExplainer<OWLIndividualAxiom>, IOWLModelGenerator {
    Set<IRI> getMarkedIndividuals();
}
