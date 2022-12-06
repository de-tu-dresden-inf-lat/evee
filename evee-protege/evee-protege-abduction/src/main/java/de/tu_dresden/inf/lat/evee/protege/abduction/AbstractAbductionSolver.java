package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.OWLAbductionSolver;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.AbstractNonEntailmentExplainer;

abstract public class AbstractAbductionSolver extends AbstractNonEntailmentExplainer implements OWLAbductionSolver {

    abstract public void abduce();

}
