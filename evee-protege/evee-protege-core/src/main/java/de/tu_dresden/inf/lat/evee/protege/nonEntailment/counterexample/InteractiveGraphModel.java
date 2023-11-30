package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.SubsumptionHoldsException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLModelGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.ControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.ui.GraphModelComponent;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.MappingUtils;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.ReasoningUtils;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.*;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The `InteractiveGraphModel` class represents a model of an ontology that is interactive, meaning it can be displayed to the user, and the user can interact with it. This class is used for ontology analysis as well as visualizing counterexamples.
 */
public class InteractiveGraphModel implements IInteractiveComponent, ICounterexampleGenerationEventListener {
    private final OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private final OWLOntology ontology;
    private final IOWLCounterexampleGenerator modelGenerator;
    private final GraphModelComponent graphModelComponent;
    private final Logger logger = Logger.getLogger(InteractiveGraphModel.class);
    private final IGraphModelControlPanel controlPanel;
    private OWLSubClassOfAxiom observation;
    private static final int DEFAULT_LABELS_NUM = 2;
    private final OWLEditorKit owlEditorKit;
    private final IGraphViewService graphViewService;
    private boolean requiersSubsumptionCheck = true;
    private  IGraphView graphView;
    private IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener;
    private int currentLabelsNum = DEFAULT_LABELS_NUM;
    private Set<OWLIndividualAxiom> model;
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

    public InteractiveGraphModel(IOWLCounterexampleGenerator modelGenerator,
                                 IGraphViewService graphViewService,
                                 OWLOntology ontology,
                                 OWLSubClassOfAxiom observation,
                                 OWLEditorKit owlEditorKit,
                                 IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewComponentListener)
            throws ModelGenerationException, SubsumptionHoldsException {
        this.owlEditorKit = owlEditorKit;
        this.ontology = ontology;
        this.viewComponentListener = viewComponentListener;
        if(observation == null) {
            this.requiersSubsumptionCheck = false;
        } else {
            this.observation = observation;
        }
        this.modelGenerator = modelGenerator;
        this.graphViewService = graphViewService;
        computeModel();
        logger.debug("model: "+ this.model);
        this.controlPanel = new ControlPanel(owlEditorKit);
        this.controlPanel.addCounterexampleGenerationEventListener(this);
        this.graphView = graphViewService.computeView(model,
                ontology,
                modelGenerator.getMarkedIndividuals(),
                DEFAULT_LABELS_NUM);
        this.graphModelComponent = new GraphModelComponent(graphView,controlPanel);
    }



    @Override
    public void onModelRefreshed(IGraphModelControlPanel source) {
        currentLabelsNum = source.getCurrentLabelsNum();
        SwingWorker modelRefreshWorker = new SwingWorker() {
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
        SwingWorker modelRecomputeWorker = new SwingWorker() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    recomputeModel(additionalAxioms);
                    recomputeGraphView();
                    graphModelComponent.update(graphView);
                    graphViewService.doPostProcessing();
                } catch (ModelGenerationException | InconsistentOntologyException e) {
                    JOptionPane.showMessageDialog(new JPanel(), "Adding disjointnesses causes the following problem: "+e.getMessage(), "Error", 0);
                }
                return null;
            }
        };
        modelRecomputeWorker.execute();
    }
    @Override
    public void onDisjointnessesAddedToOntology(IGraphModelControlPanel source) {
        logger.debug("additional axioms to add: " +source.getAdditionalAxioms());
        man.addAxioms(ontology,source.getAdditionalAxioms());
        man.addAxioms(owlEditorKit.getModelManager().getActiveOntology(), source.getAdditionalAxioms());
    }

    private void computeModel()
            throws SubsumptionHoldsException, ModelGenerationException, InconsistentOntologyException {
        if(!ReasoningUtils.isConsistent(ontology, observation)) {
            throw new InconsistentOntologyException();
        }
        if(requiersSubsumptionCheck) {
            if(ReasoningUtils.subsumptionHolds(ontology, observation)) {
                throw new SubsumptionHoldsException();
            }
        }
        model = modelGenerator.generateModel();
        logger.info("Model is computed");
    }

    private void recomputeModel(Set<OWLAxiom> additionalAxioms)
            throws ModelGenerationException, InconsistentOntologyException {

        Set<OWLSubClassOfAxiom> subClassOfAxioms = MappingUtils.disjToSubclassOfAx(additionalAxioms);
        man.addAxioms(ontology, subClassOfAxioms);
        try {
            if(!ReasoningUtils.isConsistent(ontology, observation)) {
                throw new InconsistentOntologyException();
            }
            model = modelGenerator.generateModel();
            man.removeAxioms(ontology, subClassOfAxioms);
        } catch ( InconsistentOntologyException | ModelGenerationException e) {
            man.removeAxioms(ontology, subClassOfAxioms);
            throw  e;
        }
        logger.info("Model is recomputed");
    }

    private void recomputeGraphView() {
        graphView = graphViewService.computeView(model,
                ontology,
                modelGenerator.getMarkedIndividuals(),
                currentLabelsNum);
        logger.info("View is recomputed");
    }
    public Set<OWLIndividualAxiom> getModel() {
        return model;
    }
    @Override
    public GraphModelComponent toComponent() {
        SwingWorker postprocessingWorker = new SwingWorker() {
            @Override
            protected Void doInBackground() throws Exception {
                graphViewService.doPostProcessing();
                return null;
            }
        };
        postprocessingWorker.execute();
        return graphModelComponent;
    }



}
