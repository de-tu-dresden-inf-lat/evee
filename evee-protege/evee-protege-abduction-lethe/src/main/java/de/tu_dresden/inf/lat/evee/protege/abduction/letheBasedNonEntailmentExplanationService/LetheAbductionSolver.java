package de.tu_dresden.inf.lat.evee.protege.abduction.letheBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbductionLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbstractAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.ConjunctiveDLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.DisjunctiveDLStatement;

import java.util.*;
import java.util.List;

public class LetheAbductionSolver extends AbstractAbductionSolver<DLStatement> implements IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<DLStatement>>> {

    private OWLEditorKit owlEditorKit;
    private int maxLevel;
    private int currentResultAdapterIndex;
    private String errorMessage = "";
    private final OWLAbducer abducer;
    private final List<DLStatementAdapter> hypothesesAdapterList;
    private final static String LOADING = "LOADING";

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        super();
        this.logger.debug("Creating LetheAbductionSolver");
        this.abducer  = new OWLAbducer();
        this.hypothesesAdapterList = new ArrayList<>();
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.setComputationSuccessful(false);
        this.logger.debug("LetheAbductionSolver created successfully");
    }

    @Override
    public void setup(OWLEditorKit editorKit) {
        this.owlEditorKit = editorKit;
        super.setup(editorKit);
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter some signature and observation";
    }

    @Override
    public void setOntology(OWLOntology ontology) {
        super.setOntology(ontology);

    }

    @Override
    public boolean supportsExplanation() {
        return this.observation.size() != 0 && this.abducibles.size() != 0;
    }

    @Override
    public void handleEvent(ExplanationEvent<IExplanationGenerator<DLStatement>> event){
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
        if (! this.computationSuccessful()){
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
    protected void redisplayCachedExplanation(){
        this.prepareResultComponentCreation();
        this.createResultComponent();
    }

    @Override
    protected void createNewExplanation() {
        this.abducer.setBackgroundOntology(this.ontology);
        this.abducer.setAbducibles(this.abducibles);
        LetheAbductionSolverThread thread = new LetheAbductionSolverThread(this, this.abducer, this.observation);
        this.loadingUI = new AbductionLoadingUI(LOADING, this.owlEditorKit);
        this.loadingUI.showLoadingScreen();
        thread.start();
    }

    protected void explanationComputationCompleted(DLStatement hypotheses){
        if (((DisjunctiveDLStatement) hypotheses).statements().size() == 0){
            this.explanationComputationFailed("No result found, please adjust the vocabulary");
        }
        else{
            this.setComputationSuccessful(true);
            this.cachedResults.get(this.ontology).putResult(this.observation, this.abducibles, hypotheses);
            this.validateCache();
            this.prepareResultComponentCreation();
            this.createResultComponent();
        }
    }

    private void explanationComputationFailed(String errorMessage){
        this.setComputationSuccessful(false);
        this.errorMessage = errorMessage;
        this.viewComponentListener.handleEvent(new ExplanationEvent<>(this,
                ExplanationEventType.ERROR));
    }

    @Override
    protected void prepareResultComponentCreation(){
        this.resetResultComponent();
        DLStatement hypotheses = this.cachedResults.get(this.ontology).getResult(
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


}
