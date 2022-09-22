package de.tu_dresden.inf.lat.evee.protege.tools.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.renderer.ProtegeTreeNodeRenderer;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

abstract public class AbstractSignatureSelectionUI implements ActionListener {

    protected JPanel ontologySignatureTabbedPanel;
    protected JPanel selectedSignatureListPanel;
    private JTabbedPane signatureTabPane;
    protected OWLObjectTree<OWLClass> classesTree;
    protected OWLObjectTree<OWLObjectProperty> propertyTree;
    protected OWLObjectListModel<OWLNamedIndividual> ontologyIndividualsListModel;
    protected JList<OWLNamedIndividual> ontologyIndividualsJList;
    protected OWLObjectListModel<OWLEntity> selectedSignatureListModel;
    protected JList<OWLEntity> selectedSignatureJList;
    protected JButton addButton;
    private final String ADD_BTN_COMMAND = "ADD_TO_SIGNATURE";
    protected String ADD_BTN_NAME;
    protected String ADD_BTN_TOOLTIP;
    protected JButton deleteButton;
    private final String DEL_BTN_COMMAND = "DELETE_FROM_SIGNATURE";
    protected String DEL_BTN_NAME;
    protected String DEL_BTN_TOOLTIP;
    protected JButton clearButton;
    private final String CLR_BTN_COMMAND = "CLEAR_SIGNATURE";
    protected String CLR_BTN_NAME;
    protected String CLR_BTN_TOOLTIP;


    protected AbstractSignatureSelectionUI() {
        this.setButtonNamesAndToolTipStrings();
    }

    abstract protected void setButtonNamesAndToolTipStrings();

    public void createSignatureSelectionComponents(OWLEditorKit owlEditorKit){
        this.createOntologySignatureTabbedPanel(owlEditorKit);
        this.createButtons();
        this.createSelectedSignatureListPane(owlEditorKit);
    }

//    todo: this does not seem to be working correctly -> badly behaved listener found!
    public void dispose(){
        if (this.classesTree != null){
            this.classesTree.dispose();
        }
        if (this.propertyTree != null){
            this.propertyTree.dispose();
        }
    }

