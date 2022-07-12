package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedWeightedSizeMinimalProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedWeightedSizeMinimalProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedEliminationProofPreferencesManager.WEIGHTED_SIZE_MINIMAL;

    public EveeLetheBasedWeightedSizeMinimalProofService(){
        super(new EveeLetheBasedMinimalDynamicProofAdapter(
                new LetheBasedWeightedSizeMinimalProofGenerator(),
                new EveeLetheBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}
