package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class UIUtilities {

    private static final Color control = UIManager.getColor("control");
    private static final Color highlight = UIManager.getColor("controlLtHighlight");
    private static final Color black = Color.BLACK;

    private static final Logger logger = LoggerFactory.getLogger(UIUtilities.class);

    public static JLabel createProgressUILabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    public static JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    public static JButton createNamedButton(String actionCommand, @Nonnull String name, String toolTip, @Nonnull ActionListener listener){
        JButton button = new JButton(name);
        if (actionCommand != null){
            button.setActionCommand(actionCommand);
        }
        if (toolTip != null){
            button.setToolTipText(toolTip);
        }
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
        ImageIcon icon = new ImageIcon(url);
        OWLRendererPreferences preferences = OWLRendererPreferences.getInstance();
//        negative number used to keep aspect ratio
        Image image = icon.getImage().getScaledInstance(-1,
                (int) (preferences.getFontSize() * 1.5), Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(image));
        return button;
    }

    public static JButton createArrowButton(String actionCommand, int direction,
                                            String toolTip, ActionListener listener){
        assert (direction == BasicArrowButton.EAST || direction == BasicArrowButton.WEST
                || direction == BasicArrowButton.NORTH || direction == BasicArrowButton.SOUTH);
        JButton button = new BasicArrowButton(direction, control,
                black, black, highlight);
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

    public static void showWarning(String message, OWLEditorKit owlEditorKit){
        logger.debug("Displaying warning message: {}", message);
        showMessage(message, JOptionPane.WARNING_MESSAGE, owlEditorKit);
    }

    public static void showError(String message, OWLEditorKit owlEditorKit){
        logger.debug("Displaying error message: {}", message);
        showMessage(message, JOptionPane.ERROR_MESSAGE, owlEditorKit);

    }

    private static void showMessage(String message, int messageType, OWLEditorKit owlEditorKit){
        SwingUtilities.invokeLater(() -> {
            JOptionPane errorPane = new JOptionPane(message, messageType);
            String title = "";
            switch (messageType){
                case JOptionPane.ERROR_MESSAGE:
                    title = "Error";
                    break;
                case JOptionPane.WARNING_MESSAGE:
                    title = "Warning";
                    break;
            }
            JDialog dialog = errorPane.createDialog(ProtegeManager.getInstance().getFrame(
                    owlEditorKit.getWorkspace()), title);
            dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            dialog.pack();
            dialog.setLocationRelativeTo(
                    ProtegeManager.getInstance().getFrame(owlEditorKit.getWorkspace()));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }

    private static class DoubleArrowButton extends BasicArrowButton {

        public DoubleArrowButton(int direction) {
            super(direction, control, black,
                    black, highlight);
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
