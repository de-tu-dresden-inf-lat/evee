package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface OWLNonEntailmentExplainer extends NonEntailmentExplainer<Set<OWLAxiom>, OWLEntity, OWLOntology, Set<OWLAxiom>> {
}
