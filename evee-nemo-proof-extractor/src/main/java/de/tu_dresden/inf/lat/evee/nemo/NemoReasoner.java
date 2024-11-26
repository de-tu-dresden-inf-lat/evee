package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.tu_dresden.inf.lat.evee.proofs.data.Proof;

public class NemoReasoner {

    private final String nemoExecDir = System.getProperty("user.home"); // TODO make configurable
    private final String OWL_RDF_CCOMPLETE_FILE_NAME = "/owl-rdf-complete-reasoning.rls";

    private OWLOntology ontology;
    
    public NemoReasoner(OWLOntology ontology){
        this.ontology = ontology;
    }

    public Proof<OWLAxiom> proof(OWLAxiom axiom) throws OWLOntologyStorageException, IOException, InterruptedException{
        
    System.out.println("start cooking");

    String tmpDir = System.getProperty("java.io.tmpdir");

    //export ontology to ttl file
    File ontFile = new File(tmpDir + "/ont.ttl");
    TurtleDocumentFormat format = new TurtleDocumentFormat();
    ontology.saveOntology(format, IRI.create(ontFile.toURI()));

    //copy rule file to tmp dir
    ClassLoader classLoader = getClass().getClassLoader();
    File ruleSrcFile = new File(classLoader.getResource(OWL_RDF_CCOMPLETE_FILE_NAME).getFile());
    File ruleDstFile = new File(tmpDir + OWL_RDF_CCOMPLETE_FILE_NAME);
    Files.copy(ruleSrcFile.toPath(), ruleDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

    
    System.out.println("cooking...");

    //run nemo
    ProcessBuilder pb = new ProcessBuilder("./nmo", "-v", "-I", tmpDir, ruleDstFile.getAbsolutePath()).inheritIO();
    pb.directory(new File(nemoExecDir));
    Process p = pb.start();

    int returnCode = p.waitFor();
    System.out.println("return Code of nemo: " + returnCode);

    return null;
    }

}