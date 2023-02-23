package de.tu_dresden.inf.lat.evee.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.evee.eliminationProofs.FameBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedHeuristicProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(EveeFameBasedHeuristicProofService.class);
    private static final String identifier = "Elimination Proof (FAME)";

    public EveeFameBasedHeuristicProofService(){
        super(new CachingProofGenerator<>(new FameBasedHeuristicProofGenerator()),
                identifier, new EveeFameBasedEliminationProofPreferencesManager(identifier));
    }

}
