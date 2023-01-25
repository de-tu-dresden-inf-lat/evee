package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.core.plugin.DefaultPluginExtensionMatcher;
import org.protege.editor.core.plugin.PluginExtensionMatcher;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonEntailmentExplanationPluginLoader extends AbstractPluginLoader<NonEntailmentExplanationPlugin> {

    private final OWLEditorKit owlEditorKit;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationPluginLoader.class);

    public NonEntailmentExplanationPluginLoader(OWLEditorKit editorKit) {
        super(NonEntailmentExplanationPlugin.PLUGIN_ID,
                NonEntailmentExplanationPlugin.EXTENSION_POINT_ID);
        this.owlEditorKit = editorKit;
    }

    @Override
    protected PluginExtensionMatcher getExtensionMatcher() {
        return new DefaultPluginExtensionMatcher();
    }

    @Override
    protected NonEntailmentExplanationPlugin createInstance(IExtension iExtension) {
        return new NonEntailmentExplanationPlugin(this.owlEditorKit, iExtension);
    }

}
