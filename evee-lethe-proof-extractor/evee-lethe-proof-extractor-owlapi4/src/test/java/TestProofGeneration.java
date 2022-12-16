import de.tu_dresden.inf.lat.evee.general.data.exceptions.FormattingException;
import de.tu_dresden.inf.lat.evee.proofs.data.exceptions.ProofGenerationException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.lethe.ProofUtils;
import de.tu_dresden.inf.lat.evee.proofs.lethe.TestTasksRunner;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;

public class TestProofGeneration {

    @Test
    public void testBasicProof() throws OWLOntologyCreationException, FormattingException, IOException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        OWLOntology ontology;
        OWLDataFactory df = man.getOWLDataFactory();

        OWLClass A = df.getOWLClass(IRI.create("A"));
        OWLClass B = df.getOWLClass(IRI.create("B"));
        OWLClass C = df.getOWLClass(IRI.create("C"));
        OWLObjectProperty R = df.getOWLObjectProperty(IRI.create("R"));

        OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(
                A,
                df.getOWLObjectSomeValuesFrom(R, B)
        );

        OWLSubClassOfAxiom ax2 = df.getOWLSubClassOfAxiom(
                df.getOWLObjectSomeValuesFrom(R, B),
                C
        );

        ontology = man.createOntology();
        man.addAxiom(ontology, ax1);
        man.addAxiom(ontology, ax2);

        LetheProofGenerator proofGenerator = new LetheProofGenerator();
        proofGenerator.setOntology(ontology);

        // Prove that A SubClassOf C
        try{
            IProof<OWLAxiom> proof = proofGenerator.proveSubsumption(A, C);
            ProofUtils.showProof(proof);
        }
        catch (ProofGenerationException e){
            System.out.println("Test failed: " + e);
        }


    }

    @Test
    public void testTask00002() throws FormattingException, IOException {
        File taskFile = new File("../evaluation-data/alc-tasks/task00002.json");
        IProof<OWLAxiom> proof = TestTasksRunner.runTask(taskFile.toString());
        assert proof!=null;
        ProofUtils.showProof(proof);
    }

    // TODO: possible bug in LETHE, to be fixed.
    // @Test
    // public void testTask00125() throws FormattingException, IOException {
    //     File taskFile = new File("./task00125.json");
    //     IProof<OWLAxiom> proof = TestTasksRunner.runTask(taskFile.toString());
    //     assert proof!=null;
    //     System.out.println(proof);
    //     ProofUtils.checkProof(proof);
    //     ProofUtils.showProof(proof);
    // }


    // TODO: this is a problematic test (slow and runs out of memory)
    // @Test
    // public void testAminoAcidsExample() {
    //     String ontologyUrl = "https://raw.githubusercontent.com/TheOntologist/AminoAcidOntology/master/amino-acid.owl";

    //     OWLOntologyManager man = OWLManager.createOWLOntologyManager();

    //     OWLOntology ontology;
    //     OWLDataFactory df = man.getOWLDataFactory();

    //     try {
    //         ontology = man.loadOntology(IRI.create(ontologyUrl));

    //         LetheProofGenerator proofGenerator = new LetheProofGenerator();
    //         proofGenerator.setOntology(ontology);

    //         OWLClass glutamine = df.getOWLClass("http://www.co-ode.org/ontologies/amino-acid/2006/05/18/amino-acid.owl#Q");
    //         OWLClass largeAliphatic = df.getOWLClass("http://www.co-ode.org/ontologies/amino-acid/2006/05/18/amino-acid.owl#LargeAliphaticAminoAcid");
    //         // Prove that A SubClassOf C
    //         IProof<OWLAxiom> proof = proofGenerator.proveSubsumption(glutamine, largeAliphatic);

    //         assert proof!=null;

    //         ProofUtils.showProof(proof);

    //     } catch (OWLOntologyCreationException | FormattingException | IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
