package de.tu_dresden.inf.lat.evee.protege.abduction.capiBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbstractAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.lat.capi.experiments.AbductionProblem;
import de.tu_dresden.lat.capi.implicateMatching.*;
import de.tu_dresden.lat.capi.ontologyTools.ELFilter;
import de.tu_dresden.lat.capi.owl2spass.OWL2SpassConverter;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;

public class CapiAbductionSolver
        extends AbstractAbductionSolver<List<Solution>>
        implements Supplier<Set<OWLAxiom>> {

    private List<Solution> solutions;
    private OWLEditorKit owlEditorKit;
    private IProgressTracker progressTracker;
    private int currentSolutionIndex;
    private boolean computationSuccessful;
    private int skolemBound;
    private String errorMessage = "";
    private String spassPath;
    private int timeLimit = -1;
    private boolean removeRedundancies;
    private boolean simplifyConjunctions;
    private boolean semanticallyOrdered;
    private Process spassProcess;
    private boolean cancelled;
    private File spassOutputFile;
    private static final String PROBLEM_SPASS = "problem.spass";
    private static final String SPASS_MODEL = "problem.spass.model";
    private static final String SPASS_CLAUSES = "problem.spass.clauses";
    private static final String SKOLEM_BOUND = "skolem.bound";
    private static final String TEMPORARY_ONTOLOGY = "eveeTemporaryOntologyFile.owl";
    private static final String NAME_MAP = "eveeTemporaryOntologyFile.nameMap";
    private final CapiPreferencesManager preferencesManager;
    private static final String EMPTY_SPASS_PATH = "<html>No path to SPASS is set.<br>Please set a path to the SPASS executable and hit 'Compute' again</html>";
    private final Logger logger = LoggerFactory.getLogger(CapiAbductionSolver.class);

    public CapiAbductionSolver(){
        super();
        this.logger.debug("Creating CapiAbductionSolver");
        this.solutions = new ArrayList<>();
        this.computationSuccessful = false;
        this.cancelled = false;
        this.currentSolutionIndex = 0;
        this.preferencesManager = new CapiPreferencesManager();
        this.spassProcess = null;
        this.logger.debug("CapiAbductionSolver created successfully");
    }

    @Override
    public void setup(OWLEditorKit owlEditorKit){
        this.owlEditorKit = owlEditorKit;
        super.setup(owlEditorKit);
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker){
        this.progressTracker = tracker;
    }

    @Override
    public Stream<Set<OWLAxiom>> generateExplanations() {
        this.spassPath = this.preferencesManager.loadSpassPath();
        this.cancelled = false;
        if (this.spassPath.equals("")){
            this.sendViewComponentEvent(ExplanationEventType.RESULT_RESET);
            this.showSpassPathDialog();
            return null;
        }
        this.timeLimit = this.preferencesManager.loadTimeLimit();
        this.removeRedundancies = this.preferencesManager.loadRemoveRedundancies();
        this.simplifyConjunctions = this.preferencesManager.loadSimplifyConjunctions();
        this.semanticallyOrdered = this.preferencesManager.loadSemanticallyOrdered();
        this.logger.debug("Generating Explanations");
        assertNotNull(this.ontology);
        this.solutions = null;
        if (this.checkResultInCache()){
            this.logger.debug("Cached result found, re-displaying cached result");
            this.solutions = this.loadResultFromCache();
            this.currentSolutionIndex = 0;
            return Stream.generate(this);
        } else{
            this.logger.debug("No cached result found, trying to compute new explanation");
            this.computeNewExplanation();
            if (this.cancelled){
                this.sendViewComponentEvent(ExplanationEventType.RESULT_RESET);
                return null;
            }
            else if (this.solutions.size() != 0){
                return Stream.generate(this);
            } else{
//                error-branch, an error was thrown to the viewComponent earlier, leading to the loading screen being disposed
                return null;
            }
        }
    }

    @Override
    public boolean supportsExplanation() {
//        todo: currently signature is completely ignored -> consider signature once functionality is enabled by capi
        if (this.missingEntailment.size() != 1){
            return false;
        } else {
            return (new ArrayList<>(this.missingEntailment)).get(0) instanceof OWLSubClassOfAxiom;
        }
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter a single 'SubClassOfAxiom' as missing entailment.";
    }

    private void computeNewExplanation() {
        assert (this.missingEntailment.size() != 0);
        final ArrayList<OWLSubClassOfAxiom> missingEntailmentList = new ArrayList<>();
        this.missingEntailment.forEach(axiom -> {
            if (axiom instanceof OWLSubClassOfAxiom)
                missingEntailmentList.add((OWLSubClassOfAxiom) axiom);
        });
        OWLSubClassOfAxiom singleMissingEntailment = missingEntailmentList.get(0);
        File spassFile = new File(this.spassPath);
        if (! spassFile.exists()){
            this.computationFailed("Invalid path to SPASS executable");
            this.sendViewComponentEvent(ExplanationEventType.ERROR);
        } else{
            try{
                this.createSpassInputFiles(singleMissingEntailment);
                this.runSpass(this.spassPath);
                this.convertSpassOutputFile();
                this.computationCompleted();
            } catch (OWLOntologyCreationException | IOException | OWL2SpassConverter.EmptyOntologyException |
                    OWL2SpassConverter.TranslationException | InterruptedException | OWLOntologyStorageException | NumberFormatException e) {
                this.computationFailed("Error during computation: " + e);
            } finally {
                this.cleanUpFiles();
            }
        }

    }

    private void createSpassInputFiles(OWLSubClassOfAxiom missingEntailment) throws OWLOntologyCreationException,
            IOException, OWL2SpassConverter.TranslationException, OWL2SpassConverter.EmptyOntologyException, OWLOntologyStorageException,
            NumberFormatException {
        this.logger.debug("Creating SPASS input files");
        this.progressTracker.setMessage("Creating SPASS input files");
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        try (PrintWriter printWriter  = new PrintWriter(PROBLEM_SPASS)) {
            OWLOntology ontologyCopy = ontologyManager.createOntology();
            ontologyCopy = ontologyCopy.getOWLOntologyManager().copyOntology(
                    this.ontology, OntologyCopy.DEEP);
            ELFilter.deleteNonELAxioms(ontologyCopy);
            AbductionProblem abductionProblem = new AbductionProblem(ontologyCopy, missingEntailment);
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
            this.logger.error("Error when copying ontology: ", e);
            throw e;
        } catch (FileNotFoundException e) {
            this.logger.error("Error when creating file \"problem.spass\":", e);
            throw e;
        } catch (OWL2SpassConverter.TranslationException e) {
            this.logger.error("Error when translating ontology to SPASS: ", e);
            throw e;
        } catch (OWL2SpassConverter.EmptyOntologyException e) {
            this.logger.error("Error when extracting module from ontology: ", e);
            throw  e;
        } catch (OWLOntologyStorageException e) {
            this.logger.error("Error when creating temporary ontology file: ", e);
            throw e;
        } catch (IOException e) {
            this.logger.error("Error when reading from skolem.bound file: ", e);
            throw e;
        } catch (NumberFormatException e){
            this.logger.error("Error when converting value from skolem.bound file: ", e);
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
            this.spassProcess = new ProcessBuilder(spassCommand).start();
            int exitCode = spassProcess.waitFor();
            this.spassProcess = null;
            this.logger.debug("SPASS computation process completed");
            this.progressTracker.setMessage("SPASS computation process completed");
        } catch (IOException e) {
            this.logger.error("Error when running SPASS command: ", e);
            throw e;
        } catch (InterruptedException e){
            this.logger.error("Error - SPASS interrupted prematurely: ", e);
            throw e;
        }
    }

    private void convertSpassOutputFile() throws IOException, OWLOntologyCreationException {
        this.logger.debug("Parsing SPASS output to capi-solutions");
        if (this.cancelled){
            this.logger.debug("Computation cancelled, no post-processing possible");
            return;
        }
        if (! this.checkSpassOutputFileExistence()){
            this.computationFailed("Error when trying to read from SPASS output file: file not existing.");
        } else{
            this.progressTracker.setMessage("Parsing SPASS output");
            this.logger.debug("Parameters for parsing: remove redundancies={} - simplify conjunctions={} - " +
                            "semantically ordered={}",
                    this.removeRedundancies, this.simplifyConjunctions, this.semanticallyOrdered);
            PrimeImplicateParser parser = new PrimeImplicateParser();
            try {
                parser.parse(this.spassOutputFile);
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
                this.solutions = new ArrayList<>(generatedSolutions);
                this.logger.debug("Solutions successfully generated");
                this.progressTracker.setMessage("Solutions successfully generated");
                this.logger.debug("Generated solutions:\n" + this.solutions);
            } catch (IOException e) {
                this.logger.error("Error when reading file: ", e);
                throw e;
            } catch (OWLOntologyCreationException e){
                this.logger.error("Error when reading temporary ontology file: ", e);
                throw e;
            }
        }
    }

    private boolean checkSpassOutputFileExistence(){
        File modelFile = new File(SPASS_MODEL);
        File clausesFile = new File(SPASS_CLAUSES);
        if (modelFile.exists()){
            this.spassOutputFile = modelFile;
            return true;
        } else if (clausesFile.exists()){
            this.spassOutputFile = clausesFile;
            return true;
        } else{
            return false;
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
        File modelFile = new File(SPASS_MODEL);
        if (modelFile.exists()){
            this.deleteFile(modelFile);
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
    public Set<OWLAxiom> get() {
        if (! this.computationSuccessful){
            this.logger.debug("Last computation did not end successfully, cannot return result");
            return null;
        } else {
            if (this.currentSolutionIndex < this.solutions.size()){
                this.logger.debug("Returning new solution");
                Set<OWLAxiom> result = new HashSet<>();
                Solution currentSolution = this.solutions.get(this.currentSolutionIndex);
                this.currentSolutionIndex ++;
                final Set<OWLClass> lhsClassNames = new HashSet<>();
                final Set<OWLClass> rhsClassNames = new HashSet<>();
                currentSolution.subsumptions().forEach(
                        solution -> {
                            lhsClassNames.addAll(this.parse2OWLClasses(solution.getLHS()));
                            rhsClassNames.addAll(this.parse2OWLClasses(solution.getRHS()));
                            OWLDataFactory factory = this.ontology.getOWLOntologyManager().getOWLDataFactory();
                            result.add(factory.getOWLSubClassOfAxiom(
                                    factory.getOWLObjectIntersectionOf(lhsClassNames),
                                    factory.getOWLObjectIntersectionOf(rhsClassNames)));
                        });
                return result;
            } else {
                this.logger.debug("All solutions have already been returned, returning null");
                return null;
            }
        }
    }

    private Set<OWLClass> parse2OWLClasses(Collection<ClassName> classNames){
        Set<OWLClass> resultClasses = new HashSet<>();
        classNames.forEach(className -> resultClasses.addAll(
                this.ontology.getClassesInSignature().stream().filter(
                                owlClass -> owlClass.getIRI().getRemainder().or("").equals(
                                        IRI.create(className.toString()).toString()))
                        .collect(Collectors.toSet())));
        return resultClasses;
    }

    private void computationCompleted(){
        this.computationSuccessful = ! this.cancelled;
        if (this.computationSuccessful){
            this.saveResultToCache(this.solutions);
            this.setActiveOntologyEditedExternally(false);
            this.currentSolutionIndex = 0;
        }
    }

    private void computationFailed(String errorMessage){
        this.computationSuccessful = false;
        this.setActiveOntologyEditedExternally(false);
        this.errorMessage = errorMessage;
        this.sendViewComponentEvent(ExplanationEventType.ERROR);
    }


    private void showSpassPathDialog(){
        SwingUtilities.invokeLater(() -> {
            JOptionPane warningPane = new JOptionPane(EMPTY_SPASS_PATH, JOptionPane.WARNING_MESSAGE);
            JDialog warningDialog = warningPane.createDialog(ProtegeManager.getInstance().getFrame(
                    this.owlEditorKit.getWorkspace()), "Warning");
            warningDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            warningDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(this.owlEditorKit.getWorkspace())));
            warningDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            warningDialog.addWindowListener(new java.awt.event.WindowAdapter(){
                @Override
                public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {
                    SwingUtilities.invokeLater(() -> {
//                        todo: what was this used for!?
//                        windowEvent.getWindow().dispose();
                        logger.debug("window disposed, why isn't this showing now!?");
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        int result = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(
                                ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
                        if (result == JFileChooser.APPROVE_OPTION){
                            File file = fileChooser.getSelectedFile();
                            preferencesManager.saveSpassPath(file.getPath());
                        }
                    });
                }
            });
            warningDialog.setVisible(true);
        });
    }

    @Override
    protected boolean parametersChanged(){
        return super.parametersChanged() || this.capiParametersChanged();
    }

    protected boolean capiParametersChanged(){
        return (!this.spassPath.equals(this.preferencesManager.loadSpassPath())) ||
                (!(this.timeLimit == this.preferencesManager.loadTimeLimit())) ||
                (!(this.removeRedundancies == this.preferencesManager.loadRemoveRedundancies())) ||
                (!(this.simplifyConjunctions == this.preferencesManager.loadSimplifyConjunctions())) ||
                (!(this.semanticallyOrdered == this.preferencesManager.loadSemanticallyOrdered()));
    }

    @Override
    public void cancel() {
        this.logger.debug("Cancellation called");
        if (this.spassProcess != null){
            this.logger.debug("Destroying SPASS process");
            this.spassProcess.destroyForcibly();
        }
        this.cancelled = true;
        super.cancel();
    }

    @Override
    public boolean successful() {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
