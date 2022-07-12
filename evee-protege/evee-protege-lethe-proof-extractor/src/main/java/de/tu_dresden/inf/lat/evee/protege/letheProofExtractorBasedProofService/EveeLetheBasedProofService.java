package de.tu_dresden.inf.lat.evee.protege.letheProofExtractorBasedProofService;

import de.tu_dresden.inf.lat.evee.proofs.lethe.LetheProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.CachingProofGenerator;
import de.tu_dresden.inf.lat.evee.proofs.proofGenerators.MinimalTreeProofGenerator;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.AbstractEveeProofService;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicProofLoadingUI;
import de.tu_dresden.inf.lat.evee.protege.abstractProofService.ui.EveeDynamicSuboptimalProofLoadingUI;

public class EveeLetheBasedProofService extends AbstractEveeProofService {

    private static final String identifier = EveeLetheBasedExtractorProofPreferencesManager.DETAILED;

    public EveeLetheBasedProofService(){
        super(new EveeLetheBasedDynamicProofAdapter(
                new MinimalTreeProofGenerator<>(new LetheProofGenerator()),
                new EveeLetheBasedExtractorProofPreferencesManager(identifier),
                new EveeDynamicProofLoadingUI(identifier)));
    }

}
