package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.ApproximateProofMeasureInferenceNumber;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.MinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.ProofEvaluatorInferenceNumber$;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.util.HashSet;

public class EveeLetheBasedSizeMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;
    private MinimalForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;
    private long timeOutTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSizeMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSizeMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.createInnerProofGenerator();
        super.setInnerProofGenerator(this.innerProofGenerator);
        super.resetCachingProofGenerator();
        this.logger.debug("Dynamic proof adapter created.");
    }

    private void createInnerProofGenerator(){
        this.logger.debug("Creating new inner proof generator.");
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOutSeconds());
        this.innerProofGenerator = new MinimalForgettingBasedProofGenerator(
                ProofEvaluatorInferenceNumber$.MODULE$,
                new ApproximateProofMeasureInferenceNumber(JavaConverters.asScalaSet(new HashSet<OWLEntity>()).toSet()),
                LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
                OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
        this.timeOutTimeStamp = this.proofPreferencesManager.getTimeOutTimeStamp();
    }

    @Override
    protected void setProofGeneratorParameters(boolean previousParametersChanged){
        this.logger.debug("Checking parameters for proof generator.");
        boolean parameterChanged = false;
        if (this.proofPreferencesManager.timeOutChanged(this.timeOutTimeStamp)){
            this.createInnerProofGenerator();
            parameterChanged = true;
        }
        else if (this.proofPreferencesManager.skipStepsChanged(this.skipStepsTimeStamp)){
            this.setSkipSteps();
            parameterChanged = true;
        }
        if (parameterChanged){
            super.setInnerProofGenerator(this.innerProofGenerator);
        }
        super.setProofGeneratorParameters(previousParametersChanged || parameterChanged);
    }

    private void setSkipSteps(){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
    }

}
