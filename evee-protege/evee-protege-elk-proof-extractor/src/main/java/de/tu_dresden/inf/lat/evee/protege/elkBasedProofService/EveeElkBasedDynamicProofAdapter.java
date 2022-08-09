package de.tu_dresden.inf.lat.evee.protege.elkBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.ISignatureBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeElkBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    private Logger logger = LoggerFactory.getLogger(EveeElkBasedDynamicProofAdapter.class);

    public EveeElkBasedDynamicProofAdapter(ISignatureBasedProofGenerator<OWLEntity, OWLAxiom, OWLOntology> proofGen,
                                           EveeElkBasedExtractorProofPreferencesManager proofPreferencesManager,
                                           EveeDynamicProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        super.setProofGenerator(proofGen);
        this.logger.debug("Dynamic proof adapter created.");
    }

}
