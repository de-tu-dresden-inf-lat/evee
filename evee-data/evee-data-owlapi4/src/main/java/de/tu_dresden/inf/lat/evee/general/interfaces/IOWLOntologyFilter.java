package de.tu_dresden.inf.lat.evee.general.interfaces;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;
import java.util.Set;

public interface IOWLOntologyFilter {

    List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes();

    List<ClassExpressionType> getSupportedConceptTypes();

    Set<AxiomType<? extends OWLAxiom>> getSupportedFromABoxTBoxRBox();

}
