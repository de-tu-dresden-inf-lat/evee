package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.AbductionViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.man.cs.lethe.abduction.OWLAbducer;

import java.util.Set;

public class AbductionSolverThread extends Thread {

    private final LetheAbductionSolver abductionSolver;
    private final OWLAbducer abducer;
    private final Set<OWLAxiom> observation;

    private final Logger logger = LoggerFactory.getLogger(AbductionSolverThread.class);

    public AbductionSolverThread(LetheAbductionSolver abductionSolver, OWLAbducer abducer, Set<OWLAxiom> observation){
        super.setName("Evee Abduction Generation Thread");
        this.abductionSolver = abductionSolver;
        this.abducer = abducer;
        this.observation = observation;
    }

    public void run(){
        this.logger.debug("Abduction generation thread started");
        try{
            this.abductionSolver.computationCompleted(this.abducer.abduce(this.observation));
        }
        catch (Throwable e){
            this.logger.error("Error during abduction generation:\n" + e);
            this.abductionSolver.disposeLoadingScreen();
            this.abductionSolver.showError(e.toString());
        }
        finally{
            this.logger.debug("Abduction generation completed successfully");
            this.abductionSolver.disposeLoadingScreen();
            this.abductionSolver.showResults();
        }
    }

}