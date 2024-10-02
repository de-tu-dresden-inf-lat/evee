package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonEntailmentExplanationPlugin extends AbstractProtegePlugin<INonEntailmentExplanationService<?>> {

    private final OWLEditorKit owlEditorKit;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationPlugin.class);

    public static final String NAME_PARAM = "name";
//    PLUGIN_ID needs to be bundle-id from pom in order to correctly define extension points
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
        this.logger.debug(
                "Creating new instance of plugin <{}> for pluginId.extensionPoint <{}.{}>",
                this.getName(), PLUGIN_ID, EXTENSION_POINT_ID);
        INonEntailmentExplanationService<?> pluginInstance = super.newInstance();
        return pluginInstance;
    }

}
