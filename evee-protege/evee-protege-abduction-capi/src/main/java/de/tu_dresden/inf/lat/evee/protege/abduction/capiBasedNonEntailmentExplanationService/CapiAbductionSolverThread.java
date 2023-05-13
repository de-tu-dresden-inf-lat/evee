package de.tu_dresden.inf.lat.evee.protege.abduction.capiBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.lat.capi.experiments.AbductionProblem;
import de.tu_dresden.lat.capi.implicateMatching.*;
import de.tu_dresden.lat.capi.ontologyTools.ELFilter;
import de.tu_dresden.lat.capi.owl2spass.OWL2SpassConverter;
import de.tu_dresden.lat.capi.owl2spass.OWL2SpassConverter.EmptyOntologyException;
import de.tu_dresden.lat.capi.owl2spass.OWL2SpassConverter.TranslationException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

public class CapiAbductionSolverThread extends Thread implements IExplanationGenerator<List<Solution>> {

    private final IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<List<Solution>>>> listener;
    private final OWLOntology activeOntology;
    private final OWLSubClassOfAxiom observation;
    private final List<Solution> solutions;
    private IProgressTracker progressTracker = null;
    private int skolemBound;
    private String errorMessage;
    private String spassPath = null;
    private int timeLimit;
    private boolean removeRedundancies;
    private boolean simplifyConjunctions;
    private boolean semanticallyOrdered;
    private static final String PROBLEM_SPASS = "problem.spass";
    private static final String SPASS_CLAUSES = "problem.spass.clauses";
    private static final String SKOLEM_BOUND = "skolem.bound";
    private static final String TEMPORARY_ONTOLOGY = "eveeTemporaryOntologyFile.owl";
    private static final String NAME_MAP = "eveeTemporaryOntologyFile.nameMap";

    private final Logger logger = LoggerFactory.getLogger(CapiAbductionSolverThread.class);

    public CapiAbductionSolverThread(
            IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<List<Solution>>>> listener,
            OWLOntology activeOntology, OWLSubClassOfAxiom observation){
        this.logger.debug("Creating CapiAbductionSolverThread");
        this.listener = listener;
        this.activeOntology = activeOntology;
        this.observation = observation;
        this.solutions = new ArrayList<>();
        this.errorMessage = "";
        this.logger.debug("CapiAbductionSolverThread created successfully");
    }

    public void setSpassPath(String path){
        this.spassPath = path;
        this.logger.debug("Parameter SPASS path set to: " + path);
    }

    public void setTimeLimit(int timeLimit){
        this.timeLimit = timeLimit;
        this.logger.debug("Parameter time limit set to: " + timeLimit);
    }

    public void setRemoveRedundancies(boolean removeRedundancies){
        this.removeRedundancies = removeRedundancies;
        this.logger.debug("Parameter remove redundancies set to: " + removeRedundancies);
    }

    public void setSimplifyConjunctions(boolean simplifyConjunctions){
        this.simplifyConjunctions = simplifyConjunctions;
        this.logger.debug("Parameter simplify conjunctions set to: " + simplifyConjunctions);
    }

    public void setSemanticallyOrdered(boolean semanticallyOrdered){
        this.semanticallyOrdered = semanticallyOrdered;
        this.logger.debug("Parameter semantically ordered set to: " + semanticallyOrdered);
    }

    public void setProgressTracker(IProgressTracker progressTracker){
        this.progressTracker = progressTracker;
        this.logger.debug("Progress tracker set");
    }

    public void run(){
        this.logger.debug("Starting Thread");
        assertNotNull(this.spassPath);
        File spassFile = new File(this.spassPath);
//        todo: adapt for linux
        if (! spassFile.exists() || ! this.spassPath.endsWith("SPASS.exe")){
            this.errorMessage = "Invalid path to SPASS.exe";
            this.listener.handleEvent(new ExplanationEvent<>(
                    this, ExplanationEventType.ERROR));
        } else{
            try{
                this.createSpassInputFiles(this.observation);
                this.runSpass(this.spassPath);
                this.convertSpassOutputFile();
                this.listener.handleEvent(new ExplanationEvent<>(
                        this, ExplanationEventType.COMPUTATION_COMPLETE));
            } catch (OWLOntologyCreationException | IOException | EmptyOntologyException |
                    TranslationException | InterruptedException | OWLOntologyStorageException | NumberFormatException e) {
                this.errorMessage = "Error during computation: " + e;
                this.listener.handleEvent(new ExplanationEvent<>(
                        this, ExplanationEventType.ERROR));
            } finally {
                this.cleanUpFiles();
            }
        }
    }

