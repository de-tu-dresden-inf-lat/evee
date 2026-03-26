package de.tu_dresden.inf.lat.counterExample.tools;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * @author Christian Alrabbaa
 *
 */
public class OntologyFilter {
    public static OWLOntology filterEL(OWLOntology ontology) {
        for(OWLAxiom a: ontology.getAxioms()) {
            if (a.isOfType(AxiomType.SUBCLASS_OF) || a.isOfType(AxiomType.EQUIVALENT_CLASSES))
                if (!AxiomChecker.isInEL(a))
                    ontology.getOWLOntologyManager().removeAxiom(ontology, a);
        }
        return ontology;
    }
}
