package de.tu_dresden.inf.lat.evee.protege.abduction.letheBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction.AbstractAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.extended.DisjunctiveDLStatement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class LetheAbductionSolver
        extends AbstractAbductionSolver<DLStatement>
        implements Supplier<Set<OWLAxiom>> {

    private int maxLevel;
    private int currentResultAdapterIndex;
    private boolean computationSuccessful;
    private boolean computationRunning;
    private IProgressTracker progressTracker;
    private final OWLAbducer abducer;
    private final List<DLStatementAdapter> hypothesesAdapterList;
    private String errorMessage;

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolver.class);

    public LetheAbductionSolver(){
        super();
        this.logger.debug("Creating LetheAbductionSolver");
        this.abducer  = new OWLAbducer();
        this.hypothesesAdapterList = new ArrayList<>();
        this.maxLevel = 0;
        this.currentResultAdapterIndex = 0;
        this.computationSuccessful = false;
        this.computationRunning = false;
        this.errorMessage = "";
        this.logger.debug("LetheAbductionSolver created successfully");
    }

    @Override
    public void addProgressTracker(IProgressTracker tracker){
        this.progressTracker = tracker;
    }

    @Override
    public String getSupportsExplanationMessage() {
        return "Please enter some permitted vocabulary and missing entailment.";
    }

    @Override
    public Stream<Set<OWLAxiom>> generateExplanations() {
        this.logger.debug("Generating Explanations");
        DLStatement result;
        if (this.checkResultInCache()){
            this.logger.debug("Cached result found, re-displaying cached result");
            result = this.loadResultFromCache();
            this.explanationComputationCompleted(result);
            return Stream.generate(this);
        } else{
            this.logger.debug("No cached result found, trying to compute new explanation");
            try{
                this.abducer.setBackgroundOntology(this.ontology);
                this.abducer.setAbducibles(this.vocabulary);
                this.computationRunning = true;
                result = this.abducer.abduce(this.missingEntailment);
                this.computationRunning = false;
                this.logger.debug("Computation completed");
                this.explanationComputationCompleted(result);
                return Stream.generate(this);
            }
            catch (Throwable e) {
                this.errorMessage = "Error during abduction generation: " + e;
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String loggingString = stringWriter.toString();
                this.logger.error(loggingString);
                this.sendViewComponentEvent(ExplanationEventType.ERROR);
                return null;
            }
        }
    }

    @Override
    public boolean supportsExplanation() {
        return this.missingEntailment.size() != 0 && this.vocabulary.size() != 0;
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

    protected void explanationComputationCompleted(DLStatement hypotheses){
        if (((DisjunctiveDLStatement) hypotheses).statements().size() == 0){
            this.explanationComputationFailed("No result found, please adjust the vocabulary");
        }
        else{
            if (this.abducer.success()){
                this.logger.debug("Computation was not cancelled, preparing to show result");
                this.saveResultToCache(hypotheses);
                this.computationSuccessful = true;
                this.setActiveOntologyEditedExternally(false);
                this.maxLevel = 0;
                this.currentResultAdapterIndex = 0;
                this.hypothesesAdapterList.clear();
                ((DisjunctiveDLStatement) hypotheses).statements().foreach(statement -> {
                    this.hypothesesAdapterList.add(new DLStatementAdapter(statement, this.abducer));
                    return null;
                });
            } else {
                this.logger.debug("Computation was cancelled, cannot show result");
                this.computationSuccessful = false;
            }
        }
    }

    private void explanationComputationFailed(String errorMessage){
        this.setActiveOntologyEditedExternally(false);
        this.errorMessage = errorMessage;
        this.sendViewComponentEvent(ExplanationEventType.ERROR);
    }

    @Override
    public void cancel() {
        if (this.computationRunning){
            this.logger.debug("Cancelling computation");
            this.abducer.cancel();
        }
        super.cancel();
    }

    @Override
    public boolean successful() {
        return this.abducer.success();
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
