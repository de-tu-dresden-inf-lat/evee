package de.tu_dresden.inf.lat.protege.abstractProofService;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;

public class EveeProofPreferences extends OWLPreferencesPanel {

    private final String SET_ID;
    private final String PREFERENCE_ID;
    private final String PREFERENCE_KEY;
    private JCheckBox dsaCheckBox;
    private boolean dsaBoolean;

    public EveeProofPreferences(String setId, String preferenceId, String preferenceKey){
        this.SET_ID = setId;
        this.PREFERENCE_ID = preferenceId;
        this.PREFERENCE_KEY = preferenceKey;
    }

    @Override
    public void applyChanges() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();
        Preferences preferences = preferencesManager.getPreferencesForSet(this.SET_ID, this.PREFERENCE_ID);
        this.dsaBoolean = this.dsaCheckBox.isSelected();
        preferences.putBoolean(this.PREFERENCE_KEY, this.dsaBoolean);
    }

    @Override
    public void initialise() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();
        Preferences preferences = preferencesManager.getPreferencesForSet(this.SET_ID, this.PREFERENCE_ID);
        this.dsaBoolean = preferences.getBoolean(this.PREFERENCE_KEY, false);
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel prefPanel = new PreferencesLayoutPanel();
            this.dsaCheckBox = new JCheckBox("Don't show warning for suboptimal proof", this.dsaBoolean);
            this.dsaCheckBox.setToolTipText("Proof generation might return a suboptimal proof after cancellation. " +
                    "If unchecked, a warning will be displayed to remind the user of this.");
            prefPanel.add(this.dsaCheckBox);
            this.add(prefPanel, BorderLayout.NORTH);
        });

    }

    @Override
    public void dispose() {

    }

    public void reset(){
        this.dsaBoolean = false;
// todo: how does reset-button in protege get to this function!?
    }

}
