package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;

import java.util.Set;

public class AbductionSolverThread extends Thread implements ExplanationGenerator<DLStatement> {

    private final ExplanationGenerationListener<ExplanationEvent<ExplanationGenerator<DLStatement>>> explanationGenerationListener;
    private final OWLAbducer abducer;
    private final Set<OWLAxiom> observation;
    private DLStatement result = null;
    private String errorMessage = "";

    private final Logger logger = LoggerFactory.getLogger(AbductionSolverThread.class);

    public AbductionSolverThread(ExplanationGenerationListener<ExplanationEvent<ExplanationGenerator<DLStatement>>> explanationGenerationListener, OWLAbducer abducer, Set<OWLAxiom> observation){
        super.setName("Evee Abduction Generation Thread");
        this.explanationGenerationListener = explanationGenerationListener;
        this.abducer = abducer;
        this.observation = observation;
    }

    public void run(){
        this.logger.debug("Abduction generation thread started");
        try{
            this.result = this.abducer.abduce(this.observation);
            this.logger.debug("Abduction generation completed successfully");
//        this.explanationGenerationListener.newExplanationComputationCompleted();
//        this.explanationGenerationListener.disposeLoadingScreen();
            this.explanationGenerationListener.handleEvent(new ExplanationEvent<ExplanationGenerator<DLStatement>>(
                    this, ExplanationEventType.COMPUTATION_COMPLETE));
        }
        catch (Throwable e){
            this.logger.error("Error during abduction generation:\n" + e);
            this.errorMessage = e.getMessage();
            this.explanationGenerationListener.handleEvent(new ExplanationEvent<ExplanationGenerator<DLStatement>>(
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
