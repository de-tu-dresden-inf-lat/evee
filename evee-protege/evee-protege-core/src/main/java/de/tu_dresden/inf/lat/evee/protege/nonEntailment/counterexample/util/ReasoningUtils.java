package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ReasoningUtils {

    public static boolean subsumptionHolds(OWLOntology ont,OWLSubClassOfAxiom observation) {
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont);
        if (reasoner.isEntailed(observation)) {
            return true;
        }
        return false;
    }

    public static boolean isConsistent(OWLOntology ont, OWLSubClassOfAxiom observation) {

        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        IRI newIndIRI = IRI.create("newInd");
        OWLNamedIndividual newInd = df.getOWLNamedIndividual(newIndIRI);
        OWLClassAssertionAxiom newAxiom = df.getOWLClassAssertionAxiom(observation.getSubClass(),newInd);
        man.addAxiom(ont,newAxiom);
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont);
        boolean isConsistent = reasoner.isConsistent();
        man.removeAxiom(ont,newAxiom);
        return isConsistent;
    }
}
