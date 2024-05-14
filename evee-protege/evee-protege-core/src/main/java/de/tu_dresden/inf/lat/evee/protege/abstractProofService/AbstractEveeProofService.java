package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;


public abstract class AbstractEveeProofService extends ProofService
        implements OWLModelManagerListener, OWLOntologyChangeListener {

    protected OWLOntology ontology;
    protected OWLReasoner reasoner;
    protected AbstractEveeDynamicProofAdapter proofAdapter;
    protected Logger logger = LoggerFactory.getLogger(AbstractEveeProofService.class);

    protected AbstractEveeProofService(AbstractEveeDynamicProofAdapter proofAdapter){
        this.proofAdapter = proofAdapter;
        this.proofAdapter.setOntology(this.ontology);
        this.proofAdapter.setReasoner(this.reasoner);
    }

    @Override
    public boolean hasProof(OWLAxiom owlAxiom) {
        return this.proofAdapter.isActive();
    }

    @Override
    public DynamicProof<Inference<? extends OWLAxiom>> getProof(OWLAxiom owlAxiom){
        this.logger.info("ProofService <{}>, method getProof called with input <{}>.",
                this.proofAdapter.getProofServiceName(), owlAxiom);
        this.proofAdapter.start(owlAxiom, getEditorKit());
        return this.proofAdapter;
    }

    @Override
    public Inference<? extends OWLAxiom> getExample(Inference<? extends OWLAxiom> inference) {
        return null;
    }

    @Override
    public void dispose() {
        this.getEditorKit().getOWLModelManager().removeListener(this);
        this.getEditorKit().getOWLModelManager().removeOntologyChangeListener(this);
    }

    @Override
    public void initialise() {
        this.getEditorKit().getOWLModelManager().addListener(this);
        this.getEditorKit().getOWLModelManager().addOntologyChangeListener(this);
        this.changeOntology();
        this.changeReasoner();
        this.logger.debug("ProofService initialized");
    }

    @Override
    public void handleChange(OWLModelManagerChangeEvent owlModelManagerChangeEvent) {
        if (owlModelManagerChangeEvent.isType(EventType.REASONER_CHANGED)){
            this.changeReasoner();
        }
        else if (owlModelManagerChangeEvent.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)){
            this.changeOntology();
        }
    }

    private void changeOntology(){
        this.ontology = getEditorKit().getOWLModelManager().getActiveOntology();
        this.proofAdapter.setOntology(this.ontology);
    }

    private void changeReasoner(){
        this.reasoner = getEditorKit().getOWLModelManager().getReasoner();
    }

    @Override
    public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes){
        this.logger.debug("Change to ontology detected");
        for (OWLOntologyChange change: changes){
            if (change.getOntology().equals(getEditorKit().getOWLModelManager().getActiveOntology())){
                this.logger.debug("Change made to current ACTIVE ontology");
                this.proofAdapter.resetCachingProofGenerator();
                break;
            }
        }

    }

}
