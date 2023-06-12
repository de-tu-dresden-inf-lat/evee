package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;
import java.util.stream.Stream;

public class AbductionSolverThread extends Thread implements IExplanationGenerator<Stream<Set<OWLAxiom>>> {

    private final IExplanationGenerationListener<
            ExplanationEvent<
                    IExplanationGenerator<
                            Stream<Set<OWLAxiom>>>>> abstractAbductionSolverListener;
    private final AbstractAbductionSolver<?> abductionSolver;
    private Stream<Set<OWLAxiom>> resultStream;

    public AbductionSolverThread(IExplanationGenerationListener<
            ExplanationEvent<
                    IExplanationGenerator<
                            Stream<Set<OWLAxiom>>>>> abstractAbductionSolverListener,
                                 AbstractAbductionSolver<?> abductionSolver){
        super.setName("Evee Abduction Generation Thread");
        this.abstractAbductionSolverListener = abstractAbductionSolverListener;
        this.abductionSolver = abductionSolver;
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
        this.resultStream = abductionSolver.generateExplanations();
        this.abstractAbductionSolverListener.handleEvent(
                new ExplanationEvent<>(this,
                        ExplanationEventType.COMPUTATION_COMPLETE)
        );
    }
}
