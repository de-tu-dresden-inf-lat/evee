package de.tu_dresden.inf.lat.evee.protege.nonEntailment.preferences;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.plugin.AbstractProtegePlugin;
import org.protege.editor.core.ui.preferences.PreferencesPanel;
import org.protege.editor.owl.OWLEditorKit;

public class NonEntailmentPreferencesPanelPlugin extends AbstractProtegePlugin<PreferencesPanel> {

    private final EditorKit editorKit;

    public static final String LABEL_PARAM = "label";
    public static final String PLUGIN_ID = "de.tu_dresden.inf.lat.evee";
    public static final String EXTENSION_POINT_ID = "nonEntailment_explanation_service_preferences";

    protected NonEntailmentPreferencesPanelPlugin(EditorKit editorKit, IExtension extension) {
        super(extension);
        this.editorKit = editorKit;
    }

    @Override
    public String getLabel() {
        return getPluginProperty(LABEL_PARAM);
    }

    @Override
    public PreferencesPanel newInstance() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        PreferencesPanel pluginInstance = super.newInstance();
        pluginInstance.setup(this.getLabel(), this.editorKit);
        return pluginInstance;
    }

}
