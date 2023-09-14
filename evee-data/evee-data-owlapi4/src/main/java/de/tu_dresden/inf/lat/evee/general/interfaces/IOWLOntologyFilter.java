package de.tu_dresden.inf.lat.evee.general.interfaces;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;

public interface OWLOntologyFilter {

    List<AxiomType<? extends OWLAxiom>> getAxiomTypes();

    List<ClassExpressionType> getConceptTypes();

}
