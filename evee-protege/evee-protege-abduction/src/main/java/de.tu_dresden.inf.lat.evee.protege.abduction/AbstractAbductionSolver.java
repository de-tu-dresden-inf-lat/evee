package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLAbductionSolver;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;

abstract public class AbstractAbductionSolver implements OWLAbductionSolver {

    abstract public void abduce();

}
