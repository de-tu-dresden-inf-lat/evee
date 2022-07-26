package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSymbolMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void setProofGeneratorParameters(){
        if (this.proofPreferencesManager.proofPreferencesChanged(this.preferencesUsedLast)){
            boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
            long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
            boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
            SymbolMinimalForgettingBasedProofGenerator innerProofGenerator = new SymbolMinimalForgettingBasedProofGenerator(
                    LetheBasedForgetter.ALC_ABox(timeOut),
                    ALCHTBoxFilter$.MODULE$,
                    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                    skipSteps,
                    varyJustifications);
            super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(innerProofGenerator));
            this.cachingProofGen.setOntology(this.ontology);
            this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
            this.logger.debug("Long parameter timeOut set to " + timeOut);
            this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
        }
        super.setProofGeneratorParameters();
    }

}
