package de.tu_dresden.inf.lat.evee.protege.nonEntailment.core;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.AbstractSignatureSelectionUI;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLEntityCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NonEntailmentSignatureSelectionUI extends AbstractSignatureSelectionUI implements ActionListener {

    private final NonEntailmentViewComponent nonEntailmentViewComponent;
    private GivenSignatureOWLModelChangeListener givenSignatureModelManagerListener;
    private GivenSignatureOntologyChangeListener givenSignatureOntologyChangeListener;
    private static final String ADD_OBSERVATION_SIGNATURE_COMMAND = "ADD_OBSERVATION_SIGNATURE";
    private static final String ADD_OBSERVATION_SIGNATURE_NAME = "Add observation signature";
    private static final String ADD_OBSERVATION_SIGNATURE_TOOLTIP = "Adds signature of all observations";

    private final Logger logger = LoggerFactory.getLogger(NonEntailmentSignatureSelectionUI.class);

    public NonEntailmentSignatureSelectionUI(NonEntailmentViewComponent nonEntailmentViewComponent, OWLEditorKit editorKit, OWLModelManager modelManager){
        super();
        this.nonEntailmentViewComponent = nonEntailmentViewComponent;

    }

    @Override
    protected void setButtonNamesAndToolTipStrings(){
        this.ADD_BTN_NAME = "Add";
        this.ADD_BTN_TOOLTIP = "Add selected OWLObjects to \"Selected Signature\"";
        this.DEL_BTN_NAME = "Delete";
        this.DEL_BTN_TOOLTIP = "Delete selected OWLObjects from \"Selected Signature\"";
        this.CLR_BTN_NAME = "Reset";
        this.CLR_BTN_TOOLTIP = "Remove all OWLObjects from \"Selected Signature\"";
    }


    @Override
    protected void createOntologySignatureTabbedPanel(OWLEditorKit owlEditorKit){
        super.createOntologySignatureTabbedPanel(owlEditorKit);
        this.givenSignatureModelManagerListener = new GivenSignatureOWLModelChangeListener(this);
        owlEditorKit.getOWLModelManager().addListener(this.givenSignatureModelManagerListener);
        this.givenSignatureOntologyChangeListener = new GivenSignatureOntologyChangeListener(this);
        owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.givenSignatureOntologyChangeListener);;
    }

    protected JPanel getSignatureSelectionButtons(){
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
        toolbar.add(this.getAddButton());
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        toolbar.add(this.getDeleteButton());
        toolbar.add(Box.createRigidArea(new Dimension(5, 0)));
        toolbar.add(this.getClearButton());
        JPanel buttonHolderPanel = new JPanel();
        buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.PAGE_AXIS));
        buttonHolderPanel.add(toolbar);
        JPanel secondRowPanel = new JPanel();
        secondRowPanel.setLayout(new BoxLayout(secondRowPanel, BoxLayout.LINE_AXIS));
        JButton addObservationSignatureButton = new JButton(ADD_OBSERVATION_SIGNATURE_NAME);
        addObservationSignatureButton.setActionCommand(ADD_OBSERVATION_SIGNATURE_COMMAND);
        addObservationSignatureButton.setToolTipText(ADD_OBSERVATION_SIGNATURE_TOOLTIP);
        addObservationSignatureButton.addActionListener(this);
        secondRowPanel.add(Box.createGlue());
        secondRowPanel.add(addObservationSignatureButton);
        secondRowPanel.add(Box.createGlue());
        buttonHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonHolderPanel.add(secondRowPanel);
        buttonHolderPanel.setAlignmentX(Box.CENTER_ALIGNMENT);
        return buttonHolderPanel;
    }

    @Override
    protected void createSelectedSignatureListPane(OWLEditorKit owlEditorKit){
        super.createSelectedSignatureListPane(owlEditorKit);
        this.selectedSignatureListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Selected signature:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    public void dispose(OWLModelManager modelManager){
        super.dispose();
        modelManager.removeListener(this.givenSignatureModelManagerListener);
        modelManager.removeOntologyChangeListener(this.givenSignatureOntologyChangeListener);
    }

//    todo: improve - this class should not reach all the way back to the NonEntailmentViewComponent just to get access to the active ontology!
    private OWLOntology getActiveOntology(){
        return this.nonEntailmentViewComponent.getOWLEditorKit().getOWLModelManager().getActiveOntology();
    }

//    @Override
//    public void actionPerformed(ActionEvent e){
//        if (e.getActionCommand().equals(this.ADD_OBSERVATION_SIGNATURE_COMMAND)){
//            SwingUtilities.invokeLater(() -> {
//                ArrayList<OWLObject> observations = this.nonEntailmentViewComponent.getObservations();
//                HashSet<OWLEntity> observationEntities = new HashSet<>();
//                observations.forEach(observation -> observationEntities.addAll(observation.getSignature()));
//                this.selectedSignatureListModel.checkAndAddElements(observationEntities);
//            });
//        }
//        else{
//            SwingUtilities.invokeLater(() -> {
//                super.actionPerformed(e);
//                this.nonEntailmentViewComponent.changeComputeButtonStatus();
//            });
//        }
//    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
//            todo: find better way for addAction, deleteAction and clearAction (currently copied from super-class) - reason: need to enforce "this.selectedSignatureJList.clearSelection();" at end of "SwingUtilities.invokeLater()"
            case ADD_BTN_COMMAND:
                this.addAction();
                break;
            case DEL_BTN_COMMAND:
                this.deleteAction();
                break;
            case CLR_BTN_COMMAND:
                this.clearAction();
                break;
            case ADD_OBSERVATION_SIGNATURE_COMMAND:
                this.addObservationSignatureAction();
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
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    private void deleteAction(){
        SwingUtilities.invokeLater(() -> {
            List<OWLEntity> entitiesToDelete = this.selectedSignatureJList.getSelectedValuesList();
            this.selectedSignatureListModel.removeElements(entitiesToDelete);
            this.selectedSignatureJList.clearSelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    protected void clearAction(){
        SwingUtilities.invokeLater(() -> {
            this.selectedSignatureListModel.removeAll();
            this.selectedSignatureJList.clearSelection();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    protected void addObservationSignatureAction(){
        SwingUtilities.invokeLater(() -> {
            ArrayList<OWLObject> observations = this.nonEntailmentViewComponent.getObservations();
            HashSet<OWLEntity> observationEntities = new HashSet<>();
            observations.forEach(observation -> observationEntities.addAll(observation.getSignature()));
            this.selectedSignatureListModel.checkAndAddElements(observationEntities);
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
            this.nonEntailmentViewComponent.changeComputeButtonStatus();
        });
    }

    protected boolean listModelIsEmpty(){
        return this.selectedSignatureListModel.getSize() <= 0;
    }

    private class GivenSignatureOWLModelChangeListener implements OWLModelManagerListener {

        private NonEntailmentSignatureSelectionUI creator;

        private GivenSignatureOWLModelChangeListener(NonEntailmentSignatureSelectionUI creator){
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

        private NonEntailmentSignatureSelectionUI creator;

        private GivenSignatureOntologyChangeListener(NonEntailmentSignatureSelectionUI creator){
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