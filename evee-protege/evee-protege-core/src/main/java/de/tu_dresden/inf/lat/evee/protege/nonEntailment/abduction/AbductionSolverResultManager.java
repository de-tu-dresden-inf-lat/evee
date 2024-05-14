package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.ISignatureModificationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverOntologyChangeEventGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverOntologyChangeEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.abduction.IAbductionSolverSingleResultPanelEventGenerator;
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
    private JPanel resultHolderPanel;
    private JPanel resultScrollingPanel;
    private JScrollPane resultScrollPane;
    private boolean ignoreOntologyChangeEvent = false;
    private ISignatureModificationEventListener signatureModificationEventListener;
    private int hypothesisIndex;
    private final List<SingleResultPanel> singleResultPanels;
    private IAbductionSolverOntologyChangeEventListener abductionSolverOntologyChangeEventListener;
    private final OntologyChangeListener ontologyChangeListener;


    private final Logger logger = LoggerFactory.getLogger(AbductionSolverResultManager.class);

    public AbductionSolverResultManager(){
        this.hypothesisIndex = 0;
        this.singleResultPanels = new ArrayList<>();
        this.ontologyChangeListener = new OntologyChangeListener();
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
        this.resultScrollingPanel = new JPanel();
        this.resultScrollingPanel.setLayout(new BoxLayout(this.resultScrollingPanel, BoxLayout.PAGE_AXIS));
        this.resultScrollPane = ComponentFactory.createScrollPane(this.resultScrollingPanel);
        this.resultHolderPanel.add(this.resultScrollPane, BorderLayout.CENTER);
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
                    this.owlEditorKit,ontology,
                    missingEntailment, result, hypothesisIndex);
            singleResultPanel.registerSignatureModificationEventListener(this);
            singleResultPanel.registerSingleResultPanelEventListener(this);
            this.singleResultPanels.add(singleResultPanel);
            this.resultScrollingPanel.add(singleResultPanel);
            this.hypothesisIndex++;
        });
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
//            width *0.3 is default divider location for split pane in the main view
        this.resultHolderPanel.setPreferredSize(new Dimension(
                (int) (screenWidth * 0.3),
                this.resultHolderPanel.getHeight()));
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



}
