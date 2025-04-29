package de.tu_dresden.inf.lat.evee.nemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;

import org.junit.Ignore;
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
    public void testAllTasks() throws OWLOntologyCreationException, ProofGenerationException{
        String [] taskFiles = {
            "task_EL.json",
            "task_roleChain_concat.json",
            "task_roleChain_simple.json",
            "task_roleChain.json",
            "task_roleInc.json",
            "task_transProp.json"
        };
        CorrectnessEvaluator evaluator = new CorrectnessEvaluator();

        for(String taskFile : taskFiles){
            IInference<OWLAxiom> task = readTask(taskFile);
            IProof<OWLAxiom> proof = runTask(task);
            evaluator.setTask(task);

            assertTrue(evaluator.evaluate(proof)==1d);
        }
    }

    //for dev purposes
    @Ignore @Test
    public void testTask() throws OWLOntologyCreationException, ProofGenerationException{
        IInference<OWLAxiom> task = readTask(  "task_transProp.json");
        IProof<OWLAxiom> proof = runTask(task);

        System.out.println(proof.toString());
        System.out.println("final conclusion: " + proof.getFinalConclusion());

        CorrectnessEvaluator evaluator = new CorrectnessEvaluator();
        evaluator.setTask(task);
        assertTrue(evaluator.evaluate(proof)==1d);
    }

    @Ignore("For dev purposes")
    @Test
    public void testTask2() throws OWLOntologyCreationException, ProofGenerationException{
        IInference<OWLAxiom> task = readTask(  "task00001.json");
        IProof<OWLAxiom> proof = runTask(task);

        System.out.println(proof.toString());
        System.out.println("final conclusion: " + proof.getFinalConclusion());

        CorrectnessEvaluator evaluator = new CorrectnessEvaluator();
        evaluator.setTask(task);
        assertEquals(1d, evaluator.evaluate(proof), 0.0);
    }


    @Test
    public void testTask3() throws OWLOntologyCreationException, ProofGenerationException{
        IInference<OWLAxiom> task = readTask(  "task00003.json");
        IProof<OWLAxiom> proof = runTask(task);

        System.out.println(proof.toString());
        System.out.println("final conclusion: " + proof.getFinalConclusion());

        CorrectnessEvaluator evaluator = new CorrectnessEvaluator();
        evaluator.setTask(task);
        assertEquals(1d, evaluator.evaluate(proof), 0.0);
    }

    //for dev purposes
    //@Ignore
    @Test
    public void exportProofJson() throws Exception{
        IInference<OWLAxiom> task = readTask(  "task_transProp.json");
        IProof<OWLAxiom> proof = runTask(task);

        JsonProofWriter<OWLAxiom> jsonWriter = new JsonProofWriter<>();
        try{
            jsonWriter.writeToFile(proof, "filePath");
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private IProof<OWLAxiom> runTask(IInference<OWLAxiom> task) throws ProofGenerationException, OWLOntologyCreationException{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, new HashSet<OWLAxiom>(task.getPremises()));

        NemoProofGenerator generator = new NemoProofGenerator(ontology);

        return generator.getProof(task.getConclusion());
    }

    private IInference<OWLAxiom> readTask(String fileName){
        String path = Thread.currentThread().getContextClassLoader().getResource(fileName).getPath();
        File taskFile = new File(path);

        return JsonProofParser.getInstance().fromFile(taskFile).getInferences().get(0);
    }

}
