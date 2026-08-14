package de.tu_dresden.inf.lat.evee.concreteDomains;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


public class CDReasonerFactory implements OWLReasonerFactory {

    @Override
    public String getReasonerName() {
        return CDReasonerFactory.class.getPackage().getImplementationTitle();

    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createNonBufferingReasoner'");
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createReasoner'");
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createNonBufferingReasoner'");
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createReasoner'");
    }
    

    private OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config, boolean buffering) {

        // 1. check type of concrete domain, D_lin, D_diff, or D_mult
        // 2. call parser accordingly 
        //          -> write new object extendedOntologyParser in concrete domain reasoner
        // 3. parse ontology to extended ontology
        // 4. create reasoner with parsed ontology
        
        //ExtendedOntology extOntology = ExtendedOntologyParser


        //ontology.get
        return null;

    }
}
