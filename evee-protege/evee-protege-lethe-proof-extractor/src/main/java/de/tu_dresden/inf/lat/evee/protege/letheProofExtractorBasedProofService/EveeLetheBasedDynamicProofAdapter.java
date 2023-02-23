package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeLetheBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    public EveeLetheBasedDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, String uiTitle, AbstractEveeProofPreferencesManager proofPreferencesManager) {
        super(iProofGen, uiTitle, proofPreferencesManager);
    }

    @Override
    protected void setProofGeneratorParameters() {
//        todo: implement
    }
}
