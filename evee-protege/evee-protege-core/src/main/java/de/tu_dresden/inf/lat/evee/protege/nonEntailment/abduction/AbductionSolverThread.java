package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.INonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Stream;

public class AbductionSolverThread extends Thread implements IExplanationGenerator<Stream<Set<OWLAxiom>>> {

    private final IExplanationGenerationListener<
            ExplanationEvent<
                    IExplanationGenerator<
                            Stream<Set<OWLAxiom>>>>> abstractAbductionSolverListener;
    private final IOWLAbductionSolver internalSolver;
    private Stream<Set<OWLAxiom>> resultStream;
    private final Logger logger = LoggerFactory.getLogger(AbductionSolverThread.class);

    public AbductionSolverThread(IExplanationGenerationListener<
            ExplanationEvent<
                    IExplanationGenerator<
                            Stream<Set<OWLAxiom>>>>> abstractAbductionSolverListener,
                                 IOWLAbductionSolver internalSolver){
        super.setName("Evee Abduction Generation Thread");
        this.abstractAbductionSolverListener = abstractAbductionSolverListener;
        this.internalSolver = internalSolver;
    }

    @Override
    public Stream<Set<OWLAxiom>> getResult() {
        return this.resultStream;
    }

//    error-displaying needs to be done in implementing solvers
    @Override
    public String getErrorMessage() {
        return null;
    }

    public void run(){
        this.logger.debug("Running thread");
        this.resultStream = internalSolver.generateExplanations();
        this.abstractAbductionSolverListener.handleEvent(
                new ExplanationEvent<>(this,
                        ExplanationEventType.COMPUTATION_COMPLETE)
        );
    }
}
