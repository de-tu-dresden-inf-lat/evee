package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.ForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedForgettingDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final ForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedForgettingDynamicProofAdapter.class);

    public EveeFameBasedForgettingDynamicProofAdapter(ForgettingBasedProofGenerator proofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = proofGen;
        super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(
                this.innerProofGenerator));
        this.logger.debug("Dynamic proof adapter created.");
    }

    @Override
    protected void setProofGeneratorParameters() {
        this.logger.debug("Checking parameters for proof generator.");
        if (this.proofPreferencesManager.skipStepsChanged(this.skipStepsTimeStamp)){
            this.setSkipSteps();
            super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(
                    this.innerProofGenerator));
        }
        super.setProofGeneratorParameters();
    }

    private void setSkipSteps(){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
    }

}
