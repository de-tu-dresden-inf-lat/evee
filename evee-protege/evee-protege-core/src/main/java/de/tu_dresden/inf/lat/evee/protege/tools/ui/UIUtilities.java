package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class UIUtilities {

    private static final Logger logger = LoggerFactory.getLogger(UIUtilities.class);

    public static JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    public static JButton createNamedButton(String actionCommand, String name, String toolTip, ActionListener listener){
        JButton button = new JButton(name);
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTip);
        button.addActionListener(listener);
        return button;
    }

    /**
     * requires the file to be in the directory "resources" of evee-protege-core
     */
    public static JButton createIconButton(String actionCommand, URL url,
                                           String tooltip, ActionListener listener){
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(tooltip);
        button.addActionListener(listener);
        Icon icon = new ImageIcon(url);
        button.setIcon(icon);
        return button;
    }

    public static JButton createArrowButton(String actionCommand, int direction,
                                            String toolTip, ActionListener listener){
        assert (direction == BasicArrowButton.EAST || direction == BasicArrowButton.WEST
                || direction == BasicArrowButton.NORTH || direction == BasicArrowButton.SOUTH);
        JButton button = new BasicArrowButton(direction, UIManager.getColor("control"),
                Color.BLACK, Color.BLACK, UIManager.getColor("controlLtHighlight"));
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTip);
        button.addActionListener(listener);
        return button;
    }

    public static JButton createDoubleArrowButton(String actionCommand, int direction,
                                                  String toolTip, ActionListener listener){
        assert (direction == BasicArrowButton.EAST || direction == BasicArrowButton.WEST
                || direction == BasicArrowButton.NORTH || direction == BasicArrowButton.SOUTH);
        JButton button = new DoubleArrowButton(direction);
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTip);
        button.addActionListener(listener);
        return button;
    }

    public static void showError(String message, OWLEditorKit owlEditorKit){
        logger.debug("Displaying error message: {}", message);
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), "Error");
            errorDialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            errorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace())));
            errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            errorDialog.setVisible(true);
        });
    }

    private static class DoubleArrowButton extends BasicArrowButton {

        public DoubleArrowButton(int direction) {
            super(direction, UIManager.getColor("control"), Color.BLACK,
                    Color.BLACK, UIManager.getColor("controlLtHighlight"));
        }

        @Override
        public void paintTriangle(Graphics g, int x, int y, int size,
                                  int direction, boolean isEnabled) {
            if (direction == BasicArrowButton.NORTH || direction == BasicArrowButton.SOUTH){
                super.paintTriangle(g, x, y - (size / 2), size, direction, isEnabled);
                super.paintTriangle(g, x, y + (size / 2), size, direction, isEnabled);
            }
            else {
                super.paintTriangle(g, x - (size / 2), y, size, direction, isEnabled);
                super.paintTriangle(g, x + (size / 2), y, size, direction, isEnabled);
            }
        }
    }

}
