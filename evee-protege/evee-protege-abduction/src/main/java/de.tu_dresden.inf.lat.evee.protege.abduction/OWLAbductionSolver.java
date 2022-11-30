package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface OWLAbductionSolver extends AbductionSolver<Set<OWLAxiom>, OWLEntity, OWLOntology, Set<OWLAxiom>> {
}
