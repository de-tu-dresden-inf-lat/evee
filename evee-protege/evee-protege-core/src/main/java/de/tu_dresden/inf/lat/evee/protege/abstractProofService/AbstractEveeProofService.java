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


public abstract class AbstractEveeProofService extends ProofService implements OWLModelManagerListener {

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
        logger.debug("getProof called");
        this.proofAdapter.start(owlAxiom, getEditorKit());
        return this.proofAdapter;
    }

    @Override
    public Inference<? extends OWLAxiom> getExample(Inference<? extends OWLAxiom> inference) {
        return null;
    }

    @Override
    public void dispose() {
        getEditorKit().getOWLModelManager().removeListener(this);
    }

    @Override
    public void initialise() {
        getEditorKit().getOWLModelManager().addListener(this);
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

}
