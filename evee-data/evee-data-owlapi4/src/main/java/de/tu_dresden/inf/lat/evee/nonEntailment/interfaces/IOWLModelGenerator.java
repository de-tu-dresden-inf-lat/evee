package de.tu_dresden.inf.lat.evee.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface IOWLModelGenerator {
    void setOntology(OWLOntology ontology);
    Set<OWLIndividualAxiom> generateModel() throws ModelGenerationException;
    Set<IRI> getMarkedIndividuals();
}
