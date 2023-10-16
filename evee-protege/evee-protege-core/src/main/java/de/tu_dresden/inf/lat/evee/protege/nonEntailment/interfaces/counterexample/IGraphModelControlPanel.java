package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.Set;

public interface IGraphModelControlPanel extends IInteractiveComponent {
    void addCounterexampleGenerationEventListener(ICounterexampleGenerationEventListener listener);
    void removeCurrentCounterexampleGenerationEventListener();
    int getCurrentLabelsNum();
    Set<OWLAxiom> getAdditionalAxioms();
    void selectNode(String nodeId);
    void refreshSelectedClasses(Collection<OWLClass> selection);

}
