package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;

public class NemoReasoner {

    private static final Logger logger = LogManager.getLogger(NemoReasoner.class);

    private final static String
            ELK_FILENAME = "elk",
            ENVELOPE_FILENAME = "envelope",
            TEXTBOOK_FILENAME = "textbook",
            NEMO_RULE_FILE_SUFFIX = ".rls",
            ONTOLOGY_EXPORT_FILE_NAME = "ont.ttl";
    
    //path to directory of nemo executable TODO: make configurable
    private String nemoExecDir = System.getProperty("user.dir");
    private OWLOntology ontology;
    
    public NemoReasoner(OWLOntology ontology){
        this.ontology = ontology;
    }

    public void setNemoExecDir(String nemoExecDirPath){
        nemoExecDir = nemoExecDirPath;
    }

    public IProof<String> proof(String axiom, ECalculus calculus) throws IOException, NemoExcecException {
        
        logger.debug("generating proof");

        //create tmp files
        Path importDir = prepareImportDir(ontology);
        File ruleFile = prepareRuleFile(getRuleFileName(calculus));
        File traceFile = File.createTempFile("nemoTrace", ".json");

        //run nemo
        logger.debug("running nemo");
        int exitCode = runNemo(importDir.toString(), ruleFile.getAbsolutePath(), traceFile.getAbsolutePath(), axiom);
        if (exitCode != 0)
            throw new NemoExcecException("error running nemo. Exit code " + exitCode);

        JsonStringProofParser proofParser = JsonStringProofParser.getInstance();
        IProof<String> proof = proofParser.fromFile(traceFile);
        if (proof == null)
            throw new IOException("Error reading nemo trace file");

        return proof;
    }
    private String getRuleFileName(ECalculus calculus){
        switch (calculus) {
            case ELK:
                return ELK_FILENAME;
            case ENVELOPE:
                return ENVELOPE_FILENAME;
            case TEXTBOOK:
                return TEXTBOOK_FILENAME;
            default:
                return "";
        }
    }

    private File prepareRuleFile(String ruleFile) throws IOException{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            try(InputStream isSrcFile = classLoader.getResourceAsStream(ruleFile + NEMO_RULE_FILE_SUFFIX)){
                if(isSrcFile == null)
                    throw new FileNotFoundException("Could not locate the rule file");

            File ruleDstFile =  File.createTempFile(ruleFile, NEMO_RULE_FILE_SUFFIX);

            Files.copy(isSrcFile, ruleDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return ruleDstFile;
        }
    }

    private Path prepareImportDir(OWLOntology ontology) throws IOException {
        Path importDir = Files.createTempDirectory("nemo_exec");

        File ontFile = new File(importDir + "/" + ONTOLOGY_EXPORT_FILE_NAME);
        try{
            ontology.saveOntology(new TurtleDocumentFormat(), IRI.create(ontFile.toURI()));
        }catch(OWLOntologyStorageException e){
            throw new IOException("error saving Ontology to file: ", e);
        }

        return importDir;
    }

    private int runNemo(String importDir, String ruleFile, String traceFile, String axiom) throws NemoExcecException {
        ProcessBuilder pb = new ProcessBuilder( "./nmo", "-v", "-I", importDir, ruleFile, "--trace-output",
                traceFile
                , "--trace", axiom).inheritIO();

        pb.directory(new File(nemoExecDir));
         
        int exitCode;
        try{
            Process p = pb.start();
            exitCode = p.waitFor();
        }catch(Exception e){
            throw new NemoExcecException("error running nemo: ", e);
        }

        return exitCode;
    }
}