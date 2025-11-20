package de.tu_dresden.inf.lat.evee.protege.nemoBasedProofService;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.AbstractEveeProofPreferencesUI;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;

public class EveeNemoBasedProofPreferencesUI extends AbstractEveeProofPreferencesUI implements ActionListener{

    public final static String PATH_LABEL_TEXT = "Path to Nemo executable";
    public final static String PATH_TEXT_FIELD_TOOL_TIP = "Currently used path to the Nemo executable";
    public final static String PATH_BROWSE_BUTTON_COMMAND = "PATH_BROWSE_COMMAND";
    public final static String PATH_BROWSE_BUTTON_TOOL_TIP = "Change path to the Nemo executable";

    private JTextField nemoPathTextField;
    
    public EveeNemoBasedProofPreferencesUI(){
        super();
        this.setNemoBasedProofPreferencesManager(new EveeNemoBasedProofPreferencesManager());
    }

    public void setNemoBasedProofPreferencesManager(EveeNemoBasedProofPreferencesManager proofPreferencesManager){
        super.setAbstractProofPreferencesManager(proofPreferencesManager);
    }

    @Override
    public void applyChanges() {
        saveActiveProofServices();
        ((EveeNemoBasedProofPreferencesManager)abstractProofPreferencesManager).saveNemoPath(nemoPathTextField.getText());
    }

    @Override
    public void initialise() {
        // proof service checkboxes
       createUiElements();
       createAndFillHolderPanel();

        //NEMO path
       SwingUtilities.invokeLater(() -> {
            holderPanel.addSeparator();
            holderPanel.addGroup("Nemo:");
            JPanel nemoPathPanel = new JPanel(new GridBagLayout());
            JLabel nemoPathLabel = UIUtilities.createLabel(PATH_LABEL_TEXT);
            Insets insets = new Insets(0, 0, 0, 5);
            GridBagConstraints pathConstraints = new GridBagConstraints();
            //path label constraints
            pathConstraints.gridx = 0;
            pathConstraints.gridy = 0;
            pathConstraints.insets = insets;
            pathConstraints.fill = GridBagConstraints.HORIZONTAL;
            pathConstraints.weighty = 0.1;
            nemoPathPanel.add(nemoPathLabel, pathConstraints);

            nemoPathTextField = new JTextField(25);
            nemoPathTextField.setText(((EveeNemoBasedProofPreferencesManager)abstractProofPreferencesManager).loadNemoPath());
            nemoPathTextField.setToolTipText(PATH_TEXT_FIELD_TOOL_TIP);

            //path text field constraints
            pathConstraints.gridx = 1;
            pathConstraints.weighty = 0.5;
            nemoPathPanel.add(nemoPathTextField, pathConstraints);

            JButton browsePathButton = new JButton("Browse");
            browsePathButton.setActionCommand(PATH_BROWSE_BUTTON_COMMAND);
            browsePathButton.setToolTipText(PATH_BROWSE_BUTTON_TOOL_TIP);
            browsePathButton.addActionListener(this);

            //path browse button constraints
            pathConstraints.gridx = 2;
            pathConstraints.weighty = 0.1;
            nemoPathPanel.add(browsePathButton, pathConstraints);
            
            holderPanel.addGroupComponent(nemoPathPanel);
       });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case PATH_BROWSE_BUTTON_COMMAND:
                changeNemoPath();
                break;
        }
    }

    private void changeNemoPath(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            nemoPathTextField.setText(file.getPath());
        }
    }
}
