package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.ISignatureBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeLetheBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    public EveeLetheBasedDynamicProofAdapter(ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology> proofGen,
                                             EveeLetheBasedExtractorProofPreferencesManager proofPreferencesManager,
                                             EveeDynamicProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.setProofGenerator(proofGen);
    }

}
