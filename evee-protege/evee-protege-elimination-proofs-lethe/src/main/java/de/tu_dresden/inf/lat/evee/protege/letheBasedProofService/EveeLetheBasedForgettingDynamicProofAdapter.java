package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.ForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.OWLSignatureBasedMinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.util.HashSet;

public class EveeLetheBasedForgettingDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedForgettingDynamicProofAdapter.class);

    public EveeLetheBasedForgettingDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void setProofGeneratorParameters() {
        if (this.proofPreferencesManager.proofPreferencesChanged(this.preferencesUsedLast)){
            boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
            long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
            ForgettingBasedProofGenerator innerProofGenerator = new ForgettingBasedProofGenerator(
                    LetheBasedForgetter.ALC_ABox(timeOut),
                    ALCHTBoxFilter$.MODULE$,
                    OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                    skipSteps, JavaConverters.asScalaSet(new HashSet<>()));
            super.setProofGenerator(new OWLSignatureBasedMinimalTreeProofGenerator(innerProofGenerator));
            this.cachingProofGen.setOntology(this.ontology);
            this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
            this.logger.debug("Long parameter timeOut set to " + timeOut);
        }
        super.setProofGeneratorParameters();
    }

}
