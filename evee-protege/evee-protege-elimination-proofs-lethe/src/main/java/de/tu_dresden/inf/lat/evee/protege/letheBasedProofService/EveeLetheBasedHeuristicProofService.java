package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedHeuristicProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedEliminationProofPreferencesManager.HEURISTIC;

    public EveeLetheBasedHeuristicProofService(){
        super(new EveeLetheBasedForgettingDynamicProofAdapter(
                new LetheBasedHeuristicProofGenerator(),
                new EveeLetheBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}
