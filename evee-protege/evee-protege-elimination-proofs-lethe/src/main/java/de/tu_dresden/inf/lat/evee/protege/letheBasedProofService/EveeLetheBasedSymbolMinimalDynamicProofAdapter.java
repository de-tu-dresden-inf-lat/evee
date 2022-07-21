package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter;
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;
    private SymbolMinimalForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSymbolMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void setProofGeneratorParameters(OWLEditorKit editorKit){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
        boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
        this.innerProofGenerator = new SymbolMinimalForgettingBasedProofGenerator(
                LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
                OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                skipSteps,
                varyJustifications);
        super.setProofGenerator(this.innerProofGenerator);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
        this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
    }

}
