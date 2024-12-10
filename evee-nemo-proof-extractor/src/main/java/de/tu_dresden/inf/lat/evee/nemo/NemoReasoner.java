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
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.evee.nemo.parser.NemoOwlParser;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.json.JsonStringProofParser;

public class NemoReasoner {

    private static final Logger logger = LogManager.getLogger(NemoReasoner.class);

    private static final String OWL_RDF_COMPLETE_FILE_NAME = "owl-rdf-complete-reasoning";
    private static final String NEMO_RULE_FILE_SUFFIX = ".rls";
    private static final String ONTOLOGY_EXPORT_FILE_NAME = "ont.ttl";

    private static final String nemoExecDir = System.getProperty("user.home"); // TODO make configurable

    private OWLOntology ontology;
    
    public NemoReasoner(OWLOntology ontology){
        this.ontology = ontology;
    }

    public IProof<OWLAxiom> proof(OWLAxiom axiom) throws OWLOntologyStorageException, IOException, InterruptedException{
        
        logger.debug("generating proof");

        //parse axiom to nemo format
        //assume every requested axiom is a subClass axiom (TODO: intance check of axiom)
        NemoOwlParser parser = NemoOwlParser.getInstance();
        String nemoAxiom = parser.subClassAxiomToNemoString((OWLSubClassOfAxiom) axiom);
        logger.debug("parsed nemo Axiom: " + nemoAxiom);

        //create all needed files
        Path importDir = prepareImportDir(ontology);
        File ruleFile = prepareRuleFile(OWL_RDF_COMPLETE_FILE_NAME, NEMO_RULE_FILE_SUFFIX);
        File traceFile = File.createTempFile("nemoTrace", ".json");

        logger.debug("running nemo");

        //run nemo
        int exitCode = runNemo(importDir.toString(), ruleFile.getAbsolutePath(), traceFile.getAbsolutePath(), nemoAxiom);
        logger.debug("return Code of nemo: " + exitCode);

        JsonStringProofParser proofParser = JsonStringProofParser.getInstance();
        IProof<String> proof = proofParser.fromFile(traceFile);

        if (proof == null)
            throw new UnknownFormatConversionException("error parsing proof. see debug log for stacktrace"); //TODO throw which excetption??
        
        return parser.nemoProoftoProofOWL(proof);
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