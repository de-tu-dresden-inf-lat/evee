package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UnknownFormatConversionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;

public class NemoReasoner {

    private static final Logger logger = LogManager.getLogger(NemoReasoner.class);

    private static final String OWL_RDF_COMPLETE_FILE_NAME = "owl-rdf-complete-reasoning";
    private static final String NEMO_RULE_FILE_SUFFIX = ".rls";
    private static final String ONTOLOGY_EXPORT_FILE_NAME = "ont.ttl";

    //path to directory of nemo executable TODO: make configurable
    private String nemoExecDir = System.getProperty("user.home");
    private OWLOntology ontology;
    
    public NemoReasoner(OWLOntology ontology){
        this.ontology = ontology;
    }

    public void setNemoExecDir(String nemoExecDirPath){
        nemoExecDir = nemoExecDirPath;
    }

    public IProof<String> proof(String axiom) throws IOException, OWLOntologyStorageException, InterruptedException {
        
        logger.debug("generating proof");

        //create all needed files
        Path importDir = prepareImportDir(ontology);
        File ruleFile = prepareRuleFile(OWL_RDF_COMPLETE_FILE_NAME, NEMO_RULE_FILE_SUFFIX);
        File traceFile = File.createTempFile("nemoTrace", ".json");

        logger.debug("running nemo");

        //run nemo
        int exitCode = runNemo(importDir.toString(), ruleFile.getAbsolutePath(), traceFile.getAbsolutePath(), axiom);
        logger.debug("return Code of nemo: " + exitCode);
        //TODO: check exit code?

        JsonStringProofParser proofParser = JsonStringProofParser.getInstance();

        IProof<String> proof = proofParser.fromFile(traceFile);
        if (proof == null)
            throw new IOException("Error reading nemo trace file");

        return proof;
    }

    
    private File prepareRuleFile(String resourceFileName, String resourceSuffix) throws IOException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        File ruleSrcFile = new File(classLoader.getResource(resourceFileName + resourceSuffix).getFile());
        File ruleDstFile =  File.createTempFile(resourceFileName, resourceSuffix);

        Files.copy(ruleSrcFile.toPath(), ruleDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return ruleDstFile;
    }

    private Path prepareImportDir(OWLOntology ontology) throws IOException, OWLOntologyStorageException {
        Path importDir = Files.createTempDirectory("nemo_exec");

        File ontFile = new File(importDir + "/" + ONTOLOGY_EXPORT_FILE_NAME);
        ontology.saveOntology(new TurtleDocumentFormat(), IRI.create(ontFile.toURI()));

        return importDir;
    }

    private int runNemo(String importDir, String ruleFile, String traceFile, String axiom) throws InterruptedException, IOException{
        ProcessBuilder pb = new ProcessBuilder("./nmo", "-v", "-I", importDir, ruleFile,
        "--trace-output", traceFile, "--trace", axiom)
            .inheritIO();

        pb.directory(new File(nemoExecDir));
        Process p = pb.start();

        return p.waitFor();
    }
}