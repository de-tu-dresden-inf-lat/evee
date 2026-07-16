package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import java.awt.Component;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;

import de.tu_dresden.inf.lat.evee.general.data.exceptions.ModelGenerationException;
import de.tu_dresden.inf.lat.evee.general.interfaces.IExplanationGenerationListener;
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.nonEntailment.interfaces.IOWLCounterexampleGenerator;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample.util.GraphStyleSheets;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.INonEntailmentExplanationService;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.ICounterexampleGenerationEventListener;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphModelControlPanel;
import de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces.counterexample.IGraphViewService;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEvent;
import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.ExplanationEventType;

public class CounterexampleController implements ICounterexampleGenerationEventListener{

    private final Logger logger = Logger.getLogger(CounterexampleController.class);

    private final IGraphViewService graphGenerator;
    private final CounterexampleModel model;
    
    private IProgressTracker progressTracker; //TODO
    private IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> viewListener;
    private INonEntailmentExplanationService<OWLIndividualAxiom> parentService;

    private InteractiveGraphModel graphModel;
    private GraphGenerationWorker generationWorker;
    
    private boolean simpleMode;
    private boolean generationCancelled = false;
    private String errorMessage = "";

    public CounterexampleController(INonEntailmentExplanationService<OWLIndividualAxiom> parentService, boolean simpleMode){
        this.parentService = parentService;
        this.simpleMode = simpleMode;
        this.model = new CounterexampleModel();
        this.graphGenerator = new GraphViewGenerator(GraphStyleSheets.PROTEGE);
    }

    @Override
    public void onModelRefreshed(IGraphModelControlPanel source) {
        int labelsNum = source.getCurrentLabelsNum();
       SwingWorker<Void, Void> modelRefreshWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                graphModel.recomputeGraphView(labelsNum);
                graphGenerator.doPostProcessing();
                return null;
            }
        };
        modelRefreshWorker.execute();
    }

    @Override
    public void onModelRecomputed(IGraphModelControlPanel source) {
        Set<OWLAxiom> additionalAxioms = source.getAdditionalAxioms();
        int labelsNum = source.getCurrentLabelsNum();
        
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

                graphModel.recomputeGraphView(labelsNum);
                graphGenerator.doPostProcessing();
            
            }
        };

        modelRecomputeWorker.execute();
    }

    @Override
    public void onDisjointnessesAddedToOntology(IGraphModelControlPanel source) {
        Set<OWLAxiom> additionalAxioms = source.getAdditionalAxioms();
        model.addToOntology(additionalAxioms);
    }

    public boolean supportsExplanation() {
        return this.model.supportsExplanation();
    }

    public void computeCounterexampleGraph(){
        generationWorker = new GraphGenerationWorker(parentService, this);

        logger.warn("starting generation task thread"); // debugLog
        generationCancelled = false;
        generationWorker.execute();

    };

    public void cancelGraphComputation() {
        if (generationWorker != null && !generationWorker.isDone()) {
            generationCancelled = true;
            generationWorker.cancel(true);
            logger.info("Counterexample generation is canceled");
        }
    }

    public Stream<Set<OWLIndividualAxiom>> generateExplanations() {
        return this.model.generateExplanations();
    }

    public Component getGraphComponent() {
        return graphModel.toComponent();
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setOWLEditorKit(OWLEditorKit kit){
        model.setOWLEditorKit(kit);
    }

    public void setObservation(Set<OWLAxiom> observation) {
        this.model.setObservation(observation);
    }

    public void setSignature(Collection<OWLEntity> signature) {
        this.model.setSignature(signature);
    }

    public void setOntology(OWLOntology ontology) {
        this.model.setOntology(ontology);
    }

    public void setCounterexampleGenerator(IOWLCounterexampleGenerator counterexampleGenerator) {
        this.model.setCounterexampleGenerator(counterexampleGenerator);
    }

    public void setSimpleMode(boolean simpleMode) {
        this.simpleMode = simpleMode;
    }
    
    public void setViewListener(IExplanationGenerationListener<ExplanationEvent<INonEntailmentExplanationService<?>>> listener) {
        this.viewListener = listener;
    }

    public void setProgressTracker(IProgressTracker tracker) {
        this.progressTracker = tracker;
        this.model.addProgressTracker(tracker);
    }

    {
    SwingWorker<Void, Void> postprocessingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                graphGenerator.doPostProcessing();
                return null;
            }
        };
        postprocessingWorker.execute();
    }

    private class GraphGenerationWorker extends SwingWorker<Void, Void> {

        private final INonEntailmentExplanationService<OWLIndividualAxiom> parentService;
        private final Logger loggerThread = Logger.getLogger(GraphGenerationWorker.class);

        private ICounterexampleGenerationEventListener generationEventListener;

        public GraphGenerationWorker(INonEntailmentExplanationService<OWLIndividualAxiom> parentService,ICounterexampleGenerationEventListener generationEventListener) {
            this.parentService = parentService;
            this.generationEventListener = generationEventListener;
        }

        @Override
        protected Void doInBackground() throws Exception {
            loggerThread.warn("model generation task background thread tarted"); //debugLog
            model.computeModel();
            return null;
        }

        @Override
        protected void done() {
            try {
                get(); //rethrows any exception that occurred during doInBackground()
                graphModel = new InteractiveGraphModel(
                        model,
                        graphGenerator,
                        generationEventListener,
                        simpleMode);

                viewListener.handleEvent(new ExplanationEvent<>(this.parentService, ExplanationEventType.COMPUTATION_COMPLETE));

            } catch (Throwable e) {
                if (generationCancelled) {
                    loggerThread.info("Counterexample generation is canceled");
                    return;
                }

                loggerThread.error("Model generation error", e);
                errorMessage = "Model generation error:\n" + e.getMessage();
                viewListener.handleEvent(new ExplanationEvent<>(this.parentService, ExplanationEventType.ERROR));    
            }
            
        }

    }

}
