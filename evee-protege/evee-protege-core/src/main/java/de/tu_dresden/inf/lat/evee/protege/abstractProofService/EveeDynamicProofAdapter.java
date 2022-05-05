package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EveeDynamicProofAdapter implements DynamicProof<Inference<? extends OWLAxiom>> {

    protected IProof<OWLAxiom> iProof;
    protected IProofGenerator<OWLAxiom, OWLOntology> iProofGen;
    protected OWLAxiom entailment;
    protected OWLOntology ontology;
    protected OWLReasoner reasoner;
    protected boolean generationComplete = false;
    protected boolean generationSuccess = false;
    protected boolean ontologyChanged = true;
    protected final String LOADING = "Please wait while the proof is generated";
    protected String errorMsg = "";
    protected final Logger logger = LoggerFactory.getLogger(EveeDynamicProofAdapter.class);
    protected HashSet<ChangeListener> inferenceChangeListener = new HashSet<>();
    protected String uiTitle, setId, preferenceId, preferenceKey;
    protected EveeDynamicProofUIWindow uiWindow;

    public EveeDynamicProofAdapter(OWLOntology ontology, OWLReasoner reasoner, IProofGenerator<OWLAxiom, OWLOntology> iProofGen, String uiTitle, String setId, String preferenceId, String preferenceKey){
        this.iProofGen = iProofGen;
        this.ontology = ontology;
        this.reasoner = reasoner;
        this.uiTitle = uiTitle;
        this.setId = setId;
        this.preferenceId = preferenceId;
        this.preferenceKey = preferenceKey;
        this.logger.debug("DynamicProofAdapter created");
    }



    public void setOntology(OWLOntology ontology){
        this.ontology = ontology;
        this.ontologyChanged = true;
    }

    @Override
    public void addListener(ChangeListener changeListener) {
        if (changeListener instanceof DynamicProof.ChangeListener){
            this.inferenceChangeListener.add(changeListener);
        }
    }

    @Override
    public void removeListener(ChangeListener changeListener) {
        this.inferenceChangeListener.remove(changeListener);
    }

    @Override
    public void dispose() {
    }

    @Override
    public Collection<? extends Inference<? extends OWLAxiom>> getInferences(Object o) {
        if (! (o instanceof org.semanticweb.owlapi.model.OWLAxiom)){
            return Collections.emptyList();
        }
        if (this.generationComplete){
            if (this.generationSuccess){
                return this.createActualInferences((OWLAxiom) o);
            }
            else{
                return this.createPlaceholderInference(this.errorMsg, (OWLAxiom) o);
            }
        }
        else{
            return this.createPlaceholderInference(this.LOADING, (OWLAxiom) o);
        }
    }

    protected void proofGenerationSuccess(IProof<OWLAxiom> newProof) {
        this.iProof = newProof;
        this.uiWindow.proofGenerationFinished();
        this.uiWindow.disposeLoadingScreen();
        this.proofGenerationFinished(true);
    }

//    todo: do we actually want this to happen? no indicator to user is shown unless little grey triangle is clicked
    protected void proofGenerationFailed(String errorMsg){
        this.errorMsg = errorMsg;
        this.uiWindow.proofGenerationFinished();
        this.uiWindow.disposeLoadingScreen();
        this.proofGenerationFinished(false);
    }

    protected void proofGenerationError(String errorMsg){
        this.errorMsg = errorMsg;
        this.uiWindow.proofGenerationFinished();
        this.uiWindow.showError(errorMsg);
        this.proofGenerationFinished(false);
    }

    protected void proofGenerationCancelled(IProof<OWLAxiom> newProof){
        this.iProof = newProof;
        this.uiWindow.proofGenerationFinished();
        this.uiWindow.disposeCancelDialog();
        this.uiWindow.showSubOptimalProofMessage();
        this.proofGenerationFinished(true);
    }

    protected void proofGenerationFinished(boolean generationSuccess){
        this.generationComplete = true;
        this.generationSuccess = generationSuccess;
        for (ChangeListener listener : this.inferenceChangeListener){
            listener.inferencesChanged();
        }
    }

    protected void proofNotSupported(){
        this.generationComplete = true;
        this.generationSuccess = false;
        this.errorMsg = "Proof not supported";
        this.uiWindow.disposeLoadingScreen();
        for (ChangeListener listener : this.inferenceChangeListener){
            listener.inferencesChanged();
        }
    }

    public void cancelProofGeneration(){
        this.iProofGen.cancel();
    }


    private Collection<? extends Inference<? extends OWLAxiom>> createPlaceholderInference (String msg, OWLAxiom entailment){
        ArrayList<EveeInferenceAdapter> inferences = new ArrayList<>();
        IInference<OWLAxiom> placeholderInference = new IInference<OWLAxiom>() {
            @Override
            public OWLAxiom getConclusion() {
                return entailment;
            }

            @Override
            public String getRuleName() {
                return msg;
            }

            @Override
            public List<? extends OWLAxiom> getPremises() {
                return new ArrayList<>();
            }
        };
        inferences.add(new EveeInferenceAdapter(placeholderInference));
        return inferences;
    }

    private Collection<? extends Inference<? extends OWLAxiom>> createActualInferences (OWLAxiom entailment){
        Collection<IInference<OWLAxiom>> eveeInferences = this.iProof.getInferences( entailment);
        ArrayList<EveeInferenceAdapter> protegeInferences = new ArrayList<>();
        eveeInferences.forEach(eveeInference -> protegeInferences.add(new EveeInferenceAdapter(eveeInference)));
        return protegeInferences;
    }

    public void createUI(OWLEditorKit editorKit){
        this.uiWindow = new EveeDynamicProofUIWindow(this, this.uiTitle, editorKit, this.setId, this.preferenceId, this.preferenceKey);
        this.uiWindow.updateMessage(this.LOADING);
    }

    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
        this.generationComplete = false;
        this.generationSuccess = false;
        for (ChangeListener listener : this.inferenceChangeListener){
            listener.inferencesChanged();
        }
        if(this.ontologyChanged){
            this.iProofGen.setOntology(this.ontology);
            this.ontologyChanged=false;
        }
        this.entailment = entailment;
        this.createUI(editorKit);
        IProgressTracker progressTracker = new EveeProofPluginProgressTracker(this.uiWindow);
        this.iProofGen.addProgressTracker(progressTracker);
        EveeProofGenerationThread proofGenThread = new EveeProofGenerationThread(this.entailment, this.ontology, this.reasoner, this.iProofGen, this);
        proofGenThread.start();
        this.uiWindow.showWindow();
    }

    public boolean hasProof(OWLAxiom entailment){
        if(this.ontologyChanged) {
            this.iProofGen.setOntology(this.ontology);
            this.ontologyChanged = false;
        }
        return this.iProofGen.supportsProof(entailment);
    }

}
