package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final SymbolMinimalForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSymbolMinimalDynamicProofAdapter(SymbolMinimalForgettingBasedProofGenerator iProofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(iProofGen, proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = iProofGen;
    }

    @Override
    public void start(OWLAxiom entailment, OWLEditorKit editorKit){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
        this.innerProofGenerator.setVaryJustifications(varyJustifications);
        this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
        super.start(entailment, editorKit);
    }

}
