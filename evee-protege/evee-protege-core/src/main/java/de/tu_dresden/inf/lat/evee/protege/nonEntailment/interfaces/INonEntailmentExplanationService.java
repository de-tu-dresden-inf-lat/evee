package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.general.interfaces.IHasProgressTracker;
import de.tu_dresden.inf.lat.evee.general.interfaces.IIsCancellable;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.awt.*;

public interface INonEntailmentExplanationService<T extends OWLAxiom> extends ProtegePluginInstance,
        IExplanationGenerator<Component>, IOWLNonEntailmentExplainer<T> {

    void setup(OWLEditorKit editorKit);

    /**
     * This method is called when the user hits the "compute"-button.
     */
    void computeExplanation();

    /**
     * @return Either null or a component that contains all necessary settings for the service.
     */
    Component getSettingsComponent();

    void registerListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener);

    /**
     * @return A message informing the user what conditions the 'Observation', 'Symbol' and 'Ontology' (of
     * IOWLAbductionSolver/IOWLModelGenerator) should satisfy in order for the Explainer to return a result.
     */
    String getSupportsExplanationMessage();

}
