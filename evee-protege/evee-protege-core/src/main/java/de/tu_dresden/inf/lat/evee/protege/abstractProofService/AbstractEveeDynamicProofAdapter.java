package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.*;

import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeKnownSignaturePreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.EveeProofAdapterKnownSignaturePreferenceManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractEveeDynamicProofAdapter implements DynamicProof<Inference<? extends OWLAxiom>> {

    private IProof<OWLAxiom> iProof;
    private IProofGenerator<OWLAxiom, OWLOntology> cachingProofGen = null;
    private ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology> signatureProofGen = null;
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private String errorMsg = "";
    private boolean generationComplete = false;
    private boolean generationSuccess = false;
    private boolean ontologyChanged = true;
    private boolean proofGeneratorChanged = true;
    private long signatureTimeStamp;
    private final String LOADING = "Please wait while the proof is generated";
    private final Logger logger = LoggerFactory.getLogger(AbstractEveeDynamicProofAdapter.class);
    private final Set<ChangeListener> inferenceChangeListener = new HashSet<>();
    private final AbstractEveeProofPreferencesManager proofPreferencesManager;
    private final EveeProofAdapterKnownSignaturePreferenceManager signaturePreferencesManager;
    private final EveeDynamicProofLoadingUI uiWindow;

    public AbstractEveeDynamicProofAdapter(AbstractEveeProofPreferencesManager proofPreferencesManager, EveeDynamicProofLoadingUI uiWindow){
        this.proofPreferencesManager = proofPreferencesManager;
        this.signaturePreferencesManager = new EveeProofAdapterKnownSignaturePreferenceManager();
        this.uiWindow = uiWindow;
        this.uiWindow.setProofAdapter(this);
        this.signatureTimeStamp = 0;
    }

    protected void setProofGenerator(ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology> proofGen){
        this.signatureProofGen = proofGen;
        this.setCachingProofGenerator();
    }

    private void setCachingProofGenerator(){
        this.logger.debug("New CachingProofGenerator created with empty cache.");
        this.cachingProofGen = new CachingProofGenerator<>(this.signatureProofGen);
        this.proofGeneratorChanged = true;
    }

    public void setOntology(OWLOntology ontology){
        this.ontology = ontology;
        this.ontologyChanged = true;
    }

    public void setReasoner(OWLReasoner reasoner){
        this.reasoner = reasoner;
    }

    protected String getProofServiceName(){
        return this.proofPreferencesManager.getProofServiceName();
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
        this.cachingProofGen.cancel();
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
        this.logger.debug("DynamicProofAdapter started.");
        assert (this.cachingProofGen != null);
        this.setProofGeneratorParameters();
        this.generationComplete = false;
        this.generationSuccess = false;
        for (ChangeListener listener : this.inferenceChangeListener){
            listener.inferencesChanged();
        }
        this.checkOntology();
        IProgressTracker progressTracker = new EveeProofPluginProgressTracker(this.uiWindow);
        this.cachingProofGen.addProgressTracker(progressTracker);
        this.uiWindow.initialize(editorKit);
        this.uiWindow.updateMessage(this.LOADING);
        EveeProofGenerationThread proofGenThread = new EveeProofGenerationThread(entailment,
                this.ontology, this.reasoner, this.cachingProofGen, this);
        proofGenThread.start();
        this.uiWindow.showWindow();
    }

    protected void setProofGeneratorParameters(){
        this.logger.debug("Checking signature.");
        assert (this.signatureProofGen != null);
        if (! this.ontology.getOntologyID().getOntologyIRI().isPresent()){
            this.logger.warn("Anonymous ontology detected. Signature of known OWLEntities could not be set.");
            return;
        }
        String ontologyName = this.ontology.getOntologyID().getOntologyIRI().get().toString();
        if (this.signaturePreferencesManager.signatureChanged(
                this.signatureTimeStamp, this.ontology, ontologyName)){
            this.setSignature(ontologyName);
            this.setCachingProofGenerator();
        }
    }

    private void setSignature(String ontologyName){
        Set<OWLEntity> signature = this.signaturePreferencesManager.getKnownSignatureForProofGeneration(
                this.ontology, ontologyName);
        this.signatureProofGen.setSignature(signature);
        this.logger.debug("Signature of known OWLEntities set to:");
        signature.forEach(owlEntity -> this.logger.debug(owlEntity.toString()));
        this.signatureTimeStamp = this.signaturePreferencesManager.getTimeStamp();
    }

    private void checkOntology(){
        this.logger.debug("Checking if ontology of cachingProofGenerator needs to be updated.");
        if(this.ontologyChanged || this.proofGeneratorChanged){
            this.logger.debug("Update necessary, handing over ontology.");
            this.cachingProofGen.setOntology(this.ontology);
            this.ontologyChanged=false;
            this.proofGeneratorChanged = false;
        }
    }

//    currently not in use as iProofGen.supportsProof is too expensive for Protege
//    public boolean hasProof(OWLAxiom entailment){
//        if(this.ontologyChanged) {
//            this.cachingProofGen.setOntology(this.ontology);
//            this.ontologyChanged = false;
//        }
//        return this.cachingProofGen.supportsProof(entailment);
//    }

}
