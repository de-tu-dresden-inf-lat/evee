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

    private final Logger logger = LoggerFactory.getLogger(EveeFameBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeFameBasedSymbolMinimalDynamicProofAdapter(SymbolMinimalForgettingBasedProofGenerator proofGen, AbstractEveeEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
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
            boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
            this.innerProofGenerator.setVaryJustifications(varyJustifications);
            super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(this.innerProofGenerator));
            this.cachingProofGen.setOntology(this.ontology);
            this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
            this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
        }
        super.setProofGeneratorParameters();
    }

}
