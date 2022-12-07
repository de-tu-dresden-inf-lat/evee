package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;
import org.protege.editor.owl.OWLEditorKit;

public class NonEntailmentExplanationPlugin extends AbstractProtegePlugin<NonEntailmentExplanationService> {

    private final OWLEditorKit owlEditorKit;

//    todo: why should we use type and name??
//    public static final String TYPE_PARAM = "type";
//    public static final String NAME = "name";
    public static final String PLUGIN_ID = "de.tu-dresden.inf.lat.evee";
    public static final String EXTENSION_POINT_ID = "nonEntailmentExplainer";

    protected NonEntailmentExplanationPlugin(OWLEditorKit editorKit, IExtension extension) {
        super(extension);
        this.owlEditorKit = editorKit;
    }

//    public String getType() {
//        return getPluginProperty(TYPE_PARAM, "null type");
//    }
//
//    public String getName() {
//        return NAME;
//    }

    public NonEntailmentExplanationService newInstance() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        NonEntailmentExplanationService pluginInstance = super.newInstance();
        pluginInstance.setup(this.owlEditorKit);
        return pluginInstance;
    }

}
