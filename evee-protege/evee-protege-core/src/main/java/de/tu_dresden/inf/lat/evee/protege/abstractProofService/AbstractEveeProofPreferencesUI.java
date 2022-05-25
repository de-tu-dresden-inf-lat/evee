package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public abstract class AbstractEveeProofPreferencesUI extends OWLPreferencesPanel {

    protected final AbstractEveeProofPreferencesManager proofPreferencesManager;
    private final TreeMap<String, JCheckBox> activeServicesCheckBoxes;
    protected final TreeMap<String, JCheckBox> miscellaneousCheckBoxes;
    private final String ACTIVE_SERVICES = "Active Proof Services:";
    private final String MISC_OPTIONS = "Miscellaneous Options:";
    protected int labeledGroupNumber;
    protected JPanel holderPanel;
    protected PreferencesLayoutPanel miscellaneousPreferencesPanel;
    protected final Logger logger = LoggerFactory.getLogger(AbstractEveeProofPreferencesUI.class);

    public AbstractEveeProofPreferencesUI(AbstractEveeProofPreferencesManager proofPreferencesManager){
        this.proofPreferencesManager = proofPreferencesManager;
        this.activeServicesCheckBoxes = new TreeMap<>();
        this.miscellaneousCheckBoxes = new TreeMap<>();
        this.labeledGroupNumber = 2;
        SwingUtilities.invokeLater(() -> {
            this.miscellaneousPreferencesPanel = new PreferencesLayoutPanel();
            this.miscellaneousPreferencesPanel.addGroup(MISC_OPTIONS);
        });
    }

    abstract protected void addAdditionalPreferences();

    abstract protected void saveAdditionalPreferences();

    @Override
    public void applyChanges() {
        this.saveActiveProofServices();
        this.saveAdditionalPreferences();
        this.saveMiscellaneousPreferences();
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
            JCheckBox checkBox = new JCheckBox(
                    this.proofPreferencesManager.getSuboptimalProofWarningUILabel(),
                    this.proofPreferencesManager.loadShowSuboptimalProofWarning());
            checkBox.setToolTipText(this.proofPreferencesManager.getSuboptimalProofWarningUIToolTip());
            this.miscellaneousCheckBoxes.put(this.proofPreferencesManager.SUBOPTIMAL_MSG, checkBox);
        });
    }

    protected void createAndFillHolderPanel(){
        SwingUtilities.invokeLater(() -> {
            this.holderPanel = new JPanel(new GridLayout(this.labeledGroupNumber, 1, 5, 5));
        });
        this.addActiveProofServices();
        this.addAdditionalPreferences();
        this.addMiscellaneousPreferences();
    }

    private void addActiveProofServices(){
        SwingUtilities.invokeLater(() -> {
            PreferencesLayoutPanel activeProofServicePanel = new PreferencesLayoutPanel();
            activeProofServicePanel.addGroup(ACTIVE_SERVICES);
            this.holderPanel.add(activeProofServicePanel);
            for (String key : this.activeServicesCheckBoxes.keySet()){
                activeProofServicePanel.addGroupComponent(this.activeServicesCheckBoxes.get(key));
            }
        });
    }

    protected void addMiscellaneousPreferences(){
        SwingUtilities.invokeLater(() -> {
            this.holderPanel.add(this.miscellaneousPreferencesPanel);
            for (String key : this.miscellaneousCheckBoxes.keySet()){
                this.miscellaneousPreferencesPanel.addGroupComponent(this.miscellaneousCheckBoxes.get(key));
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

    protected void saveMiscellaneousPreferences(){
        SwingUtilities.invokeLater(() -> {
            for (String identifier : this.miscellaneousCheckBoxes.keySet()){
                JCheckBox checkBox = this.miscellaneousCheckBoxes.get(identifier);
                this.proofPreferencesManager.saveBooleanPreferenceValue(
                        identifier, checkBox.isSelected());
            }
        });
    }

}
