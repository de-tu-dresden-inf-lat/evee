package de.tu_dresden.inf.lat.evee.protege.abduction.letheBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

public class LetheAbductionSolverThread extends Thread implements IExplanationGenerator<DLStatement> {

    private final IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<DLStatement>>> explanationGenerationListener;
    private final OWLAbducer abducer;
    private final Set<OWLAxiom> observation;
    private IProgressTracker progressTracker = null;
    private DLStatement result = null;
    private String errorMessage = "";

    private final Logger logger = LoggerFactory.getLogger(LetheAbductionSolverThread.class);

    public LetheAbductionSolverThread(IExplanationGenerationListener<ExplanationEvent<IExplanationGenerator<DLStatement>>> explanationGenerationListener, OWLAbducer abducer, Set<OWLAxiom> observation){
        super.setName("Evee Abduction Generation Thread");
        this.explanationGenerationListener = explanationGenerationListener;
        this.abducer = abducer;
        this.observation = observation;
    }

    public void setProgressTracker(IProgressTracker progressTracker){
        this.progressTracker = progressTracker;
    }

    public void run(){
        this.logger.debug("Abduction generation thread started");
        try{
            this.result = this.abducer.abduce(this.observation);
            this.logger.debug("Abduction generation completed successfully");
//        this.explanationGenerationListener.newExplanationComputationCompleted();
//        this.explanationGenerationListener.disposeLoadingScreen();
            this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
                    this, ExplanationEventType.COMPUTATION_COMPLETE));
        }
        catch (Throwable e){
            this.logger.error("Error during abduction generation:", e);
            this.errorMessage = e.getMessage();
            this.explanationGenerationListener.handleEvent(new ExplanationEvent<>(
                    this, ExplanationEventType.ERROR));
//            this.explanationGenerationListener.disposeLoadingScreen();
//            this.explanationGenerationListener.showError(e.toString());
        }
    }

    @Override
    public DLStatement getResult() {
        return this.result;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
