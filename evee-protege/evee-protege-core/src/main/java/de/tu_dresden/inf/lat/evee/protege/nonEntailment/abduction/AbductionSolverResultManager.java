package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IAbductionSolverOntologyChangeEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.IAbductionSolverResultButtonEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.*;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AbductionSolverResultManager implements IAbductionSolverResultButtonEventListener {

    private OWLEditorKit owlEditorKit;
    private JPanel resultHolderPanel;
    private JPanel resultScrollingPanel;
    private boolean ignoreOntologyChangeEvent = false;
    private int hypothesisIndex;
    private final List<SingleResultPanel> singleResultPanels;
    private final IAbductionSolverOntologyChangeEventListener abductionSolverOntologyChangeEventListener;
    private final IAbductionSolverResultButtonEventListener abductionSolverResultButtonEventListener;
    private final OntologyChangeListener ontologyChangeListener;


    private Logger logger = LoggerFactory.getLogger(AbductionSolverResultManager.class);

    public AbductionSolverResultManager(IAbductionSolverOntologyChangeEventListener ontologyChangeEventListener,
                                        IAbductionSolverResultButtonEventListener resultButtonEventListener){
        this.hypothesisIndex = 0;
        this.singleResultPanels = new ArrayList<>();
        this.ontologyChangeListener = new OntologyChangeListener();
        this.abductionSolverOntologyChangeEventListener = ontologyChangeEventListener;
        this.abductionSolverResultButtonEventListener = resultButtonEventListener;
    }

    public void setup(OWLEditorKit owlEditorKit){
        this.owlEditorKit = owlEditorKit;
    }

    public void initialise(){
        this.logger.debug("Initialising");
        this.owlEditorKit.getOWLModelManager().addOntologyChangeListener(this.ontologyChangeListener);
        this.owlEditorKit.getOWLModelManager().addListener(this.ontologyChangeListener);
        this.logger.debug("Initialised");
    }

    public void dispose(){
        this.logger.debug("Disposing");
        this.owlEditorKit.getOWLModelManager().removeOntologyChangeListener(this.ontologyChangeListener);
        this.owlEditorKit.getOWLModelManager().removeListener(this.ontologyChangeListener);
        for (SingleResultPanel resultPanel : this.singleResultPanels){
            resultPanel.dispose();
        }
        this.singleResultPanels.clear();
        this.logger.debug("Disposed");
    }

    public JPanel getResultComponent(){
        return this.resultHolderPanel;
    }


    public void resetResultComponent(){
        this.logger.debug("Resetting result component");
        this.hypothesisIndex = 0;
        for (SingleResultPanel panel : this.singleResultPanels){
            panel.dispose();
        }
        this.singleResultPanels.clear();
        this.resultHolderPanel = new JPanel(new BorderLayout());
        this.resultHolderPanel.setMaximumSize(new Dimension(800, 1000));
        this.resultScrollingPanel = new JPanel();
        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
        JScrollPane resultScrollPane = new JScrollPane();
        resultScrollPane.setViewportView(this.resultScrollingPanel);
        this.resultHolderPanel.add(resultScrollPane);
        this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Hypotheses:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.resultHolderPanel.repaint();
        this.resultHolderPanel.revalidate();
    }

    protected void createResultComponent(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                                         List<Set<OWLAxiom>> hypotheses){
        hypotheses.forEach(result -> {
            SingleResultPanel singleResultPanel = new SingleResultPanel(
                    this.owlEditorKit,ontology,
                    missingEntailment, result, hypothesisIndex);
            singleResultPanel.registerListener(this);
            this.singleResultPanels.add(singleResultPanel);
            this.resultScrollingPanel.add(singleResultPanel);
            this.hypothesisIndex++;
        });
        this.resultHolderPanel.repaint();
        this.resultHolderPanel.revalidate();
    }

    private void changeAddButtonStatus(boolean newStatus){
        for (SingleResultPanel panel : this.singleResultPanels){
            panel.setAddButtonStatus(newStatus);
        }
    }

    @Override
    public void handleEvent(AbductionSolverResultButtonEvent event) {
        this.ignoreOntologyChangeEvent = true;
        switch (event.getType()){
            case ADD:
                changeAddButtonStatus(false);
                this.abductionSolverResultButtonEventListener.handleEvent(
                        new AbductionSolverResultButtonEvent(
                                ResultButtonEventType.ADD));
                break;
            case ADD_AND_PROVE:
                changeAddButtonStatus(false);
                this.abductionSolverResultButtonEventListener.handleEvent(
                        new AbductionSolverResultButtonEvent(
                                ResultButtonEventType.ADD_AND_PROVE));
                break;
            case DELETE:
                changeAddButtonStatus(true);
                this.abductionSolverResultButtonEventListener.handleEvent(
                        new AbductionSolverResultButtonEvent(
                                ResultButtonEventType.DELETE));
                break;
        }
    }

    private class OntologyChangeListener implements OWLModelManagerListener, OWLOntologyChangeListener {

        private final Logger logger = LoggerFactory.getLogger(OntologyChangeListener.class);

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> list) {
            this.logger.debug("Change to ontology detected");
            if (ignoreOntologyChangeEvent){
                this.logger.debug("Change made by AbductionSolver, event ignored");
                ignoreOntologyChangeEvent = false;
            } else{
                this.logger.debug("Change not made by AbductionSolver");
                resetResultComponent();
                abductionSolverOntologyChangeEventListener.handleEvent(
                        new AbductionSolverOntologyChangeEvent(
                                OntologyChangeEventType.ONTOLOGY_EDITED));
            }
        }

        @Override
        public void handleChange(OWLModelManagerChangeEvent owlModelManagerChangeEvent) {
            if (owlModelManagerChangeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) ||
                    owlModelManagerChangeEvent.isType(EventType.ONTOLOGY_RELOADED)){
                this.logger.debug("Change/Reload of active ontology detected");
                resetResultComponent();
                abductionSolverOntologyChangeEventListener.handleEvent(
                        new AbductionSolverOntologyChangeEvent(
                                OntologyChangeEventType.ACTIVE_ONTOLOGY_CHANGED));
            }
        }
    }



}
