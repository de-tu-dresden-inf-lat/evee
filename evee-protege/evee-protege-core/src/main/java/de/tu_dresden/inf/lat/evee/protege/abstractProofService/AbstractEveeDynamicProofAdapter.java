package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProgressTracker;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;

import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.EditorKit;
import java.util.*;

public abstract class AbstractEveeDynamicProofAdapter implements DynamicProof<Inference<? extends OWLAxiom>> {

    private IProof<OWLAxiom> iProof;
    private final IProofGenerator<OWLAxiom, OWLOntology> iProofGen;
    private OWLAxiom entailment;
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private boolean generationComplete = false;
    private boolean generationSuccess = false;
    private boolean ontologyChanged = true;
    private final String LOADING = "Please wait while the proof is generated";
    private String errorMsg = "";
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeDynamicProofAdapter.class);
    private final Set<ChangeListener> inferenceChangeListener = new HashSet<>();
    private final AbstractEveeProofPreferencesManager proofPreferencesManager;
    private final EveeDynamicProofLoadingUI uiWindow;

    public AbstractEveeDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, AbstractEveeProofPreferencesManager proofPreferencesManager, EveeDynamicProofLoadingUI uiWindow){
        this.iProofGen = iProofGen;
        this.proofPreferencesManager = proofPreferencesManager;
        this.uiWindow = uiWindow;
        this.uiWindow.setProofAdapter(this);
    }

    public void setOntology(OWLOntology ontology){
        this.ontology = ontology;
        this.ontologyChanged = true;
    }

    public void setReasoner(OWLReasoner reasoner){
        this.reasoner = reasoner;
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

    protected boolean isActive(){
        return this.proofPreferencesManager.loadIsActive();
    }

    protected void proofGenerationSuccess(IProof<OWLAxiom> newProof) {
        this.iProof = newProof;
        this.uiWindow.proofGenerationFinished();
        this.uiWindow.disposeLoadingScreen();
        this.proofGenerationFinished(true);
    }

//    currently unused; no indicator to user is shown unless little grey triangle is clicked
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
        IProgressTracker progressTracker = new EveeProofPluginProgressTracker(this.uiWindow);
        this.iProofGen.addProgressTracker(progressTracker);
        this.uiWindow.initialize(editorKit);
        this.uiWindow.updateMessage(this.LOADING);
        EveeProofGenerationThread proofGenThread = new EveeProofGenerationThread(this.entailment, this.ontology, this.reasoner, this.iProofGen, this);
        proofGenThread.start();
        this.uiWindow.showWindow();
    }

//    currently not in use as iProofGen.supportsProof is too expensive for Protege
    public boolean hasProof(OWLAxiom entailment){
        if(this.ontologyChanged) {
            this.iProofGen.setOntology(this.ontology);
            this.ontologyChanged = false;
        }
        return this.iProofGen.supportsProof(entailment);
    }

}
