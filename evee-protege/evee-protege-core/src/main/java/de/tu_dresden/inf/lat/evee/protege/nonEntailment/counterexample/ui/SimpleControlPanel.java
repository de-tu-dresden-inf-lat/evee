package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui;

import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.*;

public class SimpleControlPanel extends ControlPanel{




    public SimpleControlPanel(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
        this.removeAll();
        this.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        this.add(getRefreshButton());
        this.add(Box.createRigidArea(new Dimension(0, BIG_SPACE)));
        this.add(getClassListPanel());
        this.setAlignmentY(CENTER_ALIGNMENT);
    }

    private JPanel getClassListPanel() {
        JPanel classListPanel = new JPanel();
        classListPanel.setLayout(new BoxLayout(classListPanel, BoxLayout.Y_AXIS));
        JPanel classListBorder = new JPanel();
        classListBorder.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                CLASS_LIST));
        JScrollPane scrollable = new JScrollPane(this.classList);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        classListBorder.add(scrollable);
        classListBorder.setAlignmentY(TOP_ALIGNMENT);
        classListPanel.add(classListBorder);
        classListPanel.add(Box.createRigidArea(new Dimension(0, SMALL_SPACE)));
        return classListPanel;
    }
}
