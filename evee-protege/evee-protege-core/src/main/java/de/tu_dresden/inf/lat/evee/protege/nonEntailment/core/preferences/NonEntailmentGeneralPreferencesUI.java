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
    private JCheckBox showFilterWarningCheckBox;
    private JComboBox<String> vocabularyLayoutComboBox;
    private JCheckBox useSimpleModeCheckBox;
    private final NonEntailmentGeneralPreferencesManager preferencesManager;
    private static final String COMBO_BOX_LABEL = "Automatically add new entities to vocabulary:";
    private static final String COMBO_BOX_TOOL_TIP = "Select the vocabulary tab to which new entities should be added.";
    private static final String FILTER_WARNING_LABEL = "Show Filter Warning Popup:";
    private static final String FILTER_WARNING_TOOL_TIP = "Shows a Popup Warning Message if the ontology was filtered before computation";
    private static final String VOCABULARY_TAB_LAYOUT_LABEL = "Vocabulary tab layout:";
    private static final String VOCABULARY_TAB_LAYOUT_TIP = "Determines the layout of the permitted and forbidden vocabulary tab";
    private static final String USE_SIMPLE_MODE_LABEL = "Use simple mode";
    private static final String USE_SIMPLE_MODE_TOOL_TIP = "Use a simplified version of the UI";


    public NonEntailmentGeneralPreferencesUI(){
        this.preferencesManager = NonEntailmentGeneralPreferencesManager.getInstance();
    }

    @Override
    public void applyChanges() {
        this.preferencesManager.saveDefaultVocabularyTab(
                (VocabularyTab) Objects.requireNonNull(
                        this.defaultTabComboBox.getSelectedItem()));
        this.preferencesManager.saveShowFilterWarningMessage(
                this.showFilterWarningCheckBox.isSelected());
        this.preferencesManager.saveSignatureComponentLayout(
                (String) this.vocabularyLayoutComboBox.getSelectedItem());
        this.preferencesManager.saveUseSimpleMode(
                this.useSimpleModeCheckBox.isSelected());
    }

    @Override
    public void initialise() throws Exception {
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel holderPanel = new PreferencesLayoutPanel();
            this.add(holderPanel, BorderLayout.NORTH);
//            defaultTab
            holderPanel.addGroup(COMBO_BOX_LABEL);
            Vector<VocabularyTab> comboBoxEntries = new Vector<>();
            comboBoxEntries.add(VocabularyTab.Permitted);
            comboBoxEntries.add(VocabularyTab.Forbidden);
            this.defaultTabComboBox = new JComboBox<>(comboBoxEntries);
            this.defaultTabComboBox.setSelectedItem(
                    this.preferencesManager.loadDefaultVocabularyTab());
            this.defaultTabComboBox.setToolTipText(COMBO_BOX_TOOL_TIP);
            holderPanel.addGroupComponent(this.defaultTabComboBox);
//            vocabularyLayout
            Vector<String> vocabularyLayouts = new Vector<>(this.preferencesManager.getLayoutStrings());
            this.vocabularyLayoutComboBox = new JComboBox<>(vocabularyLayouts);
            this.vocabularyLayoutComboBox.setToolTipText(VOCABULARY_TAB_LAYOUT_TIP);
            this.vocabularyLayoutComboBox.setSelectedItem(
                    this.preferencesManager.loadSignatureComponentLayout());
            holderPanel.addGroup(VOCABULARY_TAB_LAYOUT_LABEL);
            holderPanel.addGroupComponent(this.vocabularyLayoutComboBox);
//            filterWarning
            holderPanel.addGroup(FILTER_WARNING_LABEL);
            this.showFilterWarningCheckBox = new JCheckBox();
            this.showFilterWarningCheckBox.setSelected(
                    this.preferencesManager.loadShowFilterWarningMessage());
            this.showFilterWarningCheckBox.setToolTipText(FILTER_WARNING_TOOL_TIP);
            holderPanel.addGroupComponent(this.showFilterWarningCheckBox);
//            simpleMode
            holderPanel.addGroup(USE_SIMPLE_MODE_LABEL);
            this.useSimpleModeCheckBox = new JCheckBox();
            this.useSimpleModeCheckBox.setSelected(
                    this.preferencesManager.loadUseSimpleMode());
            this.useSimpleModeCheckBox.setToolTipText(USE_SIMPLE_MODE_TOOL_TIP);
            holderPanel.addGroupComponent(this.useSimpleModeCheckBox);
        });
    }

    @Override
    public void dispose() throws Exception {

    }
}
