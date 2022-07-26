package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
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

    private AbstractEveeProofPreferencesManager abstractProofPreferencesManager;
    private final SortedMap<String, JCheckBox> activeServicesCheckBoxes;
    private final String ACTIVE_SERVICES = "Active Proof Services:";
    private final String MISC_OPTIONS = "Miscellaneous Options:";
    private boolean hasMiscellaneousGroup = false;
    protected PreferencesLayoutPanel holderPanel;
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesUI.class);

    public AbstractEveeProofPreferencesUI(){
        this.activeServicesCheckBoxes = new TreeMap<>();
    }

    public void setAbstractProofPreferencesManager(AbstractEveeProofPreferencesManager proofPreferencesManager){
        this.abstractProofPreferencesManager = proofPreferencesManager;
    }

    @Override
    public void applyChanges() {
        this.saveActiveProofServices();
        this.abstractProofPreferencesManager.updateLastSaved();
    }

    @Override
    public void initialise() {
        this.createUiElements();
        this.createAndFillHolderPanel();
    }

    @Override
    public void dispose() {}

    protected void createUiElements(){
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.abstractProofPreferencesManager.getProofServiceNameSet()){
                JCheckBox checkBox = new JCheckBox(
                        this.abstractProofPreferencesManager.getIsActiveUILabel(identifier),
                        this.abstractProofPreferencesManager.loadIsActive(identifier));
                checkBox.setToolTipText(this.abstractProofPreferencesManager.getIsActiveToolTip(identifier));
                this.activeServicesCheckBoxes.put(identifier, checkBox);
            }
        });
    }

    protected void createAndFillHolderPanel(){
        SwingUtilities.invokeLater(() -> {
            this.holderPanel = new PreferencesLayoutPanel();
            this.setLayout(new BorderLayout());
            this.add(this.holderPanel, BorderLayout.NORTH);
            this.holderPanel.addGroup(ACTIVE_SERVICES);
            for (String key : this.activeServicesCheckBoxes.keySet()){
                this.holderPanel.addGroupComponent(this.activeServicesCheckBoxes.get(key));
            }
        });
    }

    private void saveActiveProofServices(){
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.activeServicesCheckBoxes.keySet()){
                this.abstractProofPreferencesManager.saveIsActive(
                        identifier, this.activeServicesCheckBoxes.get(identifier).isSelected());
            }
        });
    }

    protected void addMiscellaneousGroup(){
        if (! this.hasMiscellaneousGroup){
            SwingUtilities.invokeLater(() -> this.holderPanel.addGroup(this.MISC_OPTIONS));
            this.hasMiscellaneousGroup = true;
        }
    }

}
