package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EveeProofPreferencesUI extends OWLPreferencesPanel {

//    todo: hier gehts weiter
    private final AbstractEveeProofPreferencesManager proofPreferencesManager;
    private final HashMap<String, JCheckBox> idToCheckBox;
//    private final HashMap<String, Boolean> idToBoolean;
    private final Logger logger = LoggerFactory.getLogger(EveeProofPreferencesUI.class);
//    private JCheckBox doNotShowAgainCheckBox;
//    private boolean doNotShowAgainBoolean;

    public EveeProofPreferencesUI(AbstractEveeProofPreferencesManager proofPreferencesManager){
        this.proofPreferencesManager = proofPreferencesManager;
        this.idToCheckBox = new HashMap<>();
//        this.idToBoolean = new HashMap<>();
    }

    @Override
    public void applyChanges() {
//        todo: only apply changes when necessary?
        SwingUtilities.invokeLater(() -> {
            for (String key : this.idToCheckBox.keySet()){
                boolean newValue = this.idToCheckBox.get(key).isSelected();
                this.proofPreferencesManager.setProtegePreferenceBoolean(key, newValue);
            }
        });
    }

    @Override
    public void initialise() {
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.proofPreferencesManager.getDefaultPreferenceKeysBoolean()){
                try{
                    boolean currentValue = this.proofPreferencesManager.getProtegePreferenceBoolean(identifier);
                    EveeProofPreferenceBoolean defaultPreference = this.proofPreferencesManager.getDefaultPreferenceBoolean(identifier);
//                    this.idToBoolean.put(identifier, currentValue);
                    JCheckBox checkBox = new JCheckBox(defaultPreference.getUiLabel(), currentValue);
                    checkBox.setToolTipText(defaultPreference.getUiToolTip());
                    this.logger.debug("checkbox created for key " + identifier);
                    this.idToCheckBox.put(identifier, checkBox);
                }
                catch (EveeProofPreferecenRetrievalException e){
                    this.logger.error(e.toString());
                }
            }
            Set<String> booleanKeys = this.proofPreferencesManager.getDefaultPreferenceKeysBoolean();
            Set<String> activationKeys = new HashSet<>();
            Set<String> miscellaneousBooleanKeys = new HashSet<>();
            for (String key : booleanKeys){
                if (key.endsWith(AbstractEveeProofPreferencesManager.ACTIVE)){
                    activationKeys.add(key);
                    this.logger.debug("activation key found: " + key);
                }
                else{
                    miscellaneousBooleanKeys.add(key);
                    this.logger.debug("misc key found: " + key);
                }
            }
            Set<String> integerKeys = this.proofPreferencesManager.getDefaultPreferenceKeysBoolean();
//            for extra labels "Active Proof Services" and "Miscellaneous Options"
            int rows = 2;
            rows += booleanKeys.size();
            rows += integerKeys.size();
            this.logger.debug("number of rows for this preference panel: " + rows);
            for (String key : booleanKeys){
                this.logger.debug(key);
            }
            for (String key : integerKeys){
                this.logger.debug(key);
            }
            this.setLayout(new BorderLayout());
            JPanel holderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            PreferencesLayoutPanel activationPanel = new PreferencesLayoutPanel();
            holderPanel.add(activationPanel);
            activationPanel.addGroup("Active Proof Services:");
            for (String key : activationKeys){
                activationPanel.addGroupComponent(this.idToCheckBox.get(key));
                this.logger.debug("checkbox added to panel for key " + key);
            }
            PreferencesLayoutPanel miscellaneousPanel = new PreferencesLayoutPanel();
            holderPanel.add(miscellaneousPanel);
            miscellaneousPanel.addGroup("Miscellaneous Options:");
            for (String key : miscellaneousBooleanKeys){
                miscellaneousPanel.addGroupComponent(this.idToCheckBox.get(key));
                this.logger.debug("checkbox added to panel for key " + key);
            }
            PreferencesLayoutPanel prefPanel = new PreferencesLayoutPanel();
            prefPanel.add(holderPanel);
            this.add(prefPanel, BorderLayout.NORTH);
        });
//        PreferencesManager preferencesManager = PreferencesManager.getInstance();
//        Preferences preferences = preferencesManager.getPreferencesForSet(this.SET_ID, this.PREFERENCE_ID);
//        this.doNotShowAgainBoolean = preferences.getBoolean(this.PREFERENCE_KEY, false);
//        SwingUtilities.invokeLater(() -> {
//            this.setLayout(new BorderLayout());
//            PreferencesLayoutPanel prefPanel = new PreferencesLayoutPanel();
//            this.doNotShowAgainCheckBox = new JCheckBox("Don't show warning for suboptimal proof", this.doNotShowAgainBoolean);
//            this.doNotShowAgainCheckBox.setToolTipText("Proof generation might return a suboptimal proof after cancellation. " +
//                    "If unchecked, a warning will be displayed to remind the user of this.");
//            prefPanel.add(this.doNotShowAgainCheckBox);
//            this.add(prefPanel, BorderLayout.NORTH);
//        });

    }

    @Override
    public void dispose() {

    }

    public void reset(){
// todo: how does reset-button in protege get to this function!?
    }

}
