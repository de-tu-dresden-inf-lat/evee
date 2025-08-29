package de.tu_dresden.inf.lat.evee.nemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import de.tu_dresden.inf.lat.evee.nemo.parser.tools.OWLHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;

public class NemoReasoner {

    private static final Logger logger = LogManager.getLogger(NemoReasoner.class);
    private final OWLHelper owlHelper= OWLHelper.getInstance();

    private final String
            ELK_FILENAME = "elk",
            ENVELOPE_FILENAME = "envelope",
            TEXTBOOK_FILENAME = "textbook",
            NEMO_RULE_FILE_SUFFIX = ".rls",
            ONTOLOGY_EXPORT_FILE_NAME = "ont.ttl";
    
    //path to nemo executable
    private String nemoExecPath;

    private OWLOntology ontology;
    private Collection<OWLClassExpression> goalExpressions;
    
    public NemoReasoner(){
        this.goalExpressions = Collections.emptyList();
    }

      public NemoReasoner(OWLOntology ontology){
        this.goalExpressions = Collections.emptyList();
        this.ontology = ontology;
    }

    public void setNemoExecPath(String nemoExecPath){
        this.nemoExecPath = nemoExecPath;
    }

    public void setOntology(OWLOntology ontology){
        this.ontology = ontology;
    }

    public void setOptionalGoalExpressions(Collection<OWLClassExpression> expressions){
        this.goalExpressions = new HashSet<>(expressions);
    }

    public IProof<String> proof(String axiom, ECalculus calculus) throws IOException, NemoExcecException {
        logger.debug("generating proof");

        if(ontology == null)
            throw new NemoExcecException("no ontology found. call setOntology()");

        //create tmp files
        Path importDir = prepareImportDir(ontology, this.goalExpressions);
        File ruleFile = prepareRuleFile(getRuleFileName(calculus));
        File traceFile = File.createTempFile("nemoTrace", ".json");
        Path exportDir = Files.createTempDirectory("nemo_dump");

        //run nemo
        logger.debug("running nemo");
        int exitCode = runNemo(importDir.toString(), exportDir.toString(), ruleFile.getAbsolutePath(), traceFile.getAbsolutePath(), axiom);
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
        
            try(InputStream isSrcFile =  getClass().getClassLoader().getResourceAsStream(ruleFile + NEMO_RULE_FILE_SUFFIX)){
                if(isSrcFile == null)
                    throw new FileNotFoundException("Could not locate the rule file");

            File ruleDstFile =  File.createTempFile(ruleFile, NEMO_RULE_FILE_SUFFIX);

            Files.copy(isSrcFile, ruleDstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return ruleDstFile;
        }
    }

    private Path prepareImportDir(OWLOntology ontology, Collection<OWLClassExpression> goalExpressions) throws IOException {
        updateOntology(ontology, goalExpressions);

        Path importDir = Files.createTempDirectory("nemo_exec");

        File ontFile = new File(importDir + "/" + ONTOLOGY_EXPORT_FILE_NAME);
        try{
            ontology.saveOntology(new TurtleDocumentFormat(), IRI.create(ontFile.toURI()));
        }catch(OWLOntologyStorageException e){
            throw new IOException("error saving Ontology to file: ", e);
        }

        return importDir;
    }

    private void updateOntology(OWLOntology ontology, Collection<OWLClassExpression> owlClassExpressions) {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        owlClassExpressions.forEach(expression->{
            m.addAxiom(ontology,owlHelper.getOWLSubClassOfAxiom(expression, expression));
        });
    }

    private int runNemo(String importDir, String exportDir,String ruleFile, String traceFile, String axiom) throws NemoExcecException {
        ProcessBuilder pb = new ProcessBuilder( "."+nemoExecPath, "-I", importDir, ruleFile, "--trace-output",
                traceFile, "--trace", axiom, "-D", exportDir).inheritIO();

        pb.directory(new File("/"));
         
        int exitCode;
        try{
            Process p = pb.start();
            exitCode = p.waitFor();
        }
        catch(IOException i){
            throw new NemoExcecException("No NEMO executable at " + nemoExecPath);
        }
        catch(Exception e){
            throw new NemoExcecException("error running nemo: " + e, e);
        }

        return exitCode;
    }
}