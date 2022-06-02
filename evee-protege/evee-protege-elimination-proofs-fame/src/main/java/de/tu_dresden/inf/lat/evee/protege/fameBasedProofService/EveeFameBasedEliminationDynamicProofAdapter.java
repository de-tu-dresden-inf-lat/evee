package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.AbstractEveeEliminationDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.abstractEliminationProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeFameBasedEliminationDynamicProofAdapter extends AbstractEveeEliminationDynamicProofAdapter {

    public EveeFameBasedEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen, String uiTitle, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager) {
        super(iProofGen, uiTitle, proofPreferencesManager);
    }

}
