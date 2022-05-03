package de.tu_dresden.inf.lat.evee.protege.abstractProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import org.liveontologies.puli.Inference;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collections;
import java.util.List;

public class EveeInferenceAdapter implements Inference<OWLAxiom> {

    private IInference<OWLAxiom> iInference;

    public EveeInferenceAdapter(IInference<OWLAxiom> iInference){
        this.iInference = iInference;
    }

    @Override
    public String getName() {
        return this.iInference.getRuleName();
    }

    @Override
    public OWLAxiom getConclusion() {
        return this.iInference.getConclusion();
    }

    @Override
    public List<? extends OWLAxiom> getPremises() {
        return this.iInference.getPremises();
    }

}