    private void createSpassInputFiles(OWLSubClassOfAxiom observation) throws OWLOntologyCreationException,
            IOException, TranslationException, EmptyOntologyException, OWLOntologyStorageException,
            NumberFormatException {
        this.logger.debug("Creating SPASS input files");
        this.progressTracker.setMessage("Creating SPASS input files");
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        try (PrintWriter printWriter  = new PrintWriter(PROBLEM_SPASS)) {
            OWLOntology ontologyCopy = ontologyManager.createOntology();
            ontologyCopy = ontologyCopy.getOWLOntologyManager().copyOntology(
                    this.activeOntology, OntologyCopy.DEEP);
            ELFilter.deleteNonELAxioms(ontologyCopy);
            AbductionProblem abductionProblem = new AbductionProblem(ontologyCopy, observation);
            OWL2SpassConverter converter = new OWL2SpassConverter(true);
            converter.convertAbductionProblem(abductionProblem, printWriter,
                    ontologyCopy.getOntologyID().getOntologyIRI().or(
                            IRI.create(TEMPORARY_ONTOLOGY)).toString(),
                    SKOLEM_BOUND);
            converter.saveNameMap(new File(NAME_MAP));
            BufferedReader reader = new BufferedReader(new FileReader(SKOLEM_BOUND));
            String boundString = reader.readLine();
            reader.close();
            this.skolemBound = 2 + Integer.parseInt(boundString);
            FileOutputStream outputStream = new FileOutputStream(TEMPORARY_ONTOLOGY);
            ontologyManager.saveOntology(ontologyCopy, outputStream);
            outputStream.close();
            this.logger.debug("SPASS input files created successfully");
            this.progressTracker.setMessage("SPASS input files created successfully");
        } catch (OWLOntologyCreationException e) {
            this.logger.error("Error when copying ontology: " + e);
            throw e;
        } catch (FileNotFoundException e) {
            this.logger.error("Error when creating file \"problem.spass\":" + e);
            throw e;
        } catch (OWL2SpassConverter.TranslationException e) {
            this.logger.error("Error when translating ontology to SPASS: " + e);
            throw e;
        } catch (OWL2SpassConverter.EmptyOntologyException e) {
            this.logger.error("Error when extracting module from ontology: " + e);
            throw  e;
        } catch (OWLOntologyStorageException e) {
            this.logger.error("Error when creating temporary ontology file: " + e);
            throw e;
        } catch (IOException e) {
            this.logger.error("Error when reading from skolem.bound file: " + e);
            throw e;
        } catch (NumberFormatException e){
            this.logger.error("Error when converting value from skolem.bound file: " + e);
            throw e;
        }
    }

    private void runSpass(String spassPath) throws IOException, InterruptedException {
        this.logger.debug("Running SPASS");
        this.progressTracker.setMessage("Running SPASS");
        String timeLimit = "-TimeLimit=" + this.timeLimit;
        String boundStart = "-BoundStart=" + this.skolemBound;
        this.logger.debug("Parameters for SPASS: time limit={} - skolem bound={}", this.timeLimit, this.skolemBound);
        String[] spassCommand = new String[]{
                spassPath,
                "-SOS", "-Sorts=0", "-Auto=0", "-RFSub", "-RBSub", "-ISRe", "-ISFc", "-FPModel",
                "-CNFStrSkolem=0", "-CNFOptSkolem=0", timeLimit, "-PGiven=0", "-PProblem=0",
                "-BoundMode=2", boundStart, "-BoundLoops=1", "-WDRatio=1",
                PROBLEM_SPASS};
        try {
            Process process = new ProcessBuilder(spassCommand).start();
            int exitCode = process.waitFor();
            this.logger.debug("SPASS exited successfully");
            this.progressTracker.setMessage("SPASS exited successfully");
        } catch (IOException e) {
            this.logger.error("Error when running SPASS command: " + e);
            throw e;
        } catch (InterruptedException e){
//            todo: is this thrown when timelimit is reached -> if so, needs to be handled differently
            this.logger.error("Error - SPASS interrupted prematurely: " + e);
            throw e;
        }
    }

