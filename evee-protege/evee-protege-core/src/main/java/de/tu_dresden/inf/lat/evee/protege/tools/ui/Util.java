package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import javax.swing.*;

public class Util {

    public static JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

}
