package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.MinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final MinimalForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedMinimalDynamicProofAdapter.class);

    public EveeFameBasedMinimalDynamicProofAdapter(MinimalForgettingBasedProofGenerator proofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = proofGen;
        super.setInnerProofGenerator(this.innerProofGenerator);
        super.resetCachingProofGenerator();
        this.logger.debug("Dynamic proof adapter created.");
    }

    @Override
    protected void setProofGeneratorParameters(boolean parametersChanged) {
        this.logger.debug("Checking parameters for proof generator.");
        if (this.proofPreferencesManager.skipStepsChanged(this.skipStepsTimeStamp)){
            this.setSkipSteps();
            parametersChanged = true;
        }
        super.setProofGeneratorParameters(parametersChanged);
    }

    private void setSkipSteps(){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
    }

}
