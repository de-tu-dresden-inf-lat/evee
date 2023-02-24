package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface IOWLModelGenerator {
    void setOntology(OWLOntology ontology);

    Set<OWLIndividualAxiom> generateModel();
}
