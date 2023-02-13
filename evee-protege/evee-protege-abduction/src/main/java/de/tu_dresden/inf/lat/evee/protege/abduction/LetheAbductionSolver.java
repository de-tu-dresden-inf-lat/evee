package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbductionLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbstractAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.DisjunctiveDLStatement;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.List;

public class LetheAbductionSolver extends AbstractAbductionSolver implements ExplanationGenerationListener<ExplanationEvent<ExplanationGenerator<DLStatement>>> {

    private OWLEditorKit owlEditorKit;
    private int maxLevel;
    private int currentResultAdapterIndex;
    private boolean computationSuccessful;
    private String errorMessage = "";
    private final OWLAbducer abducer;
    private final List<DLStatementAdapter> hypothesesAdapterList;
    private final Map<OWLOntology, DLStatementCache> cachedResults;
    private final LetheAbductionSolverOntologyChangeListener changeListener;
    private final static String LOADING = "LOADING";

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        super();
        this.logger.debug("Creating LetheAbductionSolver");
        this.abducer  = new OWLAbducer();
        this.changeListener = new LetheAbductionSolverOntologyChangeListener();
        this.hypothesesAdapterList = new ArrayList<>();
        this.cachedResults = new HashMap<>();
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.computationSuccessful = false;
        this.logger.debug("LetheAbductionSolver created successfully");
    }

    @Override
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
        super.setup(editorKit);
    }

    @Override
    public void initialise(){
        this.logger.debug("Initialising LetheAbductionSolver");
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.changeListener);
        this.resetCache();
        this.logger.debug("LetheAbductionSolver initialised successfully");
    }

    @Override
    public void dispose() {
        this.owlEditorKit.getOWLModelManager().removeOntologyChangeListener(this.changeListener);
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        super.setOntology(ontology);
        if (this.cachedResults.get(ontology) == null){
            this.cachedResults.put(ontology, new DLStatementCache());
        }
    }

    @Override
    public void handleEvent(ExplanationEvent<ExplanationGenerator<DLStatement>> event){
        this.disposeLoadingScreen();
        switch (event.getType()){
            case COMPUTATION_COMPLETE :
                this.explanationComputationCompleted(event.getSource().getResult());
                break;
            case ERROR :
                this.explanationComputationFailed(event.getSource().getErrorMessage());
                break;
        }
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public Set<OWLAxiom> get() {
        if (! this.computationSuccessful){
            this.logger.debug("Last computation did not end successfully, cannot return result");
            return null;
        }
        int startIndex = this.currentResultAdapterIndex;
        boolean checkedStartIndex = false;
        while (true){
            if (this.currentResultAdapterIndex == this.hypothesesAdapterList.size()){
                this.maxLevel += 1;
                this.currentResultAdapterIndex = 0;
                for (DLStatementAdapter converter : this.hypothesesAdapterList){
                    if (! converter.singletonResult()){
                        converter.setMaxLevel(this.maxLevel);
                        converter.createNextLevelList();
                    }
                }
            }
            Set<OWLAxiom> result = this.hypothesesAdapterList.get(this.currentResultAdapterIndex).getNextConversion();
            this.currentResultAdapterIndex += 1;
            if (result == null){
//                check if we tried all statements
                if (startIndex == this.currentResultAdapterIndex)
                {
                    if (checkedStartIndex){
                        break;
                    }
                    else {
                        checkedStartIndex = true;
                    }
                }
            }
            else{
                return result;
            }
        }
        return null; // TODO returning null should be avoided - maybe this method can be changed in a way that
                     // we get an exception (no explanation), an empty set (empty explanation), or an optional
                     // (in case it is not exceptional to not have an explanation)
    }


    @Override
    public void computeExplanation() {
        if (this.parametersChanged){
            this.logger.debug("Parameters changed, creating new stream");
            if (this.cachedResults.get(this.activeOntology).containsStatement(this.observation, this.abducibles)){
                this.logger.debug("Cached result found, no computation of hypotheses necessary");
                this.computationSuccessful = true;
                this.prepareResultComponentCreation();
                this.createResultComponent();
            }
            else{
                this.logger.debug("No cached result found, computing new hypotheses");
                this.computeNewExplanation();
            }
            this.hypothesisIndex = 1;
            this.parametersChanged = false;
        }
        else{
            this.logger.debug("Parameters unchanged");
            if (this.computationSuccessful){
                this.logger.debug("Last computation was successful, continuing old stream");
                this.createResultComponent();
            }
            else{
                this.logger.debug("Last computation failed, re-displaying error message");
                this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                        ExplanationEventType.ERROR));
            }
        }
    }

    protected void computeNewExplanation() {
        assert (this.activeOntology != null);
        assert (this.observation != null);
        assert (this.abducibles != null);
        this.abducer.setBackgroundOntology(this.activeOntology);
        this.abducer.setAbducibles(this.abducibles);
        AbductionSolverThread thread = new AbductionSolverThread(this, this.abducer, this.observation);
        this.loadingUI = new AbductionLoadingUI(LOADING, this.owlEditorKit);
        this.loadingUI.showLoadingScreen();
        thread.start();
    }

    protected void explanationComputationCompleted(DLStatement hypotheses){
        if (((DisjunctiveDLStatement) hypotheses).statements().size() == 0){
            this.explanationComputationFailed("No result found, please adjust the vocabulary");
        }
        else{
            this.computationSuccessful = true;
            this.cachedResults.get(this.activeOntology).putStatement(this.observation, this.abducibles, hypotheses);
            this.prepareResultComponentCreation();
            this.createResultComponent();
        }
    }

    private void explanationComputationFailed(String errorMessage){
        this.computationSuccessful = false;
        this.errorMessage = errorMessage;
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                ExplanationEventType.ERROR));
    }

    protected void prepareResultComponentCreation(){
        this.resetResultComponent();
        DLStatement hypotheses = this.cachedResults.get(this.activeOntology).getStatement(
                this.observation, this.abducibles);
        assert (hypotheses != null);
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.hypothesesAdapterList.clear();
        ((DisjunctiveDLStatement) hypotheses).statements().foreach(statement -> {
            this.hypothesesAdapterList.add(new DLStatementAdapter((ConjunctiveDLStatement) statement));
            return null;
        });
    }

    private void resetCache(){
        OWLOntology ontology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        this.logger.debug("Resetting DLStatementCache for ontology " + ontology.getOntologyID().getOntologyIRI());
        DLStatementCache newCache = new DLStatementCache();
        this.cachedResults.put(ontology, newCache);
        this.parametersChanged = true;
    }

    private class LetheAbductionSolverOntologyChangeListener implements OWLOntologyChangeListener {

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            resetCache();
        }
    }

}
