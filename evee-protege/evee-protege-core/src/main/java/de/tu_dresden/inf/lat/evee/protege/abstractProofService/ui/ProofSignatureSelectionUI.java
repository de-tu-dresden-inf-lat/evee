package de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.AbstractSignatureSelectionUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;


public class ProofSignatureSelectionUI extends AbstractSignatureSelectionUI {

    private final OWLEntity top;
    private final OWLEntity bot;

    public ProofSignatureSelectionUI(OWLEditorKit owlEditorKit){
        this.top = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLThing();
        this.bot = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
    }

    protected void setSelectedSignature(Collection<OWLEntity> entities){
        this.selectedSignatureListModel.removeAll();
        this.selectedSignatureListModel.addElements(entities);
        this.selectedSignatureJList.clearSelection();
    }

    protected void clearSelectedSignatureUISelection(){
        this.selectedSignatureJList.clearSelection();
    }

    @Override
    protected void setButtonNamesAndToolTipStrings(){
        this.ADD_BTN_NAME = "Add";
        this.ADD_BTN_TOOLTIP =  "Add selected entries to known signature";
        this.DEL_BTN_NAME = "Delete";
        this.DEL_BTN_TOOLTIP = "Delete selected entries from known signature";
        this.CLR_BTN_NAME = "Clear";
        this.CLR_BTN_TOOLTIP = "Remove all entries from known signature";
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
    }

    @Override
    protected void clearAction(){
        SwingUtilities.invokeLater(() -> {
            this.selectedSignatureListModel.removeAll();
            ArrayList<OWLEntity> helperList = new ArrayList<>();
            helperList.add(this.top);
            helperList.add(this.bot);
            this.selectedSignatureListModel.addElements(helperList);
            this.selectedSignatureJList.clearSelection();
        });
    }

    public void resetSelectedSignatureList(){
        this.clearAction();
    }

    public void enableButtons(boolean enable){
        this.addButton.setEnabled(enable);
        this.deleteButton.setEnabled(enable);
        this.clearButton.setEnabled(enable);
    }

}
