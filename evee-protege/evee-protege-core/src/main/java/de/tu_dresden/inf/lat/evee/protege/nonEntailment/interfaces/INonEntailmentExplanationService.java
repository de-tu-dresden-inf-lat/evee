package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import java.awt.*;
import java.util.Set;

public interface INonEntailmentExplanationService<T extends OWLAxiom> extends
        ProtegePluginInstance, IExplanationGenerator<Component>,
        ISignatureModificationEventGenerator,
        IOWLNonEntailmentExplainer<T> {

    void setup(OWLEditorKit editorKit);

    /**
     * This method is called when the user hits the "compute"-button.
     */
    void computeExplanation();

    void registerListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener);

    /**
     * @return A message informing the user what conditions the 'Observation', 'Symbol' and 'Ontology' (of
     * IOWLAbductionSolver/IOWLModelGenerator) should satisfy in order for the Explainer to return a result.
     */
    String getSupportsExplanationMessage();

    String getFilterWarningMessage();

    void repaintResultComponent();

}
