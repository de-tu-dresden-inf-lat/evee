package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class Main {
    
    public static void main(String[] args) throws Exception {

        String homeDir = System.getProperty("user.home");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(homeDir + "/Documents/work/galenOWL.owl"));
        
        NemoReasoner reasoner = new NemoReasoner(ont);
        reasoner.proof(null);
    }
}
