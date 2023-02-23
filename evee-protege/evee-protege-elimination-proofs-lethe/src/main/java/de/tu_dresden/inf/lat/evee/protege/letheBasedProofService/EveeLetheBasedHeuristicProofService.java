package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;

public class EveeLetheBasedHeuristicProofService extends AbstractEveeProofService {

    public EveeLetheBasedHeuristicProofService(){
        super(new EveeLetheBasedEliminationDynamicProofAdapter(
                new CachingProofGenerator<>(new LetheBasedHeuristicProofGenerator()),
                EveeLetheBasedEliminationProofPreferencesManager.HEURISTIC,
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.HEURISTIC)));
    }

}
