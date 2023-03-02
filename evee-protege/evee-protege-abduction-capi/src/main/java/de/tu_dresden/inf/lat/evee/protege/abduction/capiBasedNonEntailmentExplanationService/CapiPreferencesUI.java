package de.tu_dresden.inf.lat.evee.protege.abduction.capiBasedNonEntailmentExplanationService;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CapiPreferencesUI extends OWLPreferencesPanel implements ActionListener {

    private final CapiPreferencesManager preferencesManager;
    private JTextField spassPathTextField;
    private JSpinner timeLimitSpinner;
    private JCheckBox removeRedundanciesCheckBox;
    private JCheckBox simplifyConjunctionsCheckBox;
    private JCheckBox semanticallyOrderedCheckBox;
//    public final static String PATH_TEXT_FIELD_COMMAND = "PATH_TEXT_FIELD_COMMAND";
    public final static String PATH_LABEL_TEXT = "Path to SPASS executable:";
    public final static String PATH_TEXT_FIELD_TOOL_TIP = "Currently used path to the SPASS executable";
    public final static String PATH_BROWSE_BUTTON_COMMAND = "PATH_BROWSE_COMMAND";
    public final static String PATH_BROWSE_BUTTON_TOOL_TIP = "Change path to the SPASS executable";
    public final static String TIME_LIMIT_LABEL_TEXT = "SPASS time limit:";
    public final static String TIME_LIMIT_TOOL_TIP = "Time limit for SPASS in seconds";
    public final static String TIME_LIMIT_UNIT = "Seconds";
    public final static String REMOVE_REDUNDANCIES_TEXT = "Remove redundancies in solutions";
    public final static String SIMPLIFY_CONJUNCTIONS_TEXT = "Simplify conjunctions in solutions";
    public final static String SEMANTICALLY_ORDERED_TEXT = "Sort solutions by semantic minimality";

    private final Logger logger = LoggerFactory.getLogger(CapiPreferencesUI.class);

    public CapiPreferencesUI(){
        this.preferencesManager = new CapiPreferencesManager();
    }

    @Override
    public void initialise() {
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel holderPanel = new PreferencesLayoutPanel();
            this.add(holderPanel, BorderLayout.NORTH);
            holderPanel.addGroup("SPASS:");
            JPanel spassPathPanel = new JPanel(new GridBagLayout());
            JLabel spassPathLabel = UIUtilities.createLabel(PATH_LABEL_TEXT);
            Insets insets = new Insets(0, 0, 0, 5);
            GridBagConstraints pathConstraints = new GridBagConstraints();
//            path label constraints
            pathConstraints.gridx = 0;
            pathConstraints.gridy = 0;
            pathConstraints.insets = insets;
            pathConstraints.fill = GridBagConstraints.HORIZONTAL;
            pathConstraints.weighty = 0.1;
            spassPathPanel.add(spassPathLabel, pathConstraints);
            this.spassPathTextField = new JTextField(25);
            this.spassPathTextField.setText(this.preferencesManager.loadSpassPath());
            this.spassPathTextField.setToolTipText(PATH_TEXT_FIELD_TOOL_TIP);
//            path text field constraints
            pathConstraints.gridx = 1;
            pathConstraints.weighty = 0.5;
            spassPathPanel.add(this.spassPathTextField, pathConstraints);
//            this.spassPathTextField.setActionCommand(PATH_TEXT_FIELD_COMMAND);
//            this.spassPathTextField.addActionListener(this);
            JButton browsePathButton = new JButton("Browse");
            browsePathButton.setActionCommand(PATH_BROWSE_BUTTON_COMMAND);
            browsePathButton.setToolTipText(PATH_BROWSE_BUTTON_TOOL_TIP);
            browsePathButton.addActionListener(this);
//            path browse button constraints
            pathConstraints.gridx = 2;
            pathConstraints.weighty = 0.1;
            spassPathPanel.add(browsePathButton, pathConstraints);
            holderPanel.addGroupComponent(spassPathPanel);

            JPanel spassTimeLimitPanel = new JPanel(new GridBagLayout());
            JLabel spassTimeLimitFrontLabel = UIUtilities.createLabel(TIME_LIMIT_LABEL_TEXT);
            GridBagConstraints timeLimitConstraints = new GridBagConstraints();
//            time limit front label constraints
            timeLimitConstraints.gridx = 0;
            timeLimitConstraints.gridy = 0;
            timeLimitConstraints.insets = insets;
            spassTimeLimitPanel.add(spassTimeLimitFrontLabel, timeLimitConstraints);
            SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(this.preferencesManager.loadTimeLimit(),
                    0, null, 1);
            this.timeLimitSpinner = new JSpinner(spinnerNumberModel);
            this.timeLimitSpinner.setToolTipText(TIME_LIMIT_TOOL_TIP);
            this.timeLimitSpinner.setPreferredSize(new Dimension(100, (int) this.timeLimitSpinner.getPreferredSize().getHeight()));
//            time limit spinner constraints
            timeLimitConstraints.gridx = 1;
            spassTimeLimitPanel.add(this.timeLimitSpinner, timeLimitConstraints);
            JLabel spassTimeLimitBackLabel = UIUtilities.createLabel(TIME_LIMIT_UNIT);
//            time limit back label constraints
            timeLimitConstraints.gridx = 2;
            spassTimeLimitPanel.add(spassTimeLimitBackLabel, timeLimitConstraints);
            holderPanel.addGroupComponent(spassTimeLimitPanel);

            holderPanel.addSeparator();
            holderPanel.addGroup("Postprocessing:");
            this.removeRedundanciesCheckBox = new JCheckBox(REMOVE_REDUNDANCIES_TEXT,
                    this.preferencesManager.loadRemoveRedundancies());
            this.removeRedundanciesCheckBox.setToolTipText(REMOVE_REDUNDANCIES_TEXT);
            holderPanel.addGroupComponent(this.removeRedundanciesCheckBox);
            this.simplifyConjunctionsCheckBox = new JCheckBox(SIMPLIFY_CONJUNCTIONS_TEXT,
                    this.preferencesManager.loadSimplifyConjunctions());
            this.simplifyConjunctionsCheckBox.setToolTipText(SIMPLIFY_CONJUNCTIONS_TEXT);
            holderPanel.addGroupComponent(this.simplifyConjunctionsCheckBox);
            this.semanticallyOrderedCheckBox = new JCheckBox(SEMANTICALLY_ORDERED_TEXT,
                    this.preferencesManager.loadSemanticallyOrdered());
            this.semanticallyOrderedCheckBox.setToolTipText(SEMANTICALLY_ORDERED_TEXT);
            holderPanel.addGroupComponent(this.semanticallyOrderedCheckBox);

        });
    }

    @Override
    public void dispose() {

    }

    @Override
    public void applyChanges() {
        this.preferencesManager.saveSpassPath(this.spassPathTextField.getText());
        this.preferencesManager.saveTimeLimit((int) this.timeLimitSpinner.getValue());
        this.preferencesManager.saveRemoveRedundancies(this.removeRedundanciesCheckBox.isSelected());
        this.preferencesManager.saveSimplifyConjunctions(this.simplifyConjunctionsCheckBox.isSelected());
        this.preferencesManager.saveSemanticallyOrdered(this.semanticallyOrderedCheckBox.isSelected());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case PATH_BROWSE_BUTTON_COMMAND:
                this.changeSpassPath();
                break;
        }
    }

    private void changeSpassPath(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            this.spassPathTextField.setText(file.getPath());
        }
    }

}
