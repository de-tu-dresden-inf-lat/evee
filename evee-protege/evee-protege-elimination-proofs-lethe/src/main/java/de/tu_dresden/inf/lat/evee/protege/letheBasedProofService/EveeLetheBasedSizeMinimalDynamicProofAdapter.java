package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter;
import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.ApproximateProofMeasureInferenceNumber;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.MinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.ProofEvaluatorInferenceNumber$;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.util.HashSet;

public class EveeLetheBasedSizeMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;
    private MinimalForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSizeMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSizeMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void setProofGeneratorParameters(OWLEditorKit editorKit){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
        this.innerProofGenerator = new MinimalForgettingBasedProofGenerator(
                ProofEvaluatorInferenceNumber$.MODULE$,
                new ApproximateProofMeasureInferenceNumber(JavaConverters.asScalaSet(new HashSet<OWLEntity>()).toSet()),
                LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
                OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                skipSteps);
        super.setProofGenerator(this.innerProofGenerator);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
    }

}
