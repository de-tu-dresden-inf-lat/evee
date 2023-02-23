package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbstractEveeProofPreferencesUI extends OWLPreferencesPanel {

    protected final AbstractEveeProofPreferencesManager proofPreferencesManager;
    private final SortedMap<String, JCheckBox> activeServicesCheckBoxes;
    private final String ACTIVE_SERVICES = "Active Proof Services:";
    protected JPanel holderPanel;
    protected final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesUI.class);

    public AbstractEveeProofPreferencesUI(AbstractEveeProofPreferencesManager proofPreferencesManager){
        this.proofPreferencesManager = proofPreferencesManager;
        this.activeServicesCheckBoxes = new TreeMap<>();
    }

    @Override
    public void applyChanges() {
        this.saveActiveProofServices();
    }

    @Override
    public void initialise() {
        this.createUiElements();
        this.createAndFillHolderPanel();
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel prefPanel = new PreferencesLayoutPanel();
            prefPanel.add(this.holderPanel);
            this.add(prefPanel, BorderLayout.NORTH);
        });
    }

    @Override
    public void dispose() {}

    protected void createUiElements(){
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.proofPreferencesManager.getProofServiceNameSet()){
                JCheckBox checkBox = new JCheckBox(
                        this.proofPreferencesManager.getIsActiveUILabel(identifier),
                        this.proofPreferencesManager.loadIsActive(identifier));
                checkBox.setToolTipText(this.proofPreferencesManager.getIsActiveToolTip(identifier));
                this.activeServicesCheckBoxes.put(identifier, checkBox);
            }
        });
    }

    protected void createAndFillHolderPanel(){
        SwingUtilities.invokeLater(() -> {
//            todo: alternatively use GridBagLayout
            this.holderPanel = new JPanel();
            this.holderPanel.setLayout(new BoxLayout(this.holderPanel, BoxLayout.Y_AXIS));
            PreferencesLayoutPanel activeProofServicePanel = new PreferencesLayoutPanel();
            activeProofServicePanel.addGroup(ACTIVE_SERVICES);
            this.holderPanel.add(activeProofServicePanel);
            for (String key : this.activeServicesCheckBoxes.keySet()){
                activeProofServicePanel.addGroupComponent(this.activeServicesCheckBoxes.get(key));
            }
        });
    }

    private void saveActiveProofServices(){
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.activeServicesCheckBoxes.keySet()){
                JCheckBox checkBox = this.activeServicesCheckBoxes.get(identifier);
                this.proofPreferencesManager.saveIsActive(
                        identifier, checkBox.isSelected());
            }
        });
    }

}
