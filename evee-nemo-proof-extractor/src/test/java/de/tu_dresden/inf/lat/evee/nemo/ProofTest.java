package de.tu_dresden.inf.lat.evee.nemo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofParser;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonProofWriter;
import de.tu_dresden.inf.lat.evee.proofs.tools.evaluators.CorrectnessEvaluator;

public class ProofTest {
    
    @Test
    public void testProof_success() throws ProofGenerationException, OWLOntologyCreationException{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        
        IInference<OWLAxiom> task = JsonProofParser.getInstance()
        .fromFile(new File(
                Thread.currentThread().getContextClassLoader().getResource("task_roleChain_concat.json").getPath()))
        .getInferences().get(0);

        OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));

        CorrectnessEvaluator evaluator = new CorrectnessEvaluator();
        evaluator.setTask(task);

        NemoProofGenerator generator = new NemoProofGenerator(ontology);
        IProof<OWLAxiom> proof = generator.getProof(task.getConclusion());

        System.out.println(proof.toString());
        System.out.println("final conclusion: " + proof.getFinalConclusion());

        assertTrue(evaluator.evaluate(proof)==1d);

        // JsonProofWriter<OWLAxiom> jsonWriter = new JsonProofWriter<>();
        // try{
        //     jsonWriter.writeToFile(proof, "/Users/max/Documents/proofs/owlProof_EL_ruleNames");
        // }catch(Exception e){
        //     System.out.println(e);
        // }
    }
}
