package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedDynamicProofAdapter extends AbstractEveeDynamicProofAdapter {

    private Logger logger = LoggerFactory.getLogger(EveeLetheBasedDynamicProofAdapter.class);

    public EveeLetheBasedDynamicProofAdapter(IProofGenerator<OWLAxiom, OWLOntology> proofGen,
                                             EveeLetheBasedExtractorProofPreferencesManager proofPreferencesManager,
                                             EveeDynamicProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        super.setInnerProofGenerator(proofGen);
        super.resetCachingProofGenerator();
        this.logger.debug("Dynamic proof adapter created.");
    }

}
