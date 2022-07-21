package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.ForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedForgettingDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final ForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedForgettingDynamicProofAdapter.class);

    public EveeFameBasedForgettingDynamicProofAdapter(ForgettingBasedProofGenerator iProofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
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
    }

}
