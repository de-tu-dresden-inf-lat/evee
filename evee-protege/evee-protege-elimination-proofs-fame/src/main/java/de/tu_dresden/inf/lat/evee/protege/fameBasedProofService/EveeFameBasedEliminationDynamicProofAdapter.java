package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeEliminationDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeFameBasedEliminationDynamicProofAdapter extends AbstractEveeEliminationDynamicProofAdapter {

    public EveeFameBasedEliminationDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen,
                                                       EveeFameBasedEliminationProofPreferencesManager proofPreferencesManager,
                                                       EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
    }

}
