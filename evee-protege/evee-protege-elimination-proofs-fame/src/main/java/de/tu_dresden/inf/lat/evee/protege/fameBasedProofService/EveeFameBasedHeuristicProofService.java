package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeFameBasedHeuristicProofService extends AbstractEveeProofService {

    private static final String identifier = EveeFameBasedEliminationProofPreferencesManager.HEURISTIC;

    public EveeFameBasedHeuristicProofService(){
        super(new EveeFameBasedForgettingDynamicProofAdapter(
                new FameBasedHeuristicProofGenerator(),
                new EveeFameBasedEliminationProofPreferencesManager(identifier),
                new EveeDynamicSuboptimalProofLoadingUI(identifier)));
    }

}
