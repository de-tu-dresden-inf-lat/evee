package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.service.NonEntailmentExplanationPluginLoader;
import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.core.plugin.DefaultPluginExtensionMatcher;
import org.protege.editor.core.plugin.PluginExtensionMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonEntailmentPreferencesPanelPluginLoader extends AbstractPluginLoader<NonEntailmentPreferencesPanelPlugin> {

    private final EditorKit editorKit;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentExplanationPluginLoader.class);

    public NonEntailmentPreferencesPanelPluginLoader(EditorKit editorKit) {
        super(NonEntailmentPreferencesPanelPlugin.PLUGIN_ID,
                NonEntailmentPreferencesPanelPlugin.EXTENSION_POINT_ID);
        this.editorKit = editorKit;
    }

    @Override
    protected PluginExtensionMatcher getExtensionMatcher() {
        return new DefaultPluginExtensionMatcher();
    }

    @Override
    protected NonEntailmentPreferencesPanelPlugin createInstance(IExtension iExtension) {
        return new NonEntailmentPreferencesPanelPlugin(this.editorKit, iExtension);
    }

}
