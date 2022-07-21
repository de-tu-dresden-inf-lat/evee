package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final SymbolMinimalForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeFameBasedSymbolMinimalDynamicProofAdapter(SymbolMinimalForgettingBasedProofGenerator iProofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        super.setProofGenerator(iProofGen);
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = iProofGen;
    }

    @Override
    protected void setProofGeneratorParameters(OWLEditorKit editorKit) {
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
        this.innerProofGenerator.setVaryJustifications(varyJustifications);
        this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
    }

}
