package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser;

public class ProofTest {
    
    @Test
    public void testProof_success() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, InterruptedException{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        
        IInference<OWLAxiom> task = JsonProofParser.getInstance()
        .fromFile(new File(
                Thread.currentThread().getContextClassLoader().getResource("task01212.json").getPath()))
        .getInferences().get(0);

        OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));

        NemoReasoner reasoner = new NemoReasoner(ontology);
        IProof<OWLAxiom> proof = reasoner.proof(task.getConclusion());

        System.out.println(proof.toString());
    }
}
