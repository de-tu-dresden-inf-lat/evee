package de.tu_dresden.inf.lat.evee.protege.letheBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.LetheBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedHeuristicProofService extends AbstractEveeProofService {

    public EveeLetheBasedHeuristicProofService(){
        super(new CachingProofGenerator<>(new LetheBasedHeuristicProofGenerator()),
                EveeLetheBasedEliminationProofPreferencesManager.HEURISTIC,
                new EveeLetheBasedEliminationProofPreferencesManager(
                        EveeLetheBasedEliminationProofPreferencesManager.HEURISTIC));
    }

}
