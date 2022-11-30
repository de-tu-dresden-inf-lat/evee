package de.tu_dresden.inf.lat.evee.protege.abduction;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Stream;

public class AbductionSolverThread extends Thread {

    private final AbductionViewComponent viewComponent;
    private final OWLAbductionSolver abductionSolver;

    private final Logger logger = LoggerFactory.getLogger(AbductionSolverThread.class);

    public AbductionSolverThread(AbductionViewComponent viewComponent, OWLAbductionSolver abductionSolver){
        super.setName("Evee Abduction Generation Thread");
        this.viewComponent = viewComponent;
        this.abductionSolver = abductionSolver;
    }

    public void run(){
        this.logger.debug("Abduction generation thread started");
        assert(abductionSolver != null);
        try{
            this.abductionSolver.generateHypotheses();
        }
        catch (Exception e){
            this.logger.error("Error during abduction generation:\n" + e);
            this.viewComponent.abductionGenerationCompleted();
            this.viewComponent.showError(e.toString());
        }
        finally{
            this.logger.debug("Abduction generation completed successfully");
            this.viewComponent.abductionGenerationCompleted();
            this.viewComponent.showResults();
        }
    }

}
