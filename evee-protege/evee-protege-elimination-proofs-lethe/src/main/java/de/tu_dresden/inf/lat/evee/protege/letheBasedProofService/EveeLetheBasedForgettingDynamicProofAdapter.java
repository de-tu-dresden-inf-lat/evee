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
    private ForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;
    private long timeOutTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedForgettingDynamicProofAdapter.class);

    public EveeLetheBasedForgettingDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.createInnerProofGenerator();
        super.setInnerProofGenerator(this.innerProofGenerator);
        this.logger.debug("Dynamic proof adapter created.");
    }

    private void createInnerProofGenerator(){
        this.logger.debug("Creating new inner proof generator.");
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
        this.innerProofGenerator = new ForgettingBasedProofGenerator(
                LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
                OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                skipSteps,
                JavaConverters.asScalaSet(new HashSet<>()));
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
        this.timeOutTimeStamp = this.proofPreferencesManager.getTimeOutTimeStamp();
    }

    @Override
    protected void setProofGeneratorParameters(boolean previousParametersChanged) {
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
        super.setProofGeneratorParameters(parameterChanged || previousParametersChanged);
    }

    private void setSkipSteps(){
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        this.innerProofGenerator.setSkipSteps(skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
    }

}
