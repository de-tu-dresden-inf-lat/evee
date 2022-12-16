package de.tu_dresden.inf.lat.evee.protege.nonEntailment.service;

import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.OWLNonEntailmentExplainer;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;

import java.awt.*;

public interface NonEntailmentExplanationService extends OWLNonEntailmentExplainer, ProtegePluginInstance {

    void setup(OWLEditorKit editorKit);

    /**
     * This method is called when the user hits the "compute"-button.
     */
    void computeExplanation();

    /**
     * @return The component which is shown as the result of the computation performed by the service.
     */
    Component getResultComponent();

    /**
     * @return Either null or a component that contains all necessary settings for the service.
     */
    Component getSettingsComponent();

    void registerListener(NonEntailmentExplanationListener listener);

}
