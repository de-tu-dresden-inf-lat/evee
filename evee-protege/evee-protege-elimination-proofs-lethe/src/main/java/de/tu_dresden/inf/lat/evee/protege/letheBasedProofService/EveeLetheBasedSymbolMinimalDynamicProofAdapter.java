package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.SymbolMinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedSymbolMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;
    private SymbolMinimalForgettingBasedProofGenerator innerProofGenerator;
    private long skipStepsTimeStamp;
    private long varyJustificationsTimeStamp;
    private long timeOutTimeStamp;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedSymbolMinimalDynamicProofAdapter.class);

    public EveeLetheBasedSymbolMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
        this.createInnerProofGenerator();
        super.setInnerProofGenerator(innerProofGenerator);
        this.logger.debug("Dynamic proof adapter created.");
    }

    private void createInnerProofGenerator(){
        this.logger.debug("Creating new inner proof generator.");
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        boolean varyJustifications = this.proofPreferencesManager.loadVaryJustifications();
        long timeOut = (long) ((1000) * this.proofPreferencesManager.loadTimeOutSeconds());
        this.innerProofGenerator = new SymbolMinimalForgettingBasedProofGenerator(
                LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
                OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
                skipSteps,
                varyJustifications);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Boolean parameter varyJustifications set to " + varyJustifications);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
        this.skipStepsTimeStamp = this.proofPreferencesManager.getSkipStepsTimeStamp();
        this.varyJustificationsTimeStamp = this.proofPreferencesManager.getVaryJustificationsTimeStamp();
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
        else{
            if (this.proofPreferencesManager.skipStepsChanged(this.skipStepsTimeStamp)){
                this.setSkipSteps();
                parameterChanged = true;
            }
            if (this.proofPreferencesManager.varyJustificationsChanged(this.varyJustificationsTimeStamp)){
                this.setVaryJustifications();
                parameterChanged = true;
            }
        }
        if (parameterChanged){
            super.setInnerProofGenerator(innerProofGenerator);
        }
        super.setProofGeneratorParameters(previousParametersChanged || parameterChanged);
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
