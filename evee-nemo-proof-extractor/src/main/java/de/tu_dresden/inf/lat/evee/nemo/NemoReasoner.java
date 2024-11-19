package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;

public class NemoReasoner {

    // TODO make configurable
    private final String nemoExecDir = System.getProperty("user.home");
    private final String nemoRulesFilePath = "Documents/work/program.rls";
    private final String nemoImportDir = "Documents/work";

    private OWLOntology ontology;
    
    public NemoReasoner(OWLOntology ontology){
        this.ontology = ontology;
    }

    public Proof<OWLAxiom> proof(OWLAxiom axiom) throws OWLOntologyStorageException, IOException, InterruptedException{
        
    System.out.println("start cooking");

    //export ontology to nt file

    //File file = File.createTempFile("ontology", ".nt");
    //String tmpDir = System.getProperty("java.io.tmpdir");

    File file = new File(nemoExecDir + "/Documents/work/ont.nt");
    TurtleDocumentFormat format = new TurtleDocumentFormat();
    ontology.saveOntology(format, IRI.create(file.toURI()));
    
    System.out.println("cooking...");

    //run nemo
    ProcessBuilder pb = new ProcessBuilder("./nmo", "-v", "-I", nemoImportDir, nemoRulesFilePath).inheritIO();
    pb.directory(new File(nemoExecDir));
    Process p = pb.start();

    int returnCode = p.waitFor();
    System.out.println("return Code of nemo: " + returnCode);

    return null;
    }

}