package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverOntologyChangeEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverOntologyChangeEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverSingleResultPanelEventListener;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.*;
import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AbductionSolverResultManager implements
        IAbductionSolverSingleResultPanelEventListener,
        ISignatureModificationEventListener,
        ISignatureModificationEventGenerator,
        IAbductionSolverOntologyChangeEventGenerator {

    private OWLEditorKit owlEditorKit;
    private JPanel resultHolderPanel = null;
    private boolean ignoreOntologyChangeEvent = false;
    private ISignatureModificationEventListener signatureModificationEventListener;
    private int hypothesisIndex;
    private final List<SingleResultPanel> singleResultPanelsList;
    private final List<Set<OWLAxiom>> hypothesesList;
    private OWLOntology currentOntology = null;
    private Set<OWLAxiom> currentMissingEntailment = null;
    private IAbductionSolverOntologyChangeEventListener abductionSolverOntologyChangeEventListener;
    private final OntologyChangeListener ontologyChangeListener;


    private final Logger logger = LoggerFactory.getLogger(AbductionSolverResultManager.class);

    public AbductionSolverResultManager(){
        this.hypothesisIndex = 0;
        this.singleResultPanelsList = new ArrayList<>();
        this.ontologyChangeListener = new OntologyChangeListener();
        this.hypothesesList = new ArrayList<>();
        this.currentMissingEntailment = new HashSet<>();
    }

    public void resetInternalVariables(){
        for (SingleResultPanel resultPanel : this.singleResultPanelsList){
            resultPanel.dispose();
        }
        this.singleResultPanelsList.clear();
        this.hypothesesList.clear();
        this.currentOntology = null;
        this.currentMissingEntailment.clear();
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
        this.resetInternalVariables();
        this.logger.debug("Disposed");
    }

    public JPanel getResultComponent(){
        return this.resultHolderPanel;
    }


    public void resetResultComponent(){
        this.logger.debug("Resetting result component");
        this.resetInternalVariables();
        this.resultHolderPanel = new JPanel(new BorderLayout());
        this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Hypotheses:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        UIUtilities.revalidateAndRepaintComponent(this.resultHolderPanel);
    }

    protected void createResultComponent(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                                         List<Set<OWLAxiom>> newHypotheses){
        this.logger.debug("Creating result component");
        JPanel resultScrollingPanel = new JPanel();
        resultScrollingPanel.setLayout(new BoxLayout(resultScrollingPanel, BoxLayout.PAGE_AXIS));
        this.hypothesesList.addAll(newHypotheses);
        this.hypothesisIndex = 0;
        for (SingleResultPanel panel : this.singleResultPanelsList){
            panel.dispose();
        }
        this.hypothesesList.forEach(result -> {
            SingleResultPanel singleResultPanel = new SingleResultPanel(
                    this.owlEditorKit, ontology,
                    missingEntailment, result, hypothesisIndex);
            singleResultPanel.registerSignatureModificationEventListener(this);
            singleResultPanel.registerSingleResultPanelEventListener(this);
            this.singleResultPanelsList.add(singleResultPanel);
            resultScrollingPanel.add(singleResultPanel);
            this.hypothesisIndex++;
        });
        this.resultHolderPanel.removeAll();
        this.resultHolderPanel.add(ComponentFactory.createScrollPane(resultScrollingPanel), BorderLayout.CENTER);
        UIUtilities.revalidateAndRepaintComponent(resultScrollingPanel);
    }

    public void repaintResultComponent(){
        this.logger.debug("Repainting result component");
        List<Set<OWLAxiom>> hypotheses = new ArrayList<>(this.hypothesesList);
        OWLOntology ontology = this.currentOntology;
        Set<OWLAxiom> missingEntailment = new HashSet<>(this.currentMissingEntailment);
        this.resetResultComponent();
        this.createResultComponent(ontology, missingEntailment, hypotheses);
    }

//    methods related to SingleResultPanelEvents
    @Override
    public void handleEvent(AbductionSolverSingleResultPanelEvent event) {
        if (event.getType().equals(SingleResultPanelEventType.EXPLAIN) ||
                event.getType().equals(SingleResultPanelEventType.ADD) ||
                event.getType().equals(SingleResultPanelEventType.EXPLANATION_DIALOG_CLOSED)){
            this.ignoreOntologyChangeEvent = true;
        }
        if (event.getType().equals(SingleResultPanelEventType.ADD)){
            this.abductionSolverOntologyChangeEventListener.handleEvent(
                    new AbductionSolverOntologyChangeEvent(OntologyChangeEventType.ONTOLOGY_EDITED_INTERNALLY));
        } else if (event.getType().equals(SingleResultPanelEventType.EXPLAIN) ||
                event.getType().equals(SingleResultPanelEventType.EXPLANATION_DIALOG_CLOSED)){
            this.abductionSolverOntologyChangeEventListener.handleEvent(
                    new AbductionSolverOntologyChangeEvent(OntologyChangeEventType.VIEW_COMPONENT_IGNORE_CHANGE));
        }
    }

//    SignatureModificationEvent
    @Override
    public void handleSignatureModificationEvent(SignatureModificationEvent event) {
        this.signatureModificationEventListener.handleSignatureModificationEvent(event);
    }

    @Override
    public void registerSignatureModificationEventListener(ISignatureModificationEventListener listener) {
        this.signatureModificationEventListener = listener;
    }

//    OntologyChangeEvent
    @Override
    public void registerOntologyChangeEventListener(IAbductionSolverOntologyChangeEventListener listener) {
        this.abductionSolverOntologyChangeEventListener = listener;
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
                for (OWLOntologyChange change: list){
                    if (change.getOntology().equals(owlEditorKit.getOWLModelManager().getActiveOntology())){
                        this.logger.debug("Change (to active ontology) not made by AbductionSolver");
                        resetResultComponent();
                        abductionSolverOntologyChangeEventListener.handleEvent(
                                new AbductionSolverOntologyChangeEvent(
                                        OntologyChangeEventType.ONTOLOGY_EDITED_EXTERNALLY));
                        break;
                    }
                }
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
