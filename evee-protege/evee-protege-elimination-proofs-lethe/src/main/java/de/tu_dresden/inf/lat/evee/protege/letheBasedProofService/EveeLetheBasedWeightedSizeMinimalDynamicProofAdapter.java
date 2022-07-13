package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.dltools.ALCHTBoxFilter$;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.LetheBasedForgetter;
import de.tu_dresden.inf.lat.evee.eliminationProofs.adaptors.OWLApiBasedJustifier;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.ApproximateProofMeasureAxiomSizeSum;
import de.tu_dresden.inf.lat.evee.eliminationProofs.minimal.MinimalForgettingBasedProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.tools.RecursiveProofEvaluator;
import de.tu_dresden.inf.lat.evee.proofs.tools.measures.OWLAxiomSizeWeightedTreeSizeMeasure;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeSuboptimalDynamicProofAdapter;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.util.Collections;
import java.util.HashSet;

public class EveeLetheBasedWeightedSizeMinimalDynamicProofAdapter extends AbstractEveeSuboptimalDynamicProofAdapter {

    private final EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager;
    private MinimalForgettingBasedProofGenerator innerProofGenerator;

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedWeightedSizeMinimalDynamicProofAdapter.class);

    public EveeLetheBasedWeightedSizeMinimalDynamicProofAdapter(EveeLetheBasedEliminationProofPreferencesManager proofPreferencesManager, EveeDynamicSuboptimalProofLoadingUI uiWindow) {
        super(proofPreferencesManager, uiWindow);
        this.proofPreferencesManager = proofPreferencesManager;
    }

    @Override
    protected void setProofGeneratorParameters() {
        boolean skipSteps = this.proofPreferencesManager.loadSkipSteps();
        long timeOut = (long) (1000 * this.proofPreferencesManager.loadTimeOut());
        this.innerProofGenerator = new MinimalForgettingBasedProofGenerator(
            new RecursiveProofEvaluator<>(new OWLAxiomSizeWeightedTreeSizeMeasure()),
            new ApproximateProofMeasureAxiomSizeSum(JavaConverters.asScalaSet(new HashSet<OWLEntity>()).toSet()),
            LetheBasedForgetter.ALC_ABox(timeOut),
                ALCHTBoxFilter$.MODULE$,
            OWLApiBasedJustifier.UsingHermiT(OWLManager.createOWLOntologyManager()),
            skipSteps);
        this.logger.debug("Boolean parameter skipSteps set to " + skipSteps);
        this.logger.debug("Long parameter timeOut set to " + timeOut);
        super.setProofGenerator(this.innerProofGenerator);
    }

    @Override
    protected void checkOntology(){
        this.innerProofGenerator.setOntology(this.ontology);
    }

}
