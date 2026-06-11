package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.SubsumptionHoldsException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.ControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelComponent;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.SimpleControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.*;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;

import javax.swing.*;
import java.util.Set;

/**
 * The `InteractiveGraphModel` class represents a model of an ontology that is interactive, meaning it can be displayed to the user, and the user can interact with it. This class is used for ontology analysis as well as visualizing counterexamples.
 */
public class InteractiveGraphModel implements IInteractiveComponent,
        ICounterexampleGenerationEventListener, IExplanationGenerator<Void> {
            
    private static final int DEFAULT_LABELS_NUM = 2;

    private final Logger logger = Logger.getLogger(InteractiveGraphModel.class);
    
    private final GraphModelComponent graphModelComponent;
    private final IGraphModelControlPanel controlPanel;
    private final IGraphViewService graphViewService;

    private  IGraphView graphView;
    private IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    private int currentLabelsNum = DEFAULT_LABELS_NUM;
    private CounterexampleModel model;

    /**
     * Constructor for the `InteractiveGraphModel` class.
     *
     * @param modelGenerator The model generator for ontology analysis.
     * @param graphViewService The service providing graphical representation functionality for the model.
     * @param ontology The ontology being analyzed.
     * @param observation The subclass axiom for counterexample generation (if available, or set to null if not required).
     * @param owlEditorKit The OWL editor interface.
     * @throws ModelGenerationException An exception thrown in case of model generation error.
     * @throws SubsumptionHoldsException An exception thrown if subsumption holds.
     */

    public InteractiveGraphModel(CounterexampleModel model,
                                 IGraphViewService graphViewService,
                                 IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener,
                                 boolean simpleMode)
            throws ModelGenerationException, SubsumptionHoldsException {

        this.viewComponentListener = viewComponentListener;
        this.graphViewService = graphViewService;
        this.model = model;

        if(simpleMode) {
            this.controlPanel = new SimpleControlPanel(model.getOWLEditorKit());
        } else {
            this.controlPanel = new ControlPanel(model.getOWLEditorKit());
        }

        logger.warn("line 83"); //debugLog
        this.controlPanel.addCounterexampleGenerationEventListener(this);
         
        logger.warn("line 85"); //debugLog

        this.graphView = graphViewService.computeView(model.getModel(),
                model.getOntology(),
                model.getMarkedInds(),
                DEFAULT_LABELS_NUM);

        logger.warn("line 90"); //debugLog


        this.graphModelComponent = new GraphModelComponent(graphView, controlPanel);
    }



    @Override
    public void onModelRefreshed(IGraphModelControlPanel source) {
        currentLabelsNum = source.getCurrentLabelsNum();
       SwingWorker<Void, Void> modelRefreshWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                recomputeGraphView();
                graphModelComponent.update(graphView);
                graphViewService.doPostProcessing();
                return null;
            }
        };
        modelRefreshWorker.execute();
    }

    @Override
    public void onModelRecomputed(IGraphModelControlPanel source) {
        Set<OWLAxiom> additionalAxioms = source.getAdditionalAxioms();
        currentLabelsNum = source.getCurrentLabelsNum();
 
        SwingWorker<Void, Void> modelRecomputeWorker = new SwingWorker<Void, Void>() {
            boolean recomputed = false;
            Exception error;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    model.recomputeModel(additionalAxioms);
                    recomputed = true;
                } catch (ModelGenerationException | InconsistentOntologyException e) {
                    logger.error(e);
                    error = e;
                }
                
                return null;
            }

            @Override
            protected void done(){
                if (!recomputed) {
                    JOptionPane.showMessageDialog(new JPanel(), "model recomputation failed: "+error.getMessage(), "Error", 0);
                    return;
                }

                recomputeGraphView();
                graphModelComponent.update(graphView);
                graphViewService.doPostProcessing();
            
            }
        };

        modelRecomputeWorker.execute();
    }

    @Override
    public void onDisjointnessesAddedToOntology(IGraphModelControlPanel source) {
        Set<OWLAxiom> additionalAxioms = source.getAdditionalAxioms();
        model.addToOntology(additionalAxioms);
    }

    private void recomputeGraphView() {
        graphView = graphViewService.computeView(model.getModel(),
                model.getOntology(),
                model.getMarkedInds(),
                currentLabelsNum);
        logger.info("View is recomputed");
    }

    @Override
    public GraphModelComponent toComponent() {
        SwingWorker<Void, Void> postprocessingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                graphViewService.doPostProcessing();
                return null;
            }
        };
        postprocessingWorker.execute();
        return graphModelComponent;
    }


    @Override
    public Void getResult() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
