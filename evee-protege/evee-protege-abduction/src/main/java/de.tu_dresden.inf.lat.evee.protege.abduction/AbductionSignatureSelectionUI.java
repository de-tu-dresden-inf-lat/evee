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

public class AbductionSignatureSelectionUI extends AbstractSignatureSelectionUI implements ActionListener {


    private final AbductionViewComponent abductionViewComponent;
    private GivenSignatureOWLModelChangeListener givenSignatureModelManagerListener;
    private GivenSignatureOntologyChangeListener givenSignatureOntologyChangeListener;
    private final String ADD_OBSERVATION_SIGNATURE_COMMAND = "ADD_OBSERVATION_SIGNATURE";
    private final String ADD_OBSERVATION_SIGNATURE_NAME = "Add observation signature";
    private final String ADD_OBSERVATION_SIGNATURE_TOOLTIP = "Adds signature of all observations";

    private final Logger logger = LoggerFactory.getLogger(AbductionSignatureSelectionUI.class);

    public AbductionSignatureSelectionUI(AbductionViewComponent abductionViewComponent, OWLEditorKit editorKit, OWLModelManager modelManager){
        super();
        this.abductionViewComponent = abductionViewComponent;

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
        JButton addObservationSignatureButton = new JButton(this.ADD_OBSERVATION_SIGNATURE_NAME);
        addObservationSignatureButton.setActionCommand(this.ADD_OBSERVATION_SIGNATURE_COMMAND);
        addObservationSignatureButton.setToolTipText(this.ADD_OBSERVATION_SIGNATURE_TOOLTIP);
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

//    todo: improve - this class should not reach all the way back to the AbductionViewComponent just to get access to the active ontology!
    private OWLOntology getActiveOntology(){
        return this.abductionViewComponent.getOWLEditorKit().getOWLModelManager().getActiveOntology();
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if (e.getActionCommand().equals(this.ADD_OBSERVATION_SIGNATURE_COMMAND)){
            SwingUtilities.invokeLater(() -> {
                ArrayList<OWLObject> observations = this.abductionViewComponent.getObservations();
                HashSet<OWLEntity> observationEntities = new HashSet<>();
                observations.forEach(observation -> observationEntities.addAll(observation.getSignature()));
                this.selectedSignatureListModel.checkAndAddElements(observationEntities);
            });
        }
        else{
            super.actionPerformed(e);
        }
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