    private void convertSpassOutputFile() throws IOException, OWLOntologyCreationException {
        this.logger.debug("Parsing SPASS output to capi-solutions");
        this.progressTracker.setMessage("Parsing SPASS output");
        this.logger.debug("Parameters for parsing: remove redundancies={} - simplify conjunctions={} - " +
                "semantically ordered={}",
                this.removeRedundancies, this.simplifyConjunctions, this.semanticallyOrdered);
        PrimeImplicateParser parser = new PrimeImplicateParser();
        try {
            parser.parse(new File(SPASS_CLAUSES));
            PositiveClauseCollection positiveClauses = parser.getPosClauses();
            Collection<NegativeGroundClause> negativeClauses = parser.getNegClauses();
            SolutionGenerator solutionGenerator = new SolutionGenerator(positiveClauses);
            Collection<Solution> generatedSolutions = solutionGenerator.generateSolutions(negativeClauses);
            if (this.removeRedundancies || this.simplifyConjunctions || this.semanticallyOrdered){
                OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                        new File(TEMPORARY_ONTOLOGY));
                OWLConverter converter = new OWLConverter(ontology.getOWLOntologyManager().getOWLDataFactory(),
                        new File(NAME_MAP));
                PostProcessing postProcessing = new PostProcessing(ontology, converter);
                generatedSolutions = generatedSolutions.stream()
                        .map(sol -> {
                            if(this.simplifyConjunctions) {
                                this.logger.debug("Simplifying solution {}", sol);
                                return postProcessing.simplifyAxioms(sol);
                            }
                            else { return sol;}
                        }).map(sol -> {
                            if(this.removeRedundancies) {
                                this.logger.debug("Removing redundancy in {}", sol);
                                return postProcessing.removeRedundantAxioms(sol);
                            }
                            else { return sol;}
                        }).collect(Collectors.toSet());
                if(this.semanticallyOrdered) {
                    this.logger.debug("Semantically ordering solutions");
                    generatedSolutions = postProcessing.sortBySemanticMinimality(generatedSolutions);
                }
            }
            this.solutions.addAll(generatedSolutions);
            this.logger.debug("Solutions successfully generated");
            this.progressTracker.setMessage("Solutions successfully generated");
            this.logger.debug("Generated solutions:\n" + this.solutions);
        } catch (IOException e) {
            this.logger.error("Error when reading file: " + e);
            throw e;
        } catch (OWLOntologyCreationException e){
            this.logger.error("Error when reading temporary ontology file: " +e);
            throw e;
        }
    }

    private void cleanUpFiles(){
        this.logger.debug("Removing temporary files");
        this.progressTracker.setMessage("Cleaning up resources");
        File problemFile = new File(PROBLEM_SPASS);
        if (problemFile.exists()) {
            this.deleteFile(problemFile);
        }
        File clausesFile = new File(SPASS_CLAUSES);
        if (clausesFile.exists()) {
            this.deleteFile(clausesFile);
        }
        File skolemBound = new File(SKOLEM_BOUND);
        if (skolemBound.exists()){
            this.deleteFile(skolemBound);
        }
        File temporaryOntology = new File(TEMPORARY_ONTOLOGY);
        if (temporaryOntology.exists()){
            this.deleteFile(temporaryOntology);
        }
        File nameMap = new File(NAME_MAP);
        if (nameMap.exists()){
            this.deleteFile(nameMap);
        }
        this.progressTracker.setMessage("Clean up complete");
        this.logger.debug("All temporary files removed");
    }

    private void deleteFile(File file){
        try{
            if (! file.delete()){
                this.logger.warn("File '{}' could not be deleted", file);
            }
        } catch (Exception e) {
            this.logger.error("Error when deleting file named '{}': {}", file, e);
        }
    }

    @Override
    public List<Solution> getResult() {
        return this.solutions;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
