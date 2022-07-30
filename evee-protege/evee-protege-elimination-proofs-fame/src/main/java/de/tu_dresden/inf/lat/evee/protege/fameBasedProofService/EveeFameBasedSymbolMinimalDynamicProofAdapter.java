package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.preferences.AbstractEveeEliminationProofPreferencesManager;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final AbstractEveeEliminationProofPreferencesManager proofPreferencesManager;
    private final SymbolMinimalForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;
    private long varyJustificationsTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeFameBasedSymbolMinimalDynamicProofAdapter(SymbolMinimalForgettingBasedProofGenerator proofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(proofGen));
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = proofGen;
        this.setVaryJustifications();
        super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(
                this.innerProofGenerator));
        this.logger.debug("Dynamic proof adapter created.");
    }

    @Override
    protected void setProofGeneratorParameters() {
        this.logger.debug("Checking parameters for proof generator.");
        boolean parameterChanged = false;
        if (this.proofPreferencesManager.skipStepsChanged(this.skipStepsTimeStamp)) {
            this.setSkipSteps();
            parameterChanged = true;
        }
        if (this.proofPreferencesManager.varyJustificationsChanged(this.varyJustificationsTimeStamp)){
            this.setVaryJustifications();
            parameterChanged = true;
        }
        if (parameterChanged){
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

    private void setVaryJustifications(){
        boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
        this.innerProofGenerator.setVaryJustifications(varyJustifications);
        this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
        this.varyJustificationsTimeStamp = this.proofPreferencesManager.getVaryJustificationsTimeStamp();
    }

}