    protected void createOntologySignatureTabbedPanel(OWLEditorKit owlEditorKit){
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(400, 400));
//        todo: highlighting keywords for classes + properties necessary? see method "initialiseView" in Protege's "AbstractOWLEntityHierarchyViewComponent"
//        classes
        this.classesTree = new OWLModelManagerTree<>(
                owlEditorKit,
                owlEditorKit.getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        JScrollPane classesPane = new JScrollPane(this.classesTree);
        classesPane.getViewport().setBackground(Color.WHITE);
        this.classesTree.setCellRenderer(new ProtegeTreeNodeRenderer(owlEditorKit));
        this.classesTree.setOWLObjectComparator(owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Classes", classesPane);
//        object properties
        this.propertyTree = new OWLModelManagerTree<>(
                owlEditorKit,
                owlEditorKit.getOWLModelManager().getOWLHierarchyManager().getOWLObjectPropertyHierarchyProvider());
        JScrollPane propertyPane = new JScrollPane(this.propertyTree);
        propertyPane.getViewport().setBackground(Color.WHITE);
        this.propertyTree.setCellRenderer(new ProtegeTreeNodeRenderer(owlEditorKit));
        this.propertyTree.setOWLObjectComparator(owlEditorKit.getOWLModelManager().getOWLObjectComparator());
        tabbedPane.addTab("Object properties", propertyPane);
//        individuals
        this.ontologyIndividualsListModel = new OWLObjectListModel<>();
        this.ontologyIndividualsJList = new JList<>(this.ontologyIndividualsListModel);
        this.ontologyIndividualsJList.setCellRenderer(new OWLCellRendererSimple(owlEditorKit));
        Set<OWLNamedIndividual> individuals = owlEditorKit.getOWLModelManager().getActiveOntology().getIndividualsInSignature(Imports.INCLUDED);
        this.ontologyIndividualsListModel.addElements(individuals);
        tabbedPane.add("Individuals", this.ontologyIndividualsJList);
        this.signatureTabPane = tabbedPane;
        JPanel ontologySignaturePanel = new JPanel(new BorderLayout());
        ontologySignaturePanel.add(tabbedPane, BorderLayout.CENTER);
        ontologySignaturePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Ontology signature:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.ontologySignatureTabbedPanel = ontologySignaturePanel;
    }

    public JPanel getOntologySignatureTabbedPanel(){
        return this.ontologySignatureTabbedPanel;
    }

    private void createButtons(){
        this.addButton = this.createSingleButton(this.ADD_BTN_COMMAND, this.ADD_BTN_NAME, this.ADD_BTN_TOOLTIP);
        this.deleteButton = this.createSingleButton(this.DEL_BTN_COMMAND, this.DEL_BTN_NAME, this.DEL_BTN_TOOLTIP);
        this.clearButton = this.createSingleButton(this.CLR_BTN_COMMAND, this.CLR_BTN_NAME, this.CLR_BTN_TOOLTIP);
    }

    protected JButton createSingleButton(String actionCommand, String name, String toolTip){
        JButton newButton = new JButton(name);
        newButton.setActionCommand(actionCommand);
        newButton.setToolTipText(toolTip);
        newButton.addActionListener(this);
        return newButton;
    }

    protected JLabel createLabel(String labelText){
        JLabel label = new JLabel(labelText);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    public JButton getAddButton(){
        return this.addButton;
    }

    public JButton getDeleteButton(){
        return this.deleteButton;
    }

    public JButton getClearButton(){
        return this.clearButton;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ADD_BTN_COMMAND:
                this.addAction();
                break;
            case DEL_BTN_COMMAND:
                this.deleteAction();
                break;
            case CLR_BTN_COMMAND:
                this.clearAction();
                break;
        }
    }

    private void addAction(){
        SwingUtilities.invokeLater(() -> {
            int tabIndex = this.signatureTabPane.getSelectedIndex();
            if (tabIndex == 0){
                java.util.List<OWLClass> entitiesToAdd = this.classesTree.getSelectedOWLObjects();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.classesTree.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
            else if (tabIndex == 1){
                java.util.List<OWLObjectProperty> entitiesToAdd = this.propertyTree.getSelectedOWLObjects();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.propertyTree.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
            else{
                java.util.List<OWLNamedIndividual> entitiesToAdd = this.ontologyIndividualsJList.getSelectedValuesList();
                this.selectedSignatureListModel.checkAndAddElements(entitiesToAdd);
                this.ontologyIndividualsJList.clearSelection();
                this.selectedSignatureJList.clearSelection();
            }
        });
    }

    private void deleteAction(){
        SwingUtilities.invokeLater(() -> {
            List<OWLEntity> entitiesToDelete = this.selectedSignatureJList.getSelectedValuesList();
            this.selectedSignatureListModel.removeElements(entitiesToDelete);
            this.selectedSignatureJList.clearSelection();
        });
    }

    protected void clearAction(){
        SwingUtilities.invokeLater(() -> {
            this.selectedSignatureListModel.removeAll();
            this.selectedSignatureJList.clearSelection();
        });
    }

    protected void createSelectedSignatureListPane(OWLEditorKit owlEditorKit){
        this.selectedSignatureListPanel = new JPanel(new BorderLayout());
        this.selectedSignatureListModel = new OWLObjectListModel<>();
        this.selectedSignatureJList = new JList<>(this.selectedSignatureListModel);
        this.selectedSignatureJList.setCellRenderer(new OWLCellRendererSimple(owlEditorKit));
        JScrollPane scrollPane = new JScrollPane(this.selectedSignatureJList);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        this.selectedSignatureListPanel.add(scrollPane);
    }

    public JPanel getSelectedSignatureListPanel(){
        return this.selectedSignatureListPanel;
    }

    public List<OWLEntity> getSelectedSignature(){
        return this.selectedSignatureListModel.getOwlObjects();
    }

}
