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

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedMinimalDynamicProofAdapter.class);

    public EveeFameBasedMinimalDynamicProofAdapter(MinimalForgettingBasedProofGenerator proofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(proofGen));
        this.proofPreferencesManager = proofPreferencesManager;
        this.innerProofGenerator = proofGen;
    }

    @Override
    protected void setProofGeneratorParameters() {
        if (this.proofPreferencesManager.proofPreferencesChanged(this.preferencesUsedLast)){
            boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
            this.innerProofGenerator.setSkipSteps(skipSteps);
            super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(this.innerProofGenerator));
            this.cachingProofGen.setOntology(this.ontology);
            logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        }
        super.setProofGeneratorParameters();
    }

}
