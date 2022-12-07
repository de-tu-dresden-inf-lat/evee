package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.owl.OWLEditorKit;

public class NonEntailmentExplanationPluginLoader extends AbstractPluginLoader<NonEntailmentExplanationPlugin> {

    private final OWLEditorKit owlEditorKit;

    public NonEntailmentExplanationPluginLoader(OWLEditorKit editorKit) {
        super(NonEntailmentExplanationPlugin.PLUGIN_ID,
                NonEntailmentExplanationPlugin.EXTENSION_POINT_ID);
        this.owlEditorKit = editorKit;
    }

    @Override
    protected NonEntailmentExplanationPlugin createInstance(IExtension iExtension) {
        return new NonEntailmentExplanationPlugin(this.owlEditorKit, iExtension);
    }

}
