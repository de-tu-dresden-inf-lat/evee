package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.interfaces;

import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.ExplanationGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.OWLNonEntailmentExplainer;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;

import java.awt.*;

public interface NonEntailmentExplanationService extends OWLNonEntailmentExplainer, ProtegePluginInstance, ExplanationGenerator<Component> {

    void setup(OWLEditorKit editorKit);

    /**
     * This method is called when the user hits the "compute"-button.
     */
    void computeExplanation();

    /**
     * @return Either null or a component that contains all necessary settings for the service.
     */
    Component getSettingsComponent();

    void registerListener(ExplanationGenerationListener<ExplanationEvent<NonEntailmentExplanationService>> listener);

}
