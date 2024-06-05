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
    private JPanel resultScrollingPanel = null;
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
        this.hypothesisIndex = 0;
        this.resetInternalVariables();
        this.resultHolderPanel = new JPanel(new BorderLayout());
        this.resultScrollingPanel = new JPanel();
        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
        JScrollPane resultScrollPane = ComponentFactory.createScrollPane(this.resultScrollingPanel);
        this.resultHolderPanel.add(resultScrollPane, BorderLayout.CENTER);
        this.resultHolderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "Hypotheses:"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        UIUtilities.revalidateAndRepaintComponent(this.resultHolderPanel);
    }

    protected void createResultComponent(OWLOntology ontology, Set<OWLAxiom> missingEntailment,
                                         List<Set<OWLAxiom>> hypotheses){
        this.logger.debug("Creating result component");
        hypotheses.forEach(result -> {
            SingleResultPanel singleResultPanel = new SingleResultPanel(
                    this.owlEditorKit, ontology,
                    missingEntailment, result, hypothesisIndex);
            singleResultPanel.registerSignatureModificationEventListener(this);
            singleResultPanel.registerSingleResultPanelEventListener(this);
            this.singleResultPanelsList.add(singleResultPanel);
            this.hypothesesList.add(result);
            this.resultScrollingPanel.add(singleResultPanel);
            this.hypothesisIndex++;
        });
        UIUtilities.revalidateAndRepaintComponent(this.resultHolderPanel);
    }

//    methods related to SingleResultPanelEvents
    @Override
    public void handleEvent(AbductionSolverSingleResultPanelEvent event) {
        this.ignoreOntologyChangeEvent = true;
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
                                        OntologyChangeEventType.ONTOLOGY_EDITED));
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

    public void repaintResultComponent(){
        this.logger.debug("Repainting result component");
        List<Set<OWLAxiom>> hypotheses = new ArrayList<>(this.hypothesesList);
        OWLOntology ontology = this.currentOntology;
        Set<OWLAxiom> missingEntailment = new HashSet<>(this.currentMissingEntailment);
        this.resetResultComponent();
        if (ontology == null){
            this.logger.debug("TEST_MESSAGE: ontology is null!!!");
        }
        this.createResultComponent(ontology, missingEntailment, hypotheses);
    }

}
