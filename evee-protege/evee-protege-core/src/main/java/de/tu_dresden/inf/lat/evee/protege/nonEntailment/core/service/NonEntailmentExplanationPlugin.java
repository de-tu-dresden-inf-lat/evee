package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;
import org.protege.editor.owl.OWLEditorKit;

public class NonEntailmentExplanationPlugin extends AbstractProtegePlugin<INonEntailmentExplanationService<?>> {

    private final OWLEditorKit owlEditorKit;

    public static final String NAME_PARAM = "name";
    public static final String PLUGIN_ID = "de.tu_dresden.inf.lat.evee";
    public static final String EXTENSION_POINT_ID = "nonEntailment_explanation_service";

    protected NonEntailmentExplanationPlugin(OWLEditorKit editorKit, IExtension extension) {
        super(extension);
        this.owlEditorKit = editorKit;
    }

    public String getName() {
        return getPluginProperty(NAME_PARAM, "Non-Entailment Explanation Service");
    }

    public INonEntailmentExplanationService<?> newInstance() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        INonEntailmentExplanationService<?> pluginInstance = super.newInstance();
        pluginInstance.setup(this.owlEditorKit);
        return pluginInstance;
    }

}
