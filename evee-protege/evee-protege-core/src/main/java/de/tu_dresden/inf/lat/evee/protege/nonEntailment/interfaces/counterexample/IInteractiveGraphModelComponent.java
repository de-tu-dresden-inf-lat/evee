package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphView;

public interface IInteractiveGraphModelComponent extends IGraphModelControlPanel {

    void update(IGraphView graphView);

}
