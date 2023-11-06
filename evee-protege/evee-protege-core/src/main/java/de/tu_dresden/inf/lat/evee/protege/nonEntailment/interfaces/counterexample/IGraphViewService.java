package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphView;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface IGraphViewService {

    IGraphView computeView(Set<OWLIndividualAxiom> model,
                  OWLOntology ontology,
                  Set<IRI> markedIndividuals,
                  int labelsNum);

    default void doPostProcessing() {};
}
