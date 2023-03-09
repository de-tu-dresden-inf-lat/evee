package de.tu_dresden.inf.lat.evee.protege.abduction.capiBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbductionLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbstractAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import de.tu_dresden.lat.capi.implicateMatching.ClassName;
import de.tu_dresden.lat.capi.implicateMatching.Solution;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

public class CapiAbductionSolver extends AbstractAbductionSolver<List<Solution>> implements IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<List<Solution>>>> {

    private String errorMessage = "";
    private OWLSubClassOfAxiom singleObservation;
    private List<Solution> solutions;
    private OWLEditorKit owlEditorKit;
    private int currentSolutionIndex;
    private String lastUsedSpassPath = "";
    private int lastUsedTimeLimit = -1;
    private boolean lastUsedRemoveRedundancies;
    private boolean lastUsedSimplifyConjunctions;
    private boolean lastUsedSemanticallyOrdered;
    private final CapiPreferencesManager preferencesManager;
    private final static String LOADING = "LOADING";
    private static final String EMPTY_SPASS_PATH = "<html>No path to SPASS is set.<br>Please set a path to the SPASS executable and hit 'Compute' again</html>";
    private final Logger logger = LoggerFactory.getLogger(CapiAbductionSolver.class);

    public CapiAbductionSolver(){
        super();
        this.logger.debug("Creating CapiAbductionSolver");
        this.solutions = new ArrayList<>();
        this.setComputationSuccessful(false);
        this.currentSolutionIndex = 0;
        this.singleObservation = null;
        this.preferencesManager = new CapiPreferencesManager();
        this.logger.debug("CapiAbductionSolver created successfully");
    }

    @Override
    public void setup(OWLEditorKit editorKit){
        this.owlEditorKit = editorKit;
        super.setup(editorKit);
    }

    @Override
    public boolean supportsExplanation() {
//        todo: currently signature is completely ignored -> consider signature once functionality is enabled by capi
        if (this.observation.size() != 1){
            return false;
        } else {
            return (new ArrayList<>(this.observation)).get(0) instanceof OWLSubClassOfAxiom;
        }
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void computeExplanation() {
        this.logger.debug("Explanation computation started");
        if (this.preferencesManager.loadSpassPath().length() == 0){
            this.showSpassPathDialog();
        } else {
            if (this.capiParametersChanged()){
                this.logger.debug("Parameters for CAPI have changed");
                this.resetCompleteCache();
            }
            super.computeExplanation();
        }
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter a single 'SubClassOfAxiom' as observation.";
    }

    @Override
    protected void redisplayCachedExplanation() {
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    @Override
    protected void createNewExplanation() {
        assertNotNull(this.ontology);
        assert (this.observation.size() != 0);
        final ArrayList<OWLSubClassOfAxiom> observationList = new ArrayList<>();
        this.observation.forEach(axiom -> {
            if (axiom instanceof OWLSubClassOfAxiom)
                observationList.add((OWLSubClassOfAxiom) axiom);
        });
        this.singleObservation = observationList.get(0);
        this.lastUsedSpassPath = this.preferencesManager.loadSpassPath();
        this.lastUsedTimeLimit = this.preferencesManager.loadTimeLimit();
        this.lastUsedRemoveRedundancies = this.preferencesManager.loadRemoveRedundancies();
        this.lastUsedSimplifyConjunctions = this.preferencesManager.loadSimplifyConjunctions();
        this.lastUsedSemanticallyOrdered = this.preferencesManager.loadSemanticallyOrdered();
        CapiAbductionSolverThread thread = new CapiAbductionSolverThread(this,
                this.ontology, this.singleObservation);
        thread.setSpassPath(this.lastUsedSpassPath);
        thread.setTimeLimit(this.lastUsedTimeLimit);
        thread.setRemoveRedundancies(this.lastUsedRemoveRedundancies);
        thread.setSimplifyConjunctions(this.lastUsedSimplifyConjunctions);
        thread.setSemanticallyOrdered(this.lastUsedSemanticallyOrdered);
        this.loadingUI = new AbductionLoadingUI(LOADING, this.owlEditorKit);
        this.loadingUI.showLoadingScreen();
        thread.start();
    }


    @Override
    public Set<OWLAxiom> get() {
        if (! this.computationSuccessful()){
            this.logger.debug("Last computation did not end successfully, cannot return result");
            return null;
        } else {
//            this.logger.debug("soltuionIndex: {} - solutions.size: {}", this.currentSolutionIndex, this.solutions.size());
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

    @Override
    public void handleEvent(ExplanationEvent<IExplanationGenerator<List<Solution>>> event) {
        this.disposeLoadingScreen();
        switch (event.getType()){
            case COMPUTATION_COMPLETE :
                this.computationSuccessful(event.getSource().getResult());
                break;
            case ERROR :
                this.computationFailed(event.getSource().getErrorMessage());
                break;
        }
    }

    private void computationSuccessful(List<Solution> solutions){
        this.setComputationSuccessful(true);
        this.cachedResults.get(this.ontology).putResult(this.observation, this.abducibles, solutions);
        this.setResultValid(true);
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    private void computationFailed(String errorMessage){
        this.setComputationSuccessful(false);
        this.setResultValid(true);
        this.errorMessage = errorMessage;
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(
                this, ExplanationEventType.ERROR));
    }

    @Override
    protected void prepareResultComponentCreation(){
        this.resetResultComponent();
        this.solutions = this.cachedResults.get(this.ontology).getResult(this.observation, this.abducibles);
        this.currentSolutionIndex = 0;
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
//            warningDialog.
        });
    }

    @Override
    protected boolean parametersChanged(){
        return super.parametersChanged() || this.capiParametersChanged();
    }

    protected boolean capiParametersChanged(){
        return (!this.lastUsedSpassPath.equals(this.preferencesManager.loadSpassPath())) ||
                (!(this.lastUsedTimeLimit == this.preferencesManager.loadTimeLimit())) ||
                (!(this.lastUsedRemoveRedundancies == this.preferencesManager.loadRemoveRedundancies())) ||
                (!(this.lastUsedSimplifyConjunctions == this.preferencesManager.loadSimplifyConjunctions())) ||
                (!(this.lastUsedSemanticallyOrdered == this.preferencesManager.loadSemanticallyOrdered()));
    }

}
