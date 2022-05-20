package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    private final Logger logger = LoggerFactory.getLogger(EveeLetheBasedProofService.class);
    private static final String identifier =  "Detailed Proof";

    public EveeLetheBasedProofService(){
        super(new CachingProofGenerator<>(new MinimalTreeProofGenerator(new LetheProofGenerator())),
                identifier, new EveeLetheBasedExtractorProofPreferencesManager(identifier));
    }

}
