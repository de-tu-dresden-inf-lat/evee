package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class EveeElkBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    public EveeElkBasedDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> iProofGen,
                                           EveeElkBasedExtractorProofPreferencesManager proofPreferencesManager,
                                           EveeDynamicProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
    }

}
