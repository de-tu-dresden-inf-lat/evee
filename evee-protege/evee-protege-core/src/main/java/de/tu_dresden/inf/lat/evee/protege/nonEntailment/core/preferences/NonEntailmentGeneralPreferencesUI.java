package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.preferences;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.core.VocabularyTab;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Vector;

public class NonEntailmentGeneralPreferencesUI extends OWLPreferencesPanel {

    private JComboBox<VocabularyTab> defaultTabComboBox;
    private final NonEntailmentGeneralPreferencesManager preferencesManager;
    private static final String COMBO_BOX_LABEL = "Automatically add new entities to vocabulary:";
    private static final String COMBO_BOX_TOOL_TIP = "Select the vocabulary tab to which new entities should be added.";

    public NonEntailmentGeneralPreferencesUI(){
        this.preferencesManager = new NonEntailmentGeneralPreferencesManager();
    }

    @Override
    public void applyChanges() {
        this.preferencesManager.saveDefaultVocabularyTab(
                (VocabularyTab) Objects.requireNonNull(
                        this.defaultTabComboBox.getSelectedItem()));
    }

    @Override
    public void initialise() throws Exception {
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel holderPanel = new PreferencesLayoutPanel();
            this.add(holderPanel, BorderLayout.NORTH);
            holderPanel.addGroup(COMBO_BOX_LABEL);
            Vector<VocabularyTab> comboBoxEntries = new Vector<>();
            comboBoxEntries.add(VocabularyTab.Permitted);
            comboBoxEntries.add(VocabularyTab.Forbidden);
            this.defaultTabComboBox = new JComboBox<>(comboBoxEntries);
            this.defaultTabComboBox.setSelectedItem(
                    this.preferencesManager.loadDefaultVocabularyTab());
            this.defaultTabComboBox.setToolTipText(COMBO_BOX_TOOL_TIP);
            holderPanel.addGroupComponent(this.defaultTabComboBox);
        });
    }

    @Override
    public void dispose() throws Exception {

    }
}
