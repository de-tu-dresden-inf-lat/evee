package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences;

import org.protege.editor.core.ui.preferences.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

public class NonEntailmentPreferencesPanel extends PreferencesPanel {

    private final Map<String, PreferencesPanel> pluginMap;
    private final JTabbedPane tabbedPane;

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentPreferencesPanel.class);

    public NonEntailmentPreferencesPanel(){
        super();
        this.pluginMap = new TreeMap<>();
        this.tabbedPane = new JTabbedPane();
    }

    @Override
    public void initialise() throws Exception {
        this.setLayout(new BorderLayout());
        NonEntailmentPreferencesPanelPluginLoader loader = new NonEntailmentPreferencesPanelPluginLoader(this.getEditorKit());
        Set<NonEntailmentPreferencesPanelPlugin> plugins = new TreeSet<>(
                Comparator.comparing(NonEntailmentPreferencesPanelPlugin::getLabel));
        plugins.addAll(loader.getPlugins());
        for (NonEntailmentPreferencesPanelPlugin plugin : plugins) {
            try {
                PreferencesPanel singlePanel = plugin.newInstance();
                singlePanel.initialise();
                String label = plugin.getLabel();
                JScrollPane scrollPane = new JScrollPane(singlePanel);
                scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
                this.pluginMap.put(label, singlePanel);
                this.tabbedPane.addTab(label, scrollPane);
            } catch (Throwable e) {
                this.logger.warn(
                        "The following error occurred during the instantiation of the non-entailment preferences plugin " +
                                "'{}': {}",
                        plugin.getLabel(), e);
            }
        }
        this.add(tabbedPane);
    }

    @Override
    public void dispose() throws Exception {
        for (String key : this.pluginMap.keySet()){
            try {
                this.pluginMap.get(key).dispose();
            } catch (Throwable e){
                this.logger.error("The following error occurred when trying to dispose the non-entailment preferences plugin" +
                        "'{}: {}", key, e);
                this.logger.error("", e);
            }
        }
        this.pluginMap.clear();
    }

    @Override
    public void applyChanges() {
        for (String key : this.pluginMap.keySet()){
            try{
                this.pluginMap.get(key).applyChanges();
            } catch (Throwable e){
                this.logger.error("The following error occurred when trying to apply the changes of the non-entailment preferences plugin" +
                        "'{}': {}", key, e);
                this.logger.error("", e);
            }
        }
    }

}
