package de.tu_dresden.inf.lat.evee.protege.abduction;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.AbstractSignatureSelectionUI;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLEntityCollector;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbductionSignatureSelectionUI extends AbstractSignatureSelectionUI implements ActionListener {


    private final AbductionViewComponent abductionViewComponent;
    private GivenSignatureOWLModelChangeListener givenSignatureModelManagerListener;
    private GivenSignatureOntologyChangeListener givenSignatureOntologyChangeListener;
    private JCheckBox observationSignatureExclusionCheckBox;
    private final String CHECKBOX_LABEL = "Only exclude signature of observations";
    private final String CHECKBOX_TOOLTIP = "If checked, only signature from observations will be excluded";

    public AbductionSignatureSelectionUI(AbductionViewComponent abductionViewComponent, OWLEditorKit editorKit, OWLModelManager modelManager){
        super();
        this.abductionViewComponent = abductionViewComponent;

    }

    @Override
    protected void setButtonNamesAndToolTipStrings(){
        this.ADD_BTN_NAME = "Add";
        this.ADD_BTN_TOOLTIP = "Add selected OWLObjects to \"Excluded Signature\"";
        this.DEL_BTN_NAME = "Delete";
        this.DEL_BTN_TOOLTIP = "Delete selected OWLObjects from \"Excluded Signature\"";
        this.CLR_BTN_NAME = "Clear";
        this.CLR_BTN_TOOLTIP = "Remove all OWLObjects from \"Excluded Signature\"";
    }


    @Override
    protected void createOntologySignatureTabbedPanel(OWLEditorKit owlEditorKit){
        super.createOntologySignatureTabbedPanel(owlEditorKit);
        this.givenSignatureModelManagerListener = new GivenSignatureOWLModelChangeListener(this);
        owlEditorKit.getOWLModelManager().addListener(this.givenSignatureModelManagerListener);
        this.givenSignatureOntologyChangeListener = new GivenSignatureOntologyChangeListener(this);
        owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.givenSignatureOntologyChangeListener);;
    }

    protected JPanel getSignatureSelectionButtonsAndCheckBox(){
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
        toolbar.add(this.getAddButton());
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        toolbar.add(this.getDeleteButton());
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        toolbar.add(this.getClearButton());
        JPanel buttonAndCheckBoxPanel = new JPanel();
        buttonAndCheckBoxPanel.setLayout(new BoxLayout(buttonAndCheckBoxPanel, BoxLayout.PAGE_AXIS));
        buttonAndCheckBoxPanel.add(toolbar);
        JLabel label = this.createLabel(this.CHECKBOX_LABEL);
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.LINE_AXIS));
        checkBoxPanel.add(label);
        checkBoxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        this.observationSignatureExclusionCheckBox = new JCheckBox();
        this.observationSignatureExclusionCheckBox.setToolTipText(this.CHECKBOX_TOOLTIP);
        checkBoxPanel.add(this.observationSignatureExclusionCheckBox);
        buttonAndCheckBoxPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonAndCheckBoxPanel.add(checkBoxPanel);
        buttonAndCheckBoxPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonAndCheckBoxPanel;
    }

    @Override
    protected void createSelectedSignatureListPane(OWLEditorKit owlEditorKit){
        super.createSelectedSignatureListPane(owlEditorKit);
        this.selectedSignatureListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Excluded signature:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    public void dispose(OWLModelManager modelManager){
        super.dispose();
        modelManager.removeListener(this.givenSignatureModelManagerListener);
        modelManager.removeOntologyChangeListener(this.givenSignatureOntologyChangeListener);
    }

//    todo: improve!! - this class should not reach all the way back to the AbductionViewComponent just to get access to the active ontology!
    private OWLOntology getActiveOntology(){
        return this.abductionViewComponent.getOWLEditorKit().getOWLModelManager().getActiveOntology();
    }



    private class GivenSignatureOWLModelChangeListener implements OWLModelManagerListener {

        private AbductionSignatureSelectionUI creator;

        private GivenSignatureOWLModelChangeListener(AbductionSignatureSelectionUI creator){
            this.creator = creator;
        }

        private OWLOntology getActiveOntology(){
            return this.creator.getActiveOntology();
        }

        @Override
        public void handleChange(OWLModelManagerChangeEvent changeEvent) {
            SwingUtilities.invokeLater(() -> {
                if (changeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || changeEvent.isType(EventType.ONTOLOGY_RELOADED)){
//                    ontology signature component:
                    ontologyIndividualsListModel.removeAll();
                    ontologyIndividualsListModel.addElements(
                            this.getActiveOntology().getIndividualsInSignature(
                                    Imports.INCLUDED));
                    ontologyIndividualsJList.clearSelection();
//                    selected signature component:
                    selectedSignatureListModel.removeAll();
                }
            });
        }
    }

    private class GivenSignatureOntologyChangeListener implements OWLOntologyChangeListener {

        private AbductionSignatureSelectionUI creator;

        private GivenSignatureOntologyChangeListener(AbductionSignatureSelectionUI creator){
            this.creator = creator;
        }

        private OWLOntology getActiveOntology(){
            return this.creator.getActiveOntology();
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> ontologyChanges) throws OWLException {
            SwingUtilities.invokeLater(() -> {
//                ontology signature component:
                ontologyIndividualsListModel.removeAll();
                ontologyIndividualsListModel.addElements(
                        this.getActiveOntology().getIndividualsInSignature(
                                Imports.INCLUDED));
                ontologyIndividualsJList.clearSelection();
//                selected signature component:
                Set<OWLEntity> deletedEntities = new HashSet<>();
                OWLEntityCollector entityCollector = new OWLEntityCollector(deletedEntities);
                ontologyChanges.stream().filter(OWLOntologyChange::isRemoveAxiom).forEach(
                        removedAxiom -> removedAxiom.getAxiom().accept(entityCollector)
                );
                Set<OWLEntity> entitiesToDelete = new HashSet<>();
                for (OWLEntity deletedEntity : deletedEntities){
                    if (! this.getActiveOntology().containsEntityInSignature(deletedEntity)){
                        entitiesToDelete.add(deletedEntity);
                    }
                }
                selectedSignatureListModel.removeElements(entitiesToDelete);
            });
        }
    }

}
