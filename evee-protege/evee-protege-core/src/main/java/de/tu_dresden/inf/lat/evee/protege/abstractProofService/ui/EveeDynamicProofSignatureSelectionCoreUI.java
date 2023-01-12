package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.AbstractSignatureSelectionUI;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.protege.editor.owl.ui.tree.OWLObjectTreeRootNode;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;


public class EveeDynamicProofSignatureSelectionCoreUI extends AbstractSignatureSelectionUI {

    private final EveeDynamicProofSignatureSelectionWindow signatureSelectionUI;

    private final Logger logger = LoggerFactory.getLogger(EveeDynamicProofSignatureSelectionCoreUI.class);

    public EveeDynamicProofSignatureSelectionCoreUI(EveeDynamicProofSignatureSelectionWindow signatureSelectionUI){
        this.signatureSelectionUI = signatureSelectionUI;
    }

    @Override
    protected void setButtonNamesAndToolTipStrings(){
        this.ADD_BTN_NAME = ">";
        this.ADD_BTN_TOOLTIP =  "Add selected entries to known signature";
        this.DEL_BTN_NAME = "<";
        this.DEL_BTN_TOOLTIP = "Delete selected entries from known signature";
        this.CLR_BTN_NAME = "Reset";
        this.CLR_BTN_TOOLTIP = "Remove all entries from known signature except owl:Thing and owl:Nothing";
    }

    @Override
    public JPanel getOntologySignatureTabbedPanel(){
        JPanel resultPanel = super.getOntologySignatureTabbedPanel();
        resultPanel.setPreferredSize(new Dimension(400, 600));
        return resultPanel;
    }

    @Override
    public JPanel getSelectedSignatureListPanel(){
        JPanel resultPanel = super.getSelectedSignatureListPanel();
        resultPanel.setPreferredSize(new Dimension(400, 600));
        return resultPanel;
    }

    @Override
    protected void createSelectedSignatureListPane(OWLEditorKit owlEditorKit){
        super.createSelectedSignatureListPane(owlEditorKit);
        this.selectedSignatureListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Known signature:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.selectedSignatureJList.setCellRenderer(new OWLCellRendererSimple(owlEditorKit));
    }

    @Override
    protected void createOntologySignatureTabbedPanel(OWLEditorKit owlEditorKit){
        super.createOntologySignatureTabbedPanel(owlEditorKit);
        OWLEntity bot = owlEditorKit.getModelManager().getOWLDataFactory().getOWLNothing();
        OWLObjectTreeNode<OWLClass> newNode = new OWLObjectTreeNode<>(
                bot, this.classesTree);
        DefaultMutableTreeNode parentNode = ((DefaultMutableTreeNode) ((OWLObjectTreeRootNode<OWLClass>) this.classesTree.getModel().getRoot()).getFirstChild());
        ((DefaultTreeModel) this.classesTree.getModel()).insertNodeInto(
                newNode, parentNode, 0);
    }

    @Override
    protected void clearAction(){
        SwingUtilities.invokeLater(() -> {
            this.selectedSignatureListModel.removeAll();
            ArrayList<OWLEntity> helperList = new ArrayList<>();
            helperList.add(this.signatureSelectionUI.getOWLDataFactory().getOWLThing());
            helperList.add(this.signatureSelectionUI.getOWLDataFactory().getOWLNothing());
            this.selectedSignatureListModel.addElements(helperList);
            this.selectedSignatureJList.clearSelection();
        });
    }

    public void resetSelectedSignatureList(){
        this.clearAction();
    }

    public void enableSignature(boolean enable){
        this.addButton.setEnabled(enable);
        this.deleteButton.setEnabled(enable);
        this.clearButton.setEnabled(enable);
        this.selectedSignatureJList.setEnabled(enable);
//        this.selectedSignatureJList.setEnabled(enable);
    }

//    private class SignatureSelectionOWLCellRendererSimple extends OWLCellRendererSimple{
//
//        public SignatureSelectionOWLCellRendererSimple(OWLEditorKit owlEditorKit) {
//            super(owlEditorKit);
//        }
//
//        @Override
//        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            JLabel result = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//            if (! signatureSelectionUI.isSignatureEnabled()){
//                if (! (signatureSelectionUI.getOWLDataFactory().getOWLThing().equals(value) ||
//                        signatureSelectionUI.getOWLDataFactory().getOWLNothing().equals(value))
//                ){
//                    result.setForeground(Color.LIGHT_GRAY);
//                    Icon resultIcon = result.getIcon();
////                    only works with protege 5.6.0-beta-1-SNAPSHOT
////                    Icon resultIcon = result.getIcon();
////                    if (resultIcon instanceof OWLEntityIcon){
////                        ((OWLEntityIcon) resultIcon).setEnabled(false);
////                    }
//                }
//            }
//
//            return result;
//        }
//
//
//    }

}
