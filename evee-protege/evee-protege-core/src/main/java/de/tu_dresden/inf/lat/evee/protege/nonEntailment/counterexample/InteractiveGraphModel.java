package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.SubsumptionHoldsException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.ControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelComponent;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.SimpleControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.*;
import org.apache.log4j.Logger;


/**
 * The `InteractiveGraphModel` class represents a model of an ontology that is interactive, meaning it can be displayed to the user, and the user can interact with it. This class is used for ontology analysis as well as visualizing counterexamples.
 */
public class InteractiveGraphModel implements IInteractiveComponent, IExplanationGenerator<Void> {
            
    private static final int DEFAULT_LABELS_NUM = 2;

    private final Logger logger = Logger.getLogger(InteractiveGraphModel.class);
    
    private final GraphModelComponent graphModelComponent;
    private final IGraphModelControlPanel controlPanel;
    private final IGraphViewService graphGenerator;

    private  IGraphView graphView;
    private int currentLabelsNum = DEFAULT_LABELS_NUM;
    private CounterexampleModel model;

    /**
     * Constructor for the `InteractiveGraphModel` class.
     *
     * @param modelGenerator The model generator for ontology analysis.
     * @param graphGenerator The service providing graphical representation functionality for the model.
     * @param ontology The ontology being analyzed.
     * @param observation The subclass axiom for counterexample generation (if available, or set to null if not required).
     * @param owlEditorKit The OWL editor interface.
     * @throws ModelGenerationException An exception thrown in case of model generation error.
     * @throws SubsumptionHoldsException An exception thrown if subsumption holds.
     */

    public InteractiveGraphModel(CounterexampleModel model,
                                 IGraphViewService graphGenerator,
                                 ICounterexampleGenerationEventListener generationEventListener,
                                 boolean simpleMode)
            throws ModelGenerationException, SubsumptionHoldsException {

        this.graphGenerator = graphGenerator;
        this.model = model;

        if(simpleMode) {
            this.controlPanel = new SimpleControlPanel(model.getOWLEditorKit());
        } else {
            this.controlPanel = new ControlPanel(model.getOWLEditorKit());
        }

        logger.warn("line 83"); //debugLog
        this.controlPanel.addCounterexampleGenerationEventListener(generationEventListener);
         
        logger.warn("line 85"); //debugLog

        this.graphView = graphGenerator.computeView(model.getModel(),
                model.getOntology(),
                model.getMarkedInds(),
                DEFAULT_LABELS_NUM);

        logger.warn("line 90"); //debugLog


        this.graphModelComponent = new GraphModelComponent(graphView, controlPanel);
    }


    public void recomputeGraphView(int labelsNum) {
        currentLabelsNum = labelsNum;
        graphView = graphGenerator.computeView(model.getModel(),
                model.getOntology(),
                model.getMarkedInds(),
                currentLabelsNum);

        graphModelComponent.update(graphView);
        logger.info("View is recomputed");
    }

    @Override
    public GraphModelComponent toComponent() {
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
