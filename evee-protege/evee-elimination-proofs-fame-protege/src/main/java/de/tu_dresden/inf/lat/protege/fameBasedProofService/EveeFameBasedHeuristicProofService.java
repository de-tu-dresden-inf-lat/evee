package de.tu_dresden.inf.lat.protege.fameBasedProofService;

import de.tu_dresden.inf.lat.forgettingBasedProofs.FameBasedHeuristicProofGenerator;
import de.tu_dresden.inf.lat.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeFameBasedHeuristicProofService extends AbstractEveeProofService {

    private Logger logger = LoggerFactory.getLogger(de.tu_dresden.inf.lat.protege.fameBasedProofService.EveeFameBasedHeuristicProofService.class);

    public EveeFameBasedHeuristicProofService(){
        super(new CachingProofGenerator<>(new FameBasedHeuristicProofGenerator()),
                "Elimination Proof (FAME)", "Elimination Proof (FAME)", "de.tu_dresden.inf.lat.EveeFameBasedHeuristicProofService", "Elimination Proof (FAME)_DoNotShowAgain");
    }

}
