package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface IOWLNonEntailmentExplainer<T extends OWLAxiom> extends INonEntailmentExplainer<Set<OWLAxiom>, OWLEntity, OWLOntology, Set<T>> {

//    default implementation of ignoresPartOfOntology -> uses getSupportedAxiomTypes and ClassExpressionType of owlapi

//     pre-define sets for EL, ALC for easy use
//    getSupportedAxiomTypes() and getSupportedClassExpressionTypes implemented by Explainer

}
