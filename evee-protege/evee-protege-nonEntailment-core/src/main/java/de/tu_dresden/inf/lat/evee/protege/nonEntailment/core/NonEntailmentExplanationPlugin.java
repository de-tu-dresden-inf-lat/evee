package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;
import org.protege.editor.owl.OWLEditorKit;

public class NonEntailmentExplanationPlugin extends AbstractProtegePlugin<NonEntailmentExplanationService> {

    private final OWLEditorKit owlEditorKit;

    public static final String PLUGIN_ID = "de.tu_dresden.inf.lat.evee.protege.nonEntailment.core";
    public static final String EXTENSION_POINT_ID = "service";

    protected NonEntailmentExplanationPlugin(OWLEditorKit editorKit, IExtension extension) {
        super(extension);
        this.owlEditorKit = editorKit;
    }

    public NonEntailmentExplanationService newInstance() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        NonEntailmentExplanationService pluginInstance = super.newInstance();
        pluginInstance.setup(this.owlEditorKit);
        return pluginInstance;
    }

}
