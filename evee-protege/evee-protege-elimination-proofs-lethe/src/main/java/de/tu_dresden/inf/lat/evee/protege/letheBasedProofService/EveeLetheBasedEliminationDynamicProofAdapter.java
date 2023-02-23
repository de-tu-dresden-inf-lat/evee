package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.AbstractEveeEliminationDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeLetheBasedEliminationDynamicProofAdapter extends AbstractEveeEliminationDynamicProofAdapter {

    public EveeLetheBasedEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, String uiTitle, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(iProofGen, uiTitle, proofPreferencesManager);
    }

}
